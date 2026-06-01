package net.meddle.stronghold.config;

import net.meddle.stronghold.Stronghold;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Stronghold plugin;
    private FileConfiguration cfg;

    public ConfigManager(Stronghold plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();
    }

    // ── Teams ──────────────────────────────────────────────────────────────────

    public int getMaxTeams() {
        return cfg.getInt("teams.max_teams", 16);
    }

    // ── Broadcast ──────────────────────────────────────────────────────────────

    public int getCarrierIntervalSeconds() {
        return cfg.getInt("broadcast.carrier_interval_seconds", 30);
    }

    public String getCarrierMessage()      { return color(cfg.getString("broadcast.carrier_message", "")); }
    public String getFlagStolenMessage()   { return color(cfg.getString("broadcast.flag_stolen_message", "")); }
    public String getFlagDroppedMessage()  { return color(cfg.getString("broadcast.flag_dropped_message", "")); }
    public String getFlagReturnedMessage() { return color(cfg.getString("broadcast.flag_returned_message", "")); }

    // ── Bossbar ───────────────────────────────────────────────────────────────

    public String getBossbarCountdownTitle() { return color(cfg.getString("bossbar.countdown_title", "&6Stronghold &7— Flag deployment in: &e{time}")); }
    public String getBossbarActiveTitle()    { return color(cfg.getString("bossbar.active_title",    "&6Stronghold &7— Event ends in: &e{time}")); }
    public String getBossbarPausedTitle()    { return color(cfg.getString("bossbar.paused_title",    "&7[PAUSED] &6Stronghold &7— &e{time} &7remaining")); }

    public BarColor getBossbarColor() {
        try { return BarColor.valueOf(cfg.getString("bossbar.color", "YELLOW")); }
        catch (Exception e) { return BarColor.YELLOW; }
    }

    public BarStyle getBossbarStyle() {
        try { return BarStyle.valueOf(cfg.getString("bossbar.style", "SOLID")); }
        catch (Exception e) { return BarStyle.SOLID; }
    }

    // ── TAB integration ───────────────────────────────────────────────────────

    public boolean isTabPrefixEnabled()     { return cfg.getBoolean("tab.prefix_enabled", true); }
    public boolean isTabNameColorEnabled()  { return cfg.getBoolean("tab.name_color_enabled", true); }

    // ── Glow ──────────────────────────────────────────────────────────────────

    public boolean isGlowEnabled()               { return cfg.getBoolean("glow.enabled", true); }

    // ── Event ─────────────────────────────────────────────────────────────────

    public String getDefaultCountdownDuration()  { return cfg.getString("event.default_countdown_duration", "1d"); }
    public String getDefaultEndgameDuration()    { return cfg.getString("event.default_endgame_duration", "2d"); }
    public boolean isCarrierEffectsActiveOnly()  { return cfg.getBoolean("event.carrier_effects_active_phase_only", true); }

    // ── Protection ────────────────────────────────────────────────────────────

    public boolean isPreventFlagBurn()               { return cfg.getBoolean("protection.prevent_flag_burn", true); }
    public boolean isPreventFlagInNonVaultContainers() { return cfg.getBoolean("protection.prevent_flag_in_non_vault_containers", true); }

    // ── Flags GUI ─────────────────────────────────────────────────────────────

    public String getFlagsGuiTitle()      { return color(cfg.getString("flags_gui.title", "&8&lFlag Status")); }
    public String getInVaultIcon()        { return cfg.getString("flags_gui.in_vault_icon", "GREEN_BANNER"); }
    public String getHeldIcon()           { return cfg.getString("flags_gui.held_icon", "YELLOW_BANNER"); }
    public String getDroppedIcon()        { return cfg.getString("flags_gui.dropped_icon", "RED_BANNER"); }
    public String getFillerMaterial()     { return cfg.getString("flags_gui.filler_material", "BLACK_STAINED_GLASS_PANE"); }

    // ── Sounds ────────────────────────────────────────────────────────────────

    public Sound getSound(String key, Sound fallback) {
        String val = cfg.getString("sounds." + key);
        if (val == null) return fallback;
        try { return Sound.valueOf(val); }
        catch (Exception e) { return fallback; }
    }

    public Sound getSoundFlagStolen()   { return getSound("flag_stolen",    Sound.BLOCK_NOTE_BLOCK_BASS); }
    public Sound getSoundFlagReturned() { return getSound("flag_returned",  Sound.BLOCK_CHEST_OPEN); }
    public Sound getSoundEventStart()   { return getSound("event_start",    Sound.ENTITY_ENDER_DRAGON_GROWL); }
    public Sound getSoundEventEnd()     { return getSound("event_end",      Sound.UI_TOAST_CHALLENGE_COMPLETE); }
    public Sound getSoundWinner()       { return getSound("winner_announce", Sound.ENTITY_PLAYER_LEVELUP); }

    // ── Event End ─────────────────────────────────────────────────────────────

    public String getEventEndTitleWinner()    { return color(cfg.getString("event_end.title_winner", "&6{team} wins!")); }
    public String getEventEndTitleTie()       { return color(cfg.getString("event_end.title_tie",    "&eTie! Multiple teams win!")); }
    public String getEventEndSubtitle()       { return color(cfg.getString("event_end.subtitle",     "&7With {count} flag(s) captured")); }
    public String getEventEndBroadcast()      { return color(cfg.getString("event_end.broadcast_message", "&6&lStronghold has ended!")); }

    // ── Messages ──────────────────────────────────────────────────────────────

    public String msg(String key) {
        String val = cfg.getString("messages." + key, "&cMissing message: " + key);
        return color(val);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static String color(String s) {
        if (s == null) return "";
        return s.replace("&", "§");
    }

    /** Parse a duration string like "1d12h30m" into milliseconds. */
    public static long parseDurationMs(String input) {
        if (input == null || input.isBlank()) return 0;
        long total = 0;
        StringBuilder num = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isDigit(c)) {
                num.append(c);
            } else {
                long n = num.isEmpty() ? 0 : Long.parseLong(num.toString());
                num.setLength(0);
                switch (c) {
                    case 'd' -> total += n * 86_400_000L;
                    case 'h' -> total += n * 3_600_000L;
                    case 'm' -> total += n * 60_000L;
                    case 's' -> total += n * 1_000L;
                }
            }
        }
        return total;
    }

    /** Format milliseconds as "Xd Xh Xm Xs", omitting leading zeroes. */
    public static String formatDuration(long ms) {
        if (ms <= 0) return "0s";
        long s = ms / 1000;
        long days    = s / 86400; s %= 86400;
        long hours   = s / 3600;  s %= 3600;
        long minutes = s / 60;
        long seconds = s % 60;
        StringBuilder sb = new StringBuilder();
        if (days    > 0) sb.append(days).append("d ");
        if (hours   > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
