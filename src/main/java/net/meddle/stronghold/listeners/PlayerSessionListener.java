package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
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
        if (team != null) plugin.getScoreboardManager().addPlayer(p, team);
        else p.setScoreboard(plugin.getScoreboardManager().getScoreboard());

        // TAB may not have loaded the player yet — re-apply after 2 seconds
        if (team != null) {
            final Team t = team;
            plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getScoreboardManager().applyTabColor(p, t), 40L);
        }

        // Add to active bossbar
        plugin.getBossBarManager().addPlayer(p);

        // Restore glow if this player was holding a flag (guard against edge cases)
        var held = flags.getFlagsHeldBy(p.getUniqueId());
        if (!held.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin,
                () -> plugin.getFlagCarrierListener().applyGlowForPlayer(p));
        }
    }

    // ── Quit ──────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        // Drop all flags the player is carrying
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || !flags.isFlag(item)) continue;
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
            Component msg = Component.text(p.getName(), Msg.WHITE)
                .append(Component.text(" dropped ", Msg.LIGHT_BLUE))
                .append(ownerTeam != null ? Msg.teamName(ownerTeam) : Component.text(owner, Msg.LIGHT_BLUE))
                .append(Component.text("'s Flag at " + x + ", " + y + ", " + z + "!", Msg.LIGHT_BLUE));
            Msg.broadcast(msg);
        }

        // Clear glow state
        p.setGlowing(false);
    }
}
