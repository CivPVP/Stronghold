package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.team.Team;
import net.meddle.stronghold.team.TeamManager;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FlagItemListener implements Listener {

    private final Stronghold plugin;
    private final FlagManager flags;
    private final TeamManager teams;

    public FlagItemListener(Stronghold plugin) {
        this.plugin = plugin;
        this.flags  = plugin.getFlagManager();
        this.teams  = plugin.getTeamManager();
    }

    // ── Pickup ────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        ItemStack item = e.getItem().getItemStack();
        if (!flags.isFlag(item)) return;

        if (flags.isDuplicate(item)) {
            e.getItem().remove();
            e.setCancelled(true);
            return;
        }

        String owner = flags.getFlagOwner(item);
        if (owner == null) return;

        flags.onFlagPickedUp(owner, p);
        plugin.getFlagCarrierListener().applyGlowForPlayer(p);

        Team ownerTeam = teams.getTeam(owner);
        if (ownerTeam != null) {
            Msg.broadcast(
                Component.text(p.getName(), Msg.WHITE)
                    .append(Component.text(" stole ", Msg.LIGHT_BLUE))
                    .append(Msg.teamName(ownerTeam))
                    .append(Component.text("'s Flag!", Msg.LIGHT_BLUE))
            );
            p.getWorld().getPlayers().forEach(pl ->
                pl.playSound(pl.getLocation(), plugin.getCfg().getSoundFlagStolen(), 1f, 1f));
        }
    }

    // ── Drop ──────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Item item = e.getItemDrop();
        if (!flags.isFlag(item.getItemStack())) return;

        String owner = flags.getFlagOwner(item.getItemStack());
        if (owner == null) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            flags.onFlagDropped(owner, item.getLocation(), item.getUniqueId());

            Team ownerTeam = teams.getTeam(owner);
            if (ownerTeam != null) {
                int x = item.getLocation().getBlockX();
                int y = item.getLocation().getBlockY();
                int z = item.getLocation().getBlockZ();
                Msg.broadcast(
                    Component.text(e.getPlayer().getName(), Msg.WHITE)
                        .append(Component.text(" dropped ", Msg.LIGHT_BLUE))
                        .append(Msg.teamName(ownerTeam))
                        .append(Component.text("'s Flag at " + x + ", " + y + ", " + z + "!", Msg.LIGHT_BLUE))
                );
            }

            updateGlow(e.getPlayer());
        });
    }

    // ── Death ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        List<ItemStack> drops = e.getDrops();

        for (ItemStack item : drops) {
            if (!flags.isFlag(item)) continue;
            String owner = flags.getFlagOwner(item);
            if (owner == null) continue;

            plugin.getServer().getScheduler().runTask(plugin, () ->
                p.getWorld().getNearbyEntities(p.getLocation(), 3, 3, 3).stream()
                    .filter(en -> en instanceof Item it && flags.isFlagOf(it.getItemStack(), owner))
                    .findFirst()
                    .ifPresent(en -> flags.onFlagDropped(owner, en.getLocation(), en.getUniqueId()))
            );
        }

        p.setGlowing(false);
        plugin.getScoreboardManager().setGlowColor(p, null);
    }

    // ── Lava / Fire protection ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(EntityDamageEvent e) {
        if (!plugin.getCfg().isPreventFlagBurn()) return;
        if (!(e.getEntity() instanceof Item item)) return;
        if (!flags.isFlag(item.getItemStack())) return;

        switch (e.getCause()) {
            case FIRE, FIRE_TICK, LAVA, MELTING, VOID -> e.setCancelled(true);
            default -> {}
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    void updateGlow(Player p) {
        plugin.getFlagCarrierListener().applyGlowForPlayer(p);
    }

    /** Broadcast a §-encoded string with the CivPvP) prefix. Used by other classes. */
    public static void broadcast(String legacyMsg) {
        Msg.broadcast(legacyMsg);
    }
}
