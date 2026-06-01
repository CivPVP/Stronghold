package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.flag.FlagState;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

import java.util.UUID;

public class FlagDespawnListener implements Listener {

    private final Stronghold plugin;
    private final FlagManager flags;

    public FlagDespawnListener(Stronghold plugin) {
        this.plugin = plugin;
        this.flags  = plugin.getFlagManager();
        startOrphanCheck();
    }

    // ── /kill detection via EntityRemoveEvent ─────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(EntityRemoveEvent e) {
        if (!(e.getEntity() instanceof Item)) return;

        switch (e.getCause()) {
            case UNLOAD  -> { return; }
            case PICKUP  -> { return; }
            case MERGE   -> { return; }
            case DESPAWN -> { return; }
            default      -> {}
        }

        UUID entityId = e.getEntity().getUniqueId();
        FlagRecord matched = null;
        for (FlagRecord r : flags.getAllRecords()) {
            if (r.getState() == FlagState.DROPPED && entityId.equals(r.getDroppedEntityUUID())) {
                matched = r;
                break;
            }
        }
        if (matched == null) return;

        final String owner = matched.getOwnerTeam();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            FlagRecord current = flags.getRecord(owner);
            if (current == null || current.getState() == FlagState.IN_VAULT) return;
            Team vaultTeam = flags.placeInLastVault(owner);
            if (vaultTeam == null) return;
            broadcastReturned(owner);
        });
    }

    // ── Natural despawn: cancel and return ───────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        Item item = e.getEntity();
        if (!flags.isFlag(item.getItemStack())) return;

        e.setCancelled(true);

        String owner = flags.getFlagOwner(item.getItemStack());
        if (owner == null) { item.remove(); return; }

        Team vaultTeam = flags.placeInLastVault(owner);
        item.remove();
        if (vaultTeam == null) return;

        broadcastReturned(owner);
    }

    // ── Periodic checks ───────────────────────────────────────────────────────

    /**
     * Every second:
     * - If a tracked DROPPED flag entity is below Y=0, remove it and return to vault.
     * - If the entity is gone and its chunk is loaded, return to vault (safety net).
     */
    private void startOrphanCheck() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (FlagRecord r : flags.getAllRecords()) {
                if (r.getState() != FlagState.DROPPED) continue;
                if (r.getDroppedEntityUUID() == null) continue;

                Entity entity = Bukkit.getEntity(r.getDroppedEntityUUID());

                if (entity != null) {
                    // Entity alive but in the void
                    if (entity.getLocation().getY() < 0) {
                        entity.remove();
                        String owner = r.getOwnerTeam();
                        Team vaultTeam = flags.placeInLastVault(owner);
                        if (vaultTeam != null) broadcastReturned(owner);
                    }
                    continue;
                }

                // Entity gone — only act if chunk is loaded (avoids false positives from unloaded chunks)
                if (r.getDroppedWorld() != null) {
                    World world = Bukkit.getWorld(r.getDroppedWorld());
                    if (world == null) continue;
                    int cx = (int) r.getDroppedX() >> 4;
                    int cz = (int) r.getDroppedZ() >> 4;
                    if (!world.isChunkLoaded(cx, cz)) continue;
                }

                String owner = r.getOwnerTeam();
                Team vaultTeam = flags.placeInLastVault(owner);
                if (vaultTeam == null) continue;
                broadcastReturned(owner);
            }
        }, 20L, 20L);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    void broadcastReturned(String ownerTeamName) {
        Team ownerTeam = plugin.getTeamManager().getTeam(ownerTeamName);
        Msg.broadcast(
            (ownerTeam != null ? Msg.teamName(ownerTeam) : Component.text(ownerTeamName, Msg.LIGHT_BLUE))
                .append(Component.text("'s Flag has returned to its vault.", Msg.LIGHT_BLUE))
        );
    }
}
