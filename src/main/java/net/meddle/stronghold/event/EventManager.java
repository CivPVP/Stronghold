package net.meddle.stronghold.event;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.config.ConfigManager;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.listeners.FlagItemListener;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EventManager {

    private final Stronghold plugin;
    private final File eventFile;

    private EventPhase phase = EventPhase.IDLE;

    // Real-world timestamps (Unix ms)
    private long countdownStart;
    private long countdownEnd;
    private long endgameDurationMs;
    private long endgameEnd;
    private long pausedRemainingMs; // non-zero only in PAUSED state

    private BukkitTask phaseTask;   // single scheduled task for the next phase transition
    private BukkitTask bossbarTask; // 1-second ticker for bossbar update

    public EventManager(Stronghold plugin) {
        this.plugin    = plugin;
        this.eventFile = new File(plugin.getDataFolder(), "event.yml");
    }

    // ── Startup resume ────────────────────────────────────────────────────────

    /** Called on plugin enable — reads event.yml and resumes any running timer. */
    public void resume() {
        if (!eventFile.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(eventFile);
        String phaseStr = c.getString("phase", "IDLE");
        try { phase = EventPhase.valueOf(phaseStr); }
        catch (Exception e) { phase = EventPhase.IDLE; return; }

        countdownStart      = c.getLong("countdown_start", 0);
        countdownEnd        = c.getLong("countdown_end", 0);
        endgameDurationMs   = c.getLong("endgame_duration_ms", 0);
        endgameEnd          = c.getLong("endgame_end", 0);
        pausedRemainingMs   = c.getLong("paused_remaining_ms", 0);

        long now = System.currentTimeMillis();

        switch (phase) {
            case COUNTDOWN -> {
                long remaining = countdownEnd - now;
                if (remaining <= 0) {
                    // Expired while server was down — deploy on next tick so listeners are ready
                    Bukkit.getScheduler().runTask(plugin, this::deployFlags);
                } else {
                    schedulePhaseTask(remaining, this::deployFlags);
                    startBossbarTick();
                }
            }
            case ACTIVE -> {
                long remaining = endgameEnd - now;
                if (remaining <= 0) {
                    // Expired while server was down — end on next tick so listeners are ready
                    Bukkit.getScheduler().runTask(plugin, this::endEvent);
                } else {
                    schedulePhaseTask(remaining, this::endEvent);
                    startBossbarTick();
                }
            }
            case PAUSED -> {
                // Just show bossbar — don't start ticking
                plugin.getBossBarManager().update();
            }
            default -> {}
        }
    }

    public void shutdown() {
        cancelTasks();
    }

    // ── Start ─────────────────────────────────────────────────────────────────

    public boolean startCountdown(long durationMs) {
        if (phase != EventPhase.IDLE && phase != EventPhase.ENDED) return false;

        long now          = System.currentTimeMillis();
        countdownStart    = now;
        countdownEnd      = now + durationMs;
        endgameDurationMs = ConfigManager.parseDurationMs(plugin.getCfg().getDefaultEndgameDuration());
        endgameEnd        = 0;
        pausedRemainingMs = 0;

        phase = EventPhase.COUNTDOWN;
        persist();

        schedulePhaseTask(durationMs, this::deployFlags);
        startBossbarTick();

        FlagItemListener.broadcast(plugin.getCfg().msg("event_started")
            .replace("{time}", ConfigManager.formatDuration(durationMs)));
        Bukkit.getOnlinePlayers().forEach(p ->
            p.playSound(p.getLocation(), plugin.getCfg().getSoundEventStart(), 1f, 1f));

        plugin.audit("SYSTEM", "Event countdown started — deploy in " + ConfigManager.formatDuration(durationMs));
        return true;
    }

    // ── Pause / Resume ────────────────────────────────────────────────────────

    public boolean pause() {
        if (phase != EventPhase.COUNTDOWN && phase != EventPhase.ACTIVE) return false;

        long now = System.currentTimeMillis();
        pausedRemainingMs = (phase == EventPhase.COUNTDOWN)
            ? countdownEnd - now
            : endgameEnd - now;
        if (pausedRemainingMs < 0) pausedRemainingMs = 0;

        cancelTasks();
        phase = EventPhase.PAUSED;
        persist();

        plugin.getBossBarManager().update();
        FlagItemListener.broadcast(plugin.getCfg().msg("event_paused")
            .replace("{time}", ConfigManager.formatDuration(pausedRemainingMs)));
        return true;
    }

    public boolean resumeFromPause() {
        if (phase != EventPhase.PAUSED) return false;

        long now = System.currentTimeMillis();

        // Figure out which phase we were in by checking endgameEnd
        if (endgameEnd == 0) {
            // Was in COUNTDOWN
            countdownEnd = now + pausedRemainingMs;
            phase = EventPhase.COUNTDOWN;
            schedulePhaseTask(pausedRemainingMs, this::deployFlags);
        } else {
            // Was in ACTIVE
            endgameEnd = now + pausedRemainingMs;
            phase = EventPhase.ACTIVE;
            schedulePhaseTask(pausedRemainingMs, this::endEvent);
        }

        pausedRemainingMs = 0;
        persist();
        startBossbarTick();
        FlagItemListener.broadcast(plugin.getCfg().msg("event_resumed"));
        return true;
    }

    // ── Stop / Reset ──────────────────────────────────────────────────────────

    public void stopEvent() {
        if (phase == EventPhase.IDLE || phase == EventPhase.ENDED) return;
        cancelTasks();
        phase = EventPhase.ENDED;
        persist();
        plugin.getBossBarManager().removeAll();
        announceWinners();
        retireAndReset();
        FlagItemListener.broadcast(plugin.getCfg().msg("event_stopped"));
        plugin.audit("SYSTEM", "Event stopped manually.");
    }

    public void resetEvent() {
        cancelTasks();
        plugin.getBossBarManager().removeAll();

        // Return all flags to their owner vaults
        for (var record : plugin.getFlagManager().getAllRecords()) {
            plugin.getFlagManager().returnFlagToVault(record.getOwnerTeam());
        }

        phase             = EventPhase.IDLE;
        countdownStart    = 0;
        countdownEnd      = 0;
        endgameDurationMs = 0;
        endgameEnd        = 0;
        pausedRemainingMs = 0;

        if (eventFile.exists()) eventFile.delete();
        FlagItemListener.broadcast(plugin.getCfg().msg("event_reset"));
        plugin.audit("SYSTEM", "Event reset.");
    }

    // ── Deploy flags ──────────────────────────────────────────────────────────

    private void deployFlags() {
        phase      = EventPhase.ACTIVE;
        long now   = System.currentTimeMillis();
        endgameEnd = now + endgameDurationMs;
        persist();

        FlagItemListener.broadcast(plugin.getCfg().msg("flags_deployed"));

        int skipped = 0;
        for (Team t : plugin.getTeamManager().getAllTeams()) {
            if (!t.isVaultSet()) {
                plugin.getLogger().warning("[Flag Deploy] Team " + t.getName() + " has no vault — skipped.");
                skipped++;
                plugin.getCfg(); // log to console via audit
                FlagItemListener.broadcast(plugin.getCfg().msg("no_vault").replace("{team}", t.getName()));
                continue;
            }
            plugin.getFlagManager().spawnFlagInVault(t);
        }

        startBossbarTick();
        plugin.getFlagCarrierListener().reapplyAllGlow();
        schedulePhaseTask(endgameEnd - now, this::endEvent);

        Bukkit.getOnlinePlayers().forEach(p ->
            p.playSound(p.getLocation(), plugin.getCfg().getSoundEventStart(), 1f, 1f));
    }

    // ── End event ─────────────────────────────────────────────────────────────

    private void endEvent() {
        cancelTasks();
        phase = EventPhase.ENDED;
        persist();
        plugin.getBossBarManager().removeAll();
        announceWinners();
        retireAndReset();
        plugin.audit("SYSTEM", "Event ended (timer expired).");
    }

    private void retireAndReset() {
        plugin.getFlagManager().retireAllFlags();
        for (Team t : plugin.getTeamManager().getAllTeams()) {
            t.setVaultRaw(null, 0, 0, 0);
            t.setVaultLocked(false);
            plugin.getTeamManager().save(t);
        }
    }

    private void announceWinners() {
        FlagManager fm = plugin.getFlagManager();

        // Compute scores
        Map<Team, Integer> scores = new LinkedHashMap<>();
        for (Team t : plugin.getTeamManager().getAllTeams()) {
            scores.put(t, fm.computeScore(t));
        }

        int max = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Team> winners = scores.entrySet().stream()
            .filter(e -> e.getValue() == max)
            .map(Map.Entry::getKey)
            .toList();

        // Broadcast — team names in their DyeColor
        Component broadcastBody = Component.text("Event ended! Winner(s): ", Msg.LIGHT_BLUE);
        for (int i = 0; i < winners.size(); i++) {
            if (i > 0) broadcastBody = broadcastBody.append(Component.text(", ", Msg.LIGHT_BLUE));
            broadcastBody = broadcastBody.append(Msg.teamName(winners.get(i)));
        }
        broadcastBody = broadcastBody.append(Component.text(" with " + max + " flag(s)!", Msg.LIGHT_BLUE));
        Msg.broadcast(broadcastBody);

        // Title — single winner in team color, tie in white
        Component titleComp;
        if (winners.size() == 1) {
            titleComp = Msg.teamName(winners.get(0))
                .append(Component.text(" wins!", Msg.LIGHT_BLUE));
        } else {
            Component tied = Component.empty();
            for (int i = 0; i < winners.size(); i++) {
                if (i > 0) tied = tied.append(Component.text(", ", Msg.WHITE));
                tied = tied.append(Msg.teamName(winners.get(i)));
            }
            titleComp = Component.text("Tie! ", Msg.WHITE).append(tied);
        }
        Component subtitleComp = Component.text("With " + max + " flag(s) captured", Msg.LIGHT_BLUE);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(titleComp, subtitleComp));
            p.playSound(p.getLocation(), plugin.getCfg().getSoundEventEnd(), 1f, 1f);
        }

        StringJoiner winnerNames = new StringJoiner(", ");
        winners.forEach(t -> winnerNames.add(t.getName()));
        plugin.getLogger().info("[CivPvP] Event ended. Winners: " + winnerNames + " with " + max + " flag(s).");
    }

    // ── Bossbar tick ──────────────────────────────────────────────────────────

    private void startBossbarTick() {
        if (bossbarTask != null && !bossbarTask.isCancelled()) bossbarTask.cancel();
        bossbarTask = Bukkit.getScheduler().runTaskTimer(plugin, () ->
            plugin.getBossBarManager().update(), 0L, 20L);
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private void persist() {
        YamlConfiguration c = new YamlConfiguration();
        c.set("phase",               phase.name());
        c.set("countdown_start",     countdownStart);
        c.set("countdown_end",       countdownEnd);
        c.set("endgame_duration_ms", endgameDurationMs);
        c.set("endgame_end",         endgameEnd);
        c.set("paused_remaining_ms", pausedRemainingMs);
        try { c.save(eventFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save event.yml: " + e.getMessage()); }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void schedulePhaseTask(long delayMs, Runnable task) {
        if (phaseTask != null && !phaseTask.isCancelled()) phaseTask.cancel();
        long ticks = Math.max(1L, delayMs / 50L);
        phaseTask = Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
    }

    private void cancelTasks() {
        if (phaseTask  != null && !phaseTask.isCancelled())  phaseTask.cancel();
        if (bossbarTask != null && !bossbarTask.isCancelled()) bossbarTask.cancel();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public EventPhase getPhase()            { return phase; }
    public long getCountdownEnd()           { return countdownEnd; }
    public long getCountdownStart()         { return countdownStart; }
    public long getEndgameEnd()             { return endgameEnd; }
    public long getEndgameDurationMs()      { return endgameDurationMs; }
    public long getPausedRemainingMs()      { return pausedRemainingMs; }
}
