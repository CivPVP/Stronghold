package net.meddle.stronghold.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.config.ConfigManager;
import net.meddle.stronghold.event.EventPhase;
import org.bukkit.entity.Player;

public class BossBarManager {

    private final Stronghold plugin;
    private BossBar bossBar;

    public BossBarManager(Stronghold plugin) {
        this.plugin = plugin;
    }

    // ── Player management ─────────────────────────────────────────────────────

    public void addPlayer(Player p) {
        if (bossBar != null) p.showBossBar(bossBar);
    }

    public void removeAll() {
        if (bossBar == null) return;
        for (var p : org.bukkit.Bukkit.getOnlinePlayers()) p.hideBossBar(bossBar);
        bossBar = null;
    }

    // ── Update (called every second) ──────────────────────────────────────────

    public void update() {
        EventPhase phase = plugin.getEventManager().getPhase();

        if (phase == EventPhase.IDLE || phase == EventPhase.ENDED) {
            removeAll();
            return;
        }

        String title;
        float  progress;

        long now = System.currentTimeMillis();

        switch (phase) {
            case COUNTDOWN -> {
                long end   = plugin.getEventManager().getCountdownEnd();
                long start = plugin.getEventManager().getCountdownStart();
                long remaining = Math.max(0, end - now);
                long total     = end - start;
                progress = total <= 0 ? 0f : (float) remaining / total;
                title = plugin.getCfg().getBossbarCountdownTitle()
                    .replace("{time}", ConfigManager.formatDuration(remaining));
            }
            case PAUSED -> {
                long remaining = plugin.getEventManager().getPausedRemainingMs();
                progress = 0f; // static bar when paused
                title = plugin.getCfg().getBossbarPausedTitle()
                    .replace("{time}", ConfigManager.formatDuration(remaining));
            }
            case ACTIVE -> {
                long end       = plugin.getEventManager().getEndgameEnd();
                long remaining = Math.max(0, end - now);
                long total     = plugin.getEventManager().getEndgameDurationMs();
                progress = total <= 0 ? 0f : (float) remaining / total;
                title = plugin.getCfg().getBossbarActiveTitle()
                    .replace("{time}", ConfigManager.formatDuration(remaining));
            }
            default -> { return; }
        }

        progress = Math.max(0f, Math.min(1f, progress));

        if (bossBar == null) {
            bossBar = BossBar.bossBar(
                legacy(title),
                progress,
                toAdventureColor(plugin.getCfg().getBossbarColor()),
                toAdventureStyle(plugin.getCfg().getBossbarStyle())
            );
            for (var p : org.bukkit.Bukkit.getOnlinePlayers()) p.showBossBar(bossBar);
        } else {
            bossBar.name(legacy(title));
            bossBar.progress(progress);
        }
    }

    // ── Converters ────────────────────────────────────────────────────────────

    private static Component legacy(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }

    private static BossBar.Color toAdventureColor(org.bukkit.boss.BarColor c) {
        return switch (c) {
            case PINK   -> BossBar.Color.PINK;
            case BLUE   -> BossBar.Color.BLUE;
            case RED    -> BossBar.Color.RED;
            case GREEN  -> BossBar.Color.GREEN;
            case YELLOW -> BossBar.Color.YELLOW;
            case PURPLE -> BossBar.Color.PURPLE;
            case WHITE  -> BossBar.Color.WHITE;
        };
    }

    private static BossBar.Overlay toAdventureStyle(org.bukkit.boss.BarStyle s) {
        return switch (s) {
            case SOLID            -> BossBar.Overlay.PROGRESS;
            case SEGMENTED_6      -> BossBar.Overlay.NOTCHED_6;
            case SEGMENTED_10     -> BossBar.Overlay.NOTCHED_10;
            case SEGMENTED_12     -> BossBar.Overlay.NOTCHED_12;
            case SEGMENTED_20     -> BossBar.Overlay.NOTCHED_20;
        };
    }
}
