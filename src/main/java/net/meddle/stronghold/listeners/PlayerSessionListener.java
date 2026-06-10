package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.team.Team;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerSessionListener implements Listener {

    private final Stronghold plugin;
    private final FlagManager flags;

    public PlayerSessionListener(Stronghold plugin) {
        this.plugin = plugin;
        this.flags  = plugin.getFlagManager();
    }

    // ── Join ──────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Restore scoreboard team membership (also calls applyTabColor)
        Team team = plugin.getTeamManager().getTeamOf(p.getUniqueId());
        if (team != null) {
            plugin.getScoreboardManager().addPlayer(p, team);
            // Refresh stored name in case it changed since they were added
            team.setMemberName(p.getUniqueId(), p.getName());
            plugin.getTeamManager().save(team);
        } else {
            p.setScoreboard(plugin.getScoreboardManager().getScoreboard());
        }

        // TAB may not have loaded the player yet — re-apply after 2 seconds
        if (team != null) {
            final Team t = team;
            plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getScoreboardManager().applyTabColor(p, t), 40L);
        }

        // Add to active bossbar
        plugin.getBossBarManager().addPlayer(p);

        // Restore glow and TB records if this player is holding any flag
        var held = flags.getFlagsHeldBy(p.getUniqueId());
        var tbm  = plugin.getTieBreakManager();
        boolean holdsTB = tbm.playerHoldsTBFlag(p);
        if (!held.isEmpty() || holdsTB) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                // Re-register TB flag records that may have been wiped by a restart
                if (holdsTB) {
                    for (ItemStack item : p.getInventory().getContents()) {
                        if (!tbm.isTieBreakFlag(item)) continue;
                        UUID id = tbm.getTBFlagUUID(item);
                        if (id != null && tbm.getAllTBRecords().get(id) == null) {
                            tbm.onTBPickedUp(id, p);
                        }
                    }
                }
            });
        }
    }

    // ── Quit ──────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        // Drop all flags the player is carrying (regular + tie-breaking)
        var tbm = plugin.getTieBreakManager();
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;

            if (flags.isFlag(item)) {
                String owner = flags.getFlagOwner(item);
                if (owner == null) continue;
                p.getInventory().remove(item);
                Item dropped = p.getWorld().dropItem(p.getLocation(), item);
                dropped.setPickupDelay(40);
                FlagRecord r = flags.getRecord(owner);
                if (r != null) flags.onFlagDropped(owner, dropped.getLocation(), dropped.getUniqueId());
                Team ownerTeam = plugin.getTeamManager().getTeam(owner);
                int x = dropped.getLocation().getBlockX();
                int y = dropped.getLocation().getBlockY();
                int z = dropped.getLocation().getBlockZ();
                Msg.broadcast(Component.text(p.getName(), Msg.WHITE)
                    .append(Component.text(" dropped ", Msg.LIGHT_BLUE))
                    .append(ownerTeam != null ? Msg.teamName(ownerTeam) : Component.text(owner, Msg.LIGHT_BLUE))
                    .append(Component.text("'s Flag at " + x + ", " + y + ", " + z + "!", Msg.LIGHT_BLUE)));

            } else if (tbm.isTieBreakFlag(item)) {
                UUID flagId = tbm.getTBFlagUUID(item);
                p.getInventory().remove(item);
                Item dropped = p.getWorld().dropItem(p.getLocation(), item);
                dropped.setPickupDelay(40);
                if (flagId != null) tbm.onTBDropped(flagId, dropped.getLocation(), dropped.getUniqueId());
                int x = dropped.getLocation().getBlockX();
                int y = dropped.getLocation().getBlockY();
                int z = dropped.getLocation().getBlockZ();
                Msg.broadcast(Component.text(p.getName(), Msg.WHITE)
                    .append(Component.text(" dropped the ", Msg.LIGHT_BLUE))
                    .append(Component.text("Tie-Breaking Flag", Msg.WHITE).decorate(TextDecoration.BOLD))
                    .append(Component.text(" at " + x + ", " + y + ", " + z + "!", Msg.LIGHT_BLUE)));
            }
        }

        // Clear glow state
        p.setGlowing(false);
    }
}
