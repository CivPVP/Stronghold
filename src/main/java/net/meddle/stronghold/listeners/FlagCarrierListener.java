package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.event.EventPhase;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class FlagCarrierListener {

    private final Stronghold plugin;
    private final FlagManager flags;
    BukkitTask broadcastTask;
    private BukkitTask glowTask;

    public FlagCarrierListener(Stronghold plugin) {
        this.plugin = plugin;
        this.flags  = plugin.getFlagManager();
    }

    public void startBroadcastTask() {
        stopBroadcastTask();
        long intervalTicks = plugin.getCfg().getCarrierIntervalSeconds() * 20L;
        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastCarriers, intervalTicks, intervalTicks);
        // 1-second glow reconciliation: catches any missed pickup/drop/deposit events
        glowTask = Bukkit.getScheduler().runTaskTimer(plugin, this::reapplyAllGlow, 20L, 20L);
    }

    public void stopBroadcastTask() {
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
        if (glowTask != null && !glowTask.isCancelled()) {
            glowTask.cancel();
            glowTask = null;
        }
    }

    private void broadcastCarriers() {
        boolean activeOnly = plugin.getCfg().isCarrierEffectsActiveOnly();
        if (activeOnly && !isActivePhase()) return;

        // Regular flag carriers
        for (Player p : Bukkit.getOnlinePlayers()) {
            List<String> heldFlags = flags.getFlagsHeldBy(p.getUniqueId());
            if (heldFlags.isEmpty()) continue;

            Component flagList = Component.empty();
            for (int i = 0; i < heldFlags.size(); i++) {
                if (i > 0) flagList = flagList.append(Component.text(", ", Msg.LIGHT_BLUE));
                Team t = plugin.getTeamManager().getTeam(heldFlags.get(i));
                flagList = flagList.append(
                    (t != null ? Msg.teamName(t) : Component.text(heldFlags.get(i), Msg.LIGHT_BLUE))
                        .append(Component.text("'s Flag", Msg.LIGHT_BLUE)));
            }

            int x = p.getLocation().getBlockX();
            int y = p.getLocation().getBlockY();
            int z = p.getLocation().getBlockZ();
            Msg.broadcast(Component.text(p.getName(), Msg.WHITE)
                .append(Component.text(" is carrying ", Msg.LIGHT_BLUE))
                .append(flagList)
                .append(Component.text(" at " + x + ", " + y + ", " + z
                    + " in " + p.getWorld().getName() + "!", Msg.LIGHT_BLUE)));
        }

        // Tie-breaking flag carriers — scan inventories directly so records can't lag
        var tbm = plugin.getTieBreakManager();
        for (Player holder : Bukkit.getOnlinePlayers()) {
            if (!tbm.playerHoldsTBFlag(holder)) continue;
            int x = holder.getLocation().getBlockX();
            int y = holder.getLocation().getBlockY();
            int z = holder.getLocation().getBlockZ();
            Msg.broadcast(Component.text(holder.getName(), Msg.WHITE)
                .append(Component.text(" is carrying the ", Msg.LIGHT_BLUE))
                .append(Component.text("Tie-Breaking Flag", Msg.WHITE).decorate(TextDecoration.BOLD))
                .append(Component.text(" at " + x + ", " + y + ", " + z
                    + " in " + holder.getWorld().getName() + "!", Msg.LIGHT_BLUE)));
        }
    }

    /** Applies or clears glow for a single player based on what flags they currently hold. */
    public void applyGlowForPlayer(Player p) {
        boolean holdsRegular = !flags.getFlagsHeldBy(p.getUniqueId()).isEmpty();
        boolean holdsTB      = plugin.getTieBreakManager().playerHoldsTBFlag(p);
        if (!holdsRegular && !holdsTB) {
            p.setGlowing(false);
            return;
        }
        if (plugin.getCfg().isGlowEnabled()) p.setGlowing(true);
    }

    /**
     * Re-applies glow to all online flag carriers and verifies that players the records
     * say are holding a flag actually have it in their inventory. If not (e.g. /clear was
     * used), the flag is silently returned to its vault.
     */
    public void reapplyAllGlow() {
        boolean activeOnly = plugin.getCfg().isCarrierEffectsActiveOnly();
        var tbm = plugin.getTieBreakManager();

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Verify every regular flag the record says this player is holding
            for (String owner : flags.getFlagsHeldBy(p.getUniqueId())) {
                boolean hasIt = false;
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null && owner.equals(flags.getFlagOwner(item))) { hasIt = true; break; }
                }
                if (!hasIt) {
                    net.meddle.stronghold.team.Team vaultTeam = flags.placeInLastVault(owner);
                    if (vaultTeam != null) {
                        net.meddle.stronghold.team.Team ot = plugin.getTeamManager().getTeam(owner);
                        Msg.broadcast(
                            (ot != null ? Msg.teamName(ot) : Component.text(owner, Msg.LIGHT_BLUE))
                                .append(Component.text("'s Flag has returned to its vault.", Msg.LIGHT_BLUE)));
                    }
                }
            }

            boolean holdsRegular = !flags.getFlagsHeldBy(p.getUniqueId()).isEmpty();
            boolean holdsTB      = tbm.playerHoldsTBFlag(p);
            boolean carriersActive = !activeOnly || isActivePhase();
            if ((!holdsRegular && !holdsTB) || !carriersActive) {
                p.setGlowing(false);
                continue;
            }
            if (plugin.getCfg().isGlowEnabled()) p.setGlowing(true);
        }
    }

    private boolean isActivePhase() {
        var phase = plugin.getEventManager().getPhase();
        return phase == EventPhase.ACTIVE || phase == EventPhase.TIE_BREAK_ACTIVE;
    }
}
