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

    // Regular event timestamps
    private long countdownStart;
    private long countdownEnd;
    private long endgameDurationMs;
    private long endgameEnd;
    private long pausedRemainingMs;

    // Tie-break timestamps
    private long tiebreakCountdownEnd;
    private long tiebreakCountdownDurationMs;
    private long tiebreakActiveDurationMs;
    private long tiebreakActiveEnd;

    private BukkitTask phaseTask;
    private BukkitTask bossbarTask;

    public EventManager(Stronghold plugin) {
        this.plugin    = plugin;
        this.eventFile = new File(plugin.getDataFolder(), "event.yml");
    }

    // ── Startup resume ────────────────────────────────────────────────────────

    public void resume() {
        if (!eventFile.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(eventFile);
        String phaseStr = c.getString("phase", "IDLE");
        try { phase = EventPhase.valueOf(phaseStr); }
        catch (Exception e) { phase = EventPhase.IDLE; return; }

        countdownStart             = c.getLong("countdown_start", 0);
        countdownEnd               = c.getLong("countdown_end", 0);
        endgameDurationMs          = c.getLong("endgame_duration_ms", 0);
        endgameEnd                 = c.getLong("endgame_end", 0);
        pausedRemainingMs          = c.getLong("paused_remaining_ms", 0);
        tiebreakCountdownEnd       = c.getLong("tiebreak_countdown_end", 0);
        tiebreakCountdownDurationMs= c.getLong("tiebreak_countdown_duration_ms", 0);
        tiebreakActiveDurationMs   = c.getLong("tiebreak_active_duration_ms", 0);
        tiebreakActiveEnd          = c.getLong("tiebreak_active_end", 0);

        // Restore tied teams
        String tiedStr = c.getString("tied_teams", "");
        if (!tiedStr.isBlank()) {
            Set<String> tied = new HashSet<>(Arrays.asList(tiedStr.split(",")));
            plugin.getTieBreakManager().setTiedTeams(tied);
        }

        long now = System.currentTimeMillis();
        switch (phase) {
            case COUNTDOWN -> {
                long remaining = countdownEnd - now;
                if (remaining <= 0) Bukkit.getScheduler().runTask(plugin, this::deployFlags);
                else { schedulePhaseTask(remaining, this::deployFlags); startBossbarTick(); }
            }
            case ACTIVE -> {
                long remaining = endgameEnd - now;
                if (remaining <= 0) Bukkit.getScheduler().runTask(plugin, this::endEvent);
                else { schedulePhaseTask(remaining, this::endEvent); startBossbarTick(); }
            }
            case PAUSED -> plugin.getBossBarManager().update();
            case TIE_BREAK_COUNTDOWN -> {
                long remaining = tiebreakCountdownEnd - now;
                if (remaining <= 0) Bukkit.getScheduler().runTask(plugin, this::deployTieBreakFlags);
                else { schedulePhaseTask(remaining, this::deployTieBreakFlags); startBossbarTick(); }
            }
            case TIE_BREAK_ACTIVE -> {
                long remaining = tiebreakActiveEnd - now;
                if (remaining <= 0) Bukkit.getScheduler().runTask(plugin, this::endTieBreak);
                else { schedulePhaseTask(remaining, this::endTieBreak); startBossbarTick(); }
            }
            default -> {}
        }
    }

    public void shutdown() { cancelTasks(); }

    // ── Start ─────────────────────────────────────────────────────────────────

    public boolean startCountdown(long durationMs) {
        if (phase != EventPhase.IDLE && phase != EventPhase.ENDED) return false;

        long now          = System.currentTimeMillis();
        countdownStart    = now;
        countdownEnd      = now + durationMs;
        endgameDurationMs = ConfigManager.parseDurationMs(plugin.getCfg().getDefaultEndgameDuration());
        endgameEnd        = 0;
        pausedRemainingMs = 0;
        tiebreakCountdownEnd = tiebreakCountdownDurationMs = tiebreakActiveDurationMs = tiebreakActiveEnd = 0;

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
        pausedRemainingMs = (phase == EventPhase.COUNTDOWN) ? countdownEnd - now : endgameEnd - now;
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
        if (endgameEnd == 0) {
            countdownEnd = now + pausedRemainingMs;
            phase = EventPhase.COUNTDOWN;
            schedulePhaseTask(pausedRemainingMs, this::deployFlags);
        } else {
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

        boolean inTieBreak = (phase == EventPhase.TIE_BREAK_COUNTDOWN
                           || phase == EventPhase.TIE_BREAK_ACTIVE);

        phase = EventPhase.ENDED;
        persist();
        plugin.getBossBarManager().removeAll();

        if (inTieBreak) announceTieBreakWinners();
        else announceWinnersOrStartTieBreak(true); // force end even if tied

        plugin.getTieBreakManager().retireAllFlags();
        plugin.getTieBreakManager().clearTiedTeams();
        retireAndReset();
        FlagItemListener.broadcast(plugin.getCfg().msg("event_stopped"));
        plugin.audit("SYSTEM", "Event stopped manually.");
    }

    public void resetEvent() {
        cancelTasks();
        plugin.getBossBarManager().removeAll();

        for (var record : plugin.getFlagManager().getAllRecords()) {
            plugin.getFlagManager().returnFlagToVault(record.getOwnerTeam());
        }
        plugin.getTieBreakManager().removeAllFlags();
        plugin.getTieBreakManager().clearTiedTeams();

        phase = EventPhase.IDLE;
        countdownStart = countdownEnd = endgameDurationMs = endgameEnd = pausedRemainingMs = 0;
        tiebreakCountdownEnd = tiebreakCountdownDurationMs = tiebreakActiveDurationMs = tiebreakActiveEnd = 0;

        if (eventFile.exists()) eventFile.delete();
        FlagItemListener.broadcast(plugin.getCfg().msg("event_reset"));
        plugin.audit("SYSTEM", "Event reset.");
    }

    // ── Deploy regular flags ──────────────────────────────────────────────────

    private void deployFlags() {
        phase      = EventPhase.ACTIVE;
        long now   = System.currentTimeMillis();
        endgameEnd = now + endgameDurationMs;
        persist();

        FlagItemListener.broadcast(plugin.getCfg().msg("flags_deployed"));

        for (Team t : plugin.getTeamManager().getAllTeams()) {
            if (!t.isVaultSet()) {
                plugin.getLogger().warning("[Flag Deploy] Team " + t.getName() + " has no vault — skipped.");
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

    // ── End regular event ─────────────────────────────────────────────────────

    private void endEvent() {
        cancelTasks();
        plugin.getBossBarManager().removeAll();
        announceWinnersOrStartTieBreak(false);
        plugin.audit("SYSTEM", "Event ended (timer expired).");
    }

    /**
     * Computes winners. If there is a tie AND tie-break vaults exist AND forceEnd is false,
     * starts the tie-break phase instead of ending. Otherwise announces the result and ends.
     */
    private void announceWinnersOrStartTieBreak(boolean forceEnd) {
        FlagManager fm = plugin.getFlagManager();

        Map<Team, Integer> scores = new LinkedHashMap<>();
        for (Team t : plugin.getTeamManager().getAllTeams()) {
            scores.put(t, fm.computeScore(t));
        }
        int max = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Team> winners = scores.entrySet().stream()
            .filter(e -> e.getValue() == max).map(Map.Entry::getKey).toList();

        if (winners.size() > 1 && !forceEnd && plugin.getTieBreakManager().hasVaults()) {
            // Start tie-break
            Set<String> tiedNames = new LinkedHashSet<>();
            winners.forEach(t -> tiedNames.add(t.getName()));
            plugin.getTieBreakManager().setTiedTeams(tiedNames);

            Component tiedList = Component.empty();
            for (int i = 0; i < winners.size(); i++) {
                if (i > 0) tiedList = tiedList.append(Component.text(", ", Msg.WHITE));
                tiedList = tiedList.append(Msg.teamName(winners.get(i)));
            }
            Msg.broadcast(Component.text("Tie between ", Msg.WHITE)
                .append(tiedList)
                .append(Component.text("! Tie-breaker starting...", Msg.LIGHT_BLUE)));

            startTieBreakCountdown();
        } else {
            // Announce winner(s) and end
            phase = EventPhase.ENDED;
            persist();
            broadcastWinners(winners, max);
            retireAndReset();
        }
    }

    private void broadcastWinners(List<Team> winners, int flagCount) {
        Component body = Component.text("Event ended! Winner(s): ", Msg.LIGHT_BLUE);
        for (int i = 0; i < winners.size(); i++) {
            if (i > 0) body = body.append(Component.text(", ", Msg.LIGHT_BLUE));
            body = body.append(Msg.teamName(winners.get(i)));
        }
        body = body.append(Component.text(" with " + flagCount + " flag(s)!", Msg.LIGHT_BLUE));
        Msg.broadcast(body);

        Component title, subtitle;
        subtitle = Component.text("With " + flagCount + " flag(s) captured", Msg.LIGHT_BLUE);
        if (winners.size() == 1) {
            title = Msg.teamName(winners.get(0)).append(Component.text(" wins!", Msg.LIGHT_BLUE));
        } else {
            Component tied = Component.empty();
            for (int i = 0; i < winners.size(); i++) {
                if (i > 0) tied = tied.append(Component.text(", ", Msg.WHITE));
                tied = tied.append(Msg.teamName(winners.get(i)));
            }
            title = Component.text("Tie! ", Msg.WHITE).append(tied);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
            p.playSound(p.getLocation(), plugin.getCfg().getSoundEventEnd(), 1f, 1f);
        }

        StringJoiner names = new StringJoiner(", ");
        winners.forEach(t -> names.add(t.getName()));
        plugin.getLogger().info("[CivPvP] Event ended. Winners: " + names + " with " + flagCount + " flag(s).");
    }

    // ── Tie-break countdown ───────────────────────────────────────────────────

    private void startTieBreakCountdown() {
        tiebreakCountdownDurationMs = ConfigManager.parseDurationMs(
            plugin.getCfg().getDefaultTiebreakCountdownDuration());
        tiebreakActiveDurationMs    = ConfigManager.parseDurationMs(
            plugin.getCfg().getDefaultTiebreakActiveDuration());
        long now = System.currentTimeMillis();
        tiebreakCountdownEnd = now + tiebreakCountdownDurationMs;
        tiebreakActiveEnd    = 0;

        phase = EventPhase.TIE_BREAK_COUNTDOWN;
        persist();

        Msg.broadcast(Component.text("Tie-breaking flags deploy in: ", Msg.LIGHT_BLUE)
            .append(Component.text(ConfigManager.formatDuration(tiebreakCountdownDurationMs), Msg.WHITE)));

        startBossbarTick();
        schedulePhaseTask(tiebreakCountdownDurationMs, this::deployTieBreakFlags);
    }

    // ── Deploy tie-break flags ────────────────────────────────────────────────

    private void deployTieBreakFlags() {
        long now = System.currentTimeMillis();
        tiebreakActiveEnd = now + tiebreakActiveDurationMs;
        phase = EventPhase.TIE_BREAK_ACTIVE;
        persist();

        plugin.getTieBreakManager().spawnFlags();

        Msg.broadcast(Component.text("Tie-breaking flags have been deployed! ", Msg.LIGHT_BLUE)
            .append(Component.text("Only tied teams may pick them up!", Msg.WHITE)));

        startBossbarTick();
        schedulePhaseTask(tiebreakActiveDurationMs, this::endTieBreak);

        Bukkit.getOnlinePlayers().forEach(p ->
            p.playSound(p.getLocation(), plugin.getCfg().getSoundEventStart(), 1f, 1f));
    }

    // ── End tie-break ─────────────────────────────────────────────────────────

    private void endTieBreak() {
        cancelTasks();
        plugin.getBossBarManager().removeAll();
        announceTieBreakWinners();
        plugin.getTieBreakManager().retireAllFlags();
        plugin.getTieBreakManager().clearTiedTeams();
        phase = EventPhase.ENDED;
        persist();
        retireAndReset();
        plugin.audit("SYSTEM", "Tie-break ended (timer expired).");
    }

    private void announceTieBreakWinners() {
        var tbm = plugin.getTieBreakManager();
        var fm  = plugin.getFlagManager();
        Set<String> tied = tbm.getTiedTeams();

        // Final count: regular flags + tie-breaking flags for each tied team
        Map<Team, Integer> scores = new LinkedHashMap<>();
        for (String name : tied) {
            Team t = plugin.getTeamManager().getTeam(name);
            if (t != null) scores.put(t, fm.computeScore(t) + tbm.computeScore(t));
        }

        int max = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Team> winners = scores.entrySet().stream()
            .filter(e -> e.getValue() == max).map(Map.Entry::getKey).toList();

        Component body = Component.text("Tie-breaker ended! Winner(s): ", Msg.LIGHT_BLUE);
        for (int i = 0; i < winners.size(); i++) {
            if (i > 0) body = body.append(Component.text(", ", Msg.LIGHT_BLUE));
            body = body.append(Msg.teamName(winners.get(i)));
        }
        body = body.append(Component.text(" with " + max + " total flag(s)!", Msg.LIGHT_BLUE));
        Msg.broadcast(body);

        Component subtitle = Component.text("With " + max + " total flag(s)", Msg.LIGHT_BLUE);
        Component title;
        if (winners.size() == 1) {
            title = Msg.teamName(winners.get(0)).append(Component.text(" wins!", Msg.LIGHT_BLUE));
        } else {
            Component tied2 = Component.empty();
            for (int i = 0; i < winners.size(); i++) {
                if (i > 0) tied2 = tied2.append(Component.text(", ", Msg.WHITE));
                tied2 = tied2.append(Msg.teamName(winners.get(i)));
            }
            title = Component.text("Still tied! ", Msg.WHITE).append(tied2);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
            p.playSound(p.getLocation(), plugin.getCfg().getSoundWinner(), 1f, 1f);
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private void retireAndReset() {
        plugin.getFlagManager().retireAllFlags();
        for (Team t : plugin.getTeamManager().getAllTeams()) {
            t.setVaultRaw(null, 0, 0, 0);
            t.setVaultLocked(false);
            plugin.getTeamManager().save(t);
        }
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
        c.set("tiebreak_countdown_end",          tiebreakCountdownEnd);
        c.set("tiebreak_countdown_duration_ms",  tiebreakCountdownDurationMs);
        c.set("tiebreak_active_duration_ms",     tiebreakActiveDurationMs);
        c.set("tiebreak_active_end",             tiebreakActiveEnd);

        Set<String> tied = plugin.getTieBreakManager().getTiedTeams();
        c.set("tied_teams", tied.isEmpty() ? "" : String.join(",", tied));

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
        if (phaseTask   != null && !phaseTask.isCancelled())   phaseTask.cancel();
        if (bossbarTask != null && !bossbarTask.isCancelled()) bossbarTask.cancel();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public EventPhase getPhase()                   { return phase; }
    public long getCountdownEnd()                  { return countdownEnd; }
    public long getCountdownStart()                { return countdownStart; }
    public long getEndgameEnd()                    { return endgameEnd; }
    public long getEndgameDurationMs()             { return endgameDurationMs; }
    public long getPausedRemainingMs()             { return pausedRemainingMs; }
    public long getTiebreakCountdownEnd()          { return tiebreakCountdownEnd; }
    public long getTiebreakCountdownDurationMs()   { return tiebreakCountdownDurationMs; }
    public long getTiebreakActiveEnd()             { return tiebreakActiveEnd; }
    public long getTiebreakActiveDurationMs()      { return tiebreakActiveDurationMs; }
}
