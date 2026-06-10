package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.flag.FlagState;
import net.meddle.stronghold.team.Team;
import net.meddle.stronghold.team.TeamManager;
import org.bukkit.entity.Entity;
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

        // Tie-breaking flag: only players from tied teams may pick it up
        if (plugin.getTieBreakManager().isTieBreakFlag(item)) {
            if (!plugin.getTieBreakManager().isFromTiedTeam(p)) {
                e.setCancelled(true);
                return;
            }
            java.util.UUID flagId = plugin.getTieBreakManager().getTBFlagUUID(item);
            if (flagId != null) plugin.getTieBreakManager().onTBPickedUp(flagId, p);
            plugin.getFlagCarrierListener().applyGlowForPlayer(p);
            Msg.broadcast(
                Component.text(p.getName(), Msg.WHITE)
                    .append(Component.text(" picked up the ", Msg.LIGHT_BLUE))
                    .append(Component.text("Tie-Breaking Flag", Msg.WHITE).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
                    .append(Component.text("!", Msg.LIGHT_BLUE))
            );
            return;
        }

        if (!flags.isFlag(item)) return;

        if (flags.isDuplicate(item)) {
            e.getItem().remove();
            e.setCancelled(true);
            return;
        }

        if (teams.getTeamOf(p.getUniqueId()) == null) {
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

        if (plugin.getTieBreakManager().isTieBreakFlag(item.getItemStack())) {
            java.util.UUID flagId = plugin.getTieBreakManager().getTBFlagUUID(item.getItemStack());
            if (flagId != null) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    plugin.getTieBreakManager().onTBDropped(flagId, item.getLocation(), item.getUniqueId())
                );
            }
            updateGlow(e.getPlayer());
            return;
        }

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
        // keepInventory: player keeps flags — no drop, no state change needed
        if (Boolean.TRUE.equals(p.getWorld().getGameRuleValue(org.bukkit.GameRule.KEEP_INVENTORY))) return;
        List<ItemStack> drops = e.getDrops();

        for (ItemStack item : drops) {
            if (plugin.getTieBreakManager().isTieBreakFlag(item)) {
                java.util.UUID flagId = plugin.getTieBreakManager().getTBFlagUUID(item);
                if (flagId == null) continue;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Entity found = p.getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10).stream()
                        .filter(en -> en instanceof Item it
                            && plugin.getTieBreakManager().isTieBreakFlag(it.getItemStack())
                            && flagId.equals(plugin.getTieBreakManager().getTBFlagUUID(it.getItemStack())))
                        .findFirst().orElse(null);
                    if (found != null) {
                        plugin.getTieBreakManager().onTBDropped(flagId, found.getLocation(), found.getUniqueId());
                        int x = found.getLocation().getBlockX();
                        int y = found.getLocation().getBlockY();
                        int z = found.getLocation().getBlockZ();
                        Msg.broadcast(Component.text(p.getName(), Msg.WHITE)
                            .append(Component.text(" died and dropped the ", Msg.LIGHT_BLUE))
                            .append(Component.text("Tie-Breaking Flag", Msg.WHITE)
                                .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
                            .append(Component.text(" at " + x + ", " + y + ", " + z + "!", Msg.LIGHT_BLUE)));
                    }
                    // If not found: orphan recovery (EntityRemoveEvent / periodic scan) will return it to vault
                });
                continue;
            }

            if (!flags.isFlag(item)) continue;
            String owner = flags.getFlagOwner(item);
            if (owner == null) continue;

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Entity found = p.getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10).stream()
                    .filter(en -> en instanceof Item it && flags.isFlagOf(it.getItemStack(), owner))
                    .findFirst().orElse(null);

                if (found != null) {
                    flags.onFlagDropped(owner, found.getLocation(), found.getUniqueId());
                    Team ot = teams.getTeam(owner);
                    int x = found.getLocation().getBlockX();
                    int y = found.getLocation().getBlockY();
                    int z = found.getLocation().getBlockZ();
                    Msg.broadcast(Component.text(p.getName(), Msg.WHITE)
                        .append(Component.text(" died and dropped ", Msg.LIGHT_BLUE))
                        .append(ot != null ? Msg.teamName(ot) : Component.text(owner, Msg.LIGHT_BLUE))
                        .append(Component.text("'s Flag at " + x + ", " + y + ", " + z + "!", Msg.LIGHT_BLUE)));
                } else {
                    // Item entity already gone (void on death) — return to vault
                    FlagRecord r = flags.getRecord(owner);
                    if (r != null && r.getState() == FlagState.HELD) {
                        Team vaultTeam = flags.placeInLastVault(owner);
                        if (vaultTeam != null) {
                            Team ot = teams.getTeam(owner);
                            Msg.broadcast(
                                (ot != null ? Msg.teamName(ot) : Component.text(owner, Msg.LIGHT_BLUE))
                                    .append(Component.text("'s Flag has returned to its vault.", Msg.LIGHT_BLUE)));
                        }
                    }
                }
            });
        }

        p.setGlowing(false);
    }

    // ── Environmental destruction protection ──────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Item item)) return;
        boolean isTb = plugin.getTieBreakManager().isTieBreakFlag(item.getItemStack());
        if (!isTb && !flags.isFlag(item.getItemStack())) return;
        // VOID and KILL let the entity die so the orphan check can recover it.
        // Everything else (fire, lava, cactus, explosions…) is cancelled.
        // KILL: let the entity die so EntityRemoveEvent(DEATH) fires and the UUID lookup returns the flag.
        // Everything else (void, fire, lava, cactus, explosions…) is cancelled — void is detected
        // via the Y < 0 periodic check instead, which avoids relying on Paper void-damage events.
        if (e.getCause() == EntityDamageEvent.DamageCause.KILL) return;
        e.setCancelled(true);
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
