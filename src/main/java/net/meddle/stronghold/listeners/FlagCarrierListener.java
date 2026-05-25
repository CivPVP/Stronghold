package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.event.EventPhase;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class FlagCarrierListener {

    private final Stronghold plugin;
    private final FlagManager flags;
    BukkitTask broadcastTask;

    public FlagCarrierListener(Stronghold plugin) {
        this.plugin = plugin;
        this.flags  = plugin.getFlagManager();
    }

    public void startBroadcastTask() {
        stopBroadcastTask();
        long intervalTicks = plugin.getCfg().getCarrierIntervalSeconds() * 20L;
        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, this::broadcastCarriers, intervalTicks, intervalTicks);
    }

    public void stopBroadcastTask() {
        if (broadcastTask != null && !broadcastTask.isCancelled()) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    private void broadcastCarriers() {
        boolean activeOnly = plugin.getCfg().isCarrierEffectsActiveOnly();
        if (activeOnly && plugin.getEventManager().getPhase() != EventPhase.ACTIVE) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            List<String> heldFlags = flags.getFlagsHeldBy(p.getUniqueId());
            if (heldFlags.isEmpty()) continue;

            // Build team-colored flag list component
            Component flagList = Component.empty();
            for (int i = 0; i < heldFlags.size(); i++) {
                if (i > 0) flagList = flagList.append(Component.text(", ", Msg.LIGHT_BLUE));
                Team t = plugin.getTeamManager().getTeam(heldFlags.get(i));
                Component flagName = (t != null ? Msg.teamName(t) : Component.text(heldFlags.get(i), Msg.LIGHT_BLUE))
                    .append(Component.text("'s Flag", Msg.LIGHT_BLUE));
                flagList = flagList.append(flagName);
            }

            int x = p.getLocation().getBlockX();
            int y = p.getLocation().getBlockY();
            int z = p.getLocation().getBlockZ();
            Component msg = Component.text(p.getName(), Msg.WHITE)
                .append(Component.text(" is carrying ", Msg.LIGHT_BLUE))
                .append(flagList)
                .append(Component.text(" at " + x + ", " + y + ", " + z
                    + " in " + p.getWorld().getName() + "!", Msg.LIGHT_BLUE));
            Msg.broadcast(msg);
        }
    }

    /** Applies or clears glow for a single player based on what flags they currently hold. */
    public void applyGlowForPlayer(Player p) {
        List<String> held = flags.getFlagsHeldBy(p.getUniqueId());
        if (held.isEmpty()) {
            p.setGlowing(false);
            plugin.getScoreboardManager().setGlowColor(p, null);
            return;
        }
        if (!plugin.getCfg().isGlowEnabled()) return;
        String chosen = plugin.getCfg().isRandomizeMultiFlagColor()
            ? held.get((int) (Math.random() * held.size()))
            : held.get(0);
        Team t = plugin.getTeamManager().getTeam(chosen);
        if (t != null) {
            p.setGlowing(true);
            plugin.getScoreboardManager().setGlowColor(p, t.getColor());
        }
    }

    /** Re-applies glow to all online flag carriers. Call after reload or phase change. */
    public void reapplyAllGlow() {
        boolean activeOnly = plugin.getCfg().isCarrierEffectsActiveOnly();
        boolean isActive   = plugin.getEventManager().getPhase() == EventPhase.ACTIVE;

        for (Player p : Bukkit.getOnlinePlayers()) {
            List<String> held = flags.getFlagsHeldBy(p.getUniqueId());
            if (held.isEmpty() || (activeOnly && !isActive)) {
                p.setGlowing(false);
                plugin.getScoreboardManager().setGlowColor(p, null);
                continue;
            }
            if (!plugin.getCfg().isGlowEnabled()) continue;
            String chosen = plugin.getCfg().isRandomizeMultiFlagColor()
                ? held.get((int) (Math.random() * held.size()))
                : held.get(0);
            Team t = plugin.getTeamManager().getTeam(chosen);
            if (t != null) {
                p.setGlowing(true);
                plugin.getScoreboardManager().setGlowColor(p, t.getColor());
            }
        }
    }
}
