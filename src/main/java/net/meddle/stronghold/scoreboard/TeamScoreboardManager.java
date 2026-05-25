package net.meddle.stronghold.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class TeamScoreboardManager {

    private final Stronghold plugin;
    private final Scoreboard scoreboard;

    private static final boolean TAB_AVAILABLE;

    static {
        boolean found = false;
        try { Class.forName("me.neznamy.tab.api.TabAPI"); found = true; }
        catch (ClassNotFoundException ignored) {}
        TAB_AVAILABLE = found;
    }

    public TeamScoreboardManager(Stronghold plugin) {
        this.plugin     = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    // ── Apply / Remove ────────────────────────────────────────────────────────

    public void applyAll() {
        for (Team t : plugin.getTeamManager().getAllTeams()) {
            ensureScoreboardTeam(t);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            Team t = plugin.getTeamManager().getTeamOf(p.getUniqueId());
            if (t != null) { addPlayer(p, t); applyTabColor(p, t); }
            else p.setScoreboard(scoreboard);
        }
    }

    public void ensureScoreboardTeam(Team t) {
        String key = sbKey(t.getName());
        org.bukkit.scoreboard.Team sb = scoreboard.getTeam(key);
        if (sb == null) sb = scoreboard.registerNewTeam(key);
        sb.color(dyeToNamedColor(t.getColor()));
        sb.displayName(Component.text(t.getName()));
    }

    public void removeScoreboardTeam(Team t) {
        org.bukkit.scoreboard.Team sb = scoreboard.getTeam(sbKey(t.getName()));
        if (sb != null) sb.unregister();
    }

    public void addPlayer(Player p, Team t) {
        p.setScoreboard(scoreboard);
        org.bukkit.scoreboard.Team sb = scoreboard.getTeam(sbKey(t.getName()));
        if (sb == null) { ensureScoreboardTeam(t); sb = scoreboard.getTeam(sbKey(t.getName())); }
        if (sb != null) sb.addEntry(p.getName());
        applyTabColor(p, t);
    }

    public void removePlayer(Player p) {
        for (org.bukkit.scoreboard.Team sb : scoreboard.getTeams()) {
            if (sb.hasEntry(p.getName())) sb.removeEntry(p.getName());
        }
        clearTabColor(p);
    }

    public void removePlayerFromTeam(Player p, Team t) {
        org.bukkit.scoreboard.Team sb = scoreboard.getTeam(sbKey(t.getName()));
        if (sb != null) sb.removeEntry(p.getName());
        clearTabColor(p);
    }

    // ── TAB plugin integration ────────────────────────────────────────────────

    public void applyTabColor(Player p, Team t) {
        if (!TAB_AVAILABLE) return;
        try {
            me.neznamy.tab.api.TabAPI tab = me.neznamy.tab.api.TabAPI.getInstance();
            if (tab == null) return;
            me.neznamy.tab.api.TabPlayer tp = tab.getPlayer(p.getUniqueId());
            if (tp == null || !tp.isLoaded()) return;

            String color = Msg.dyeToSectionCode(t.getColor());
            boolean prefixOn    = plugin.getCfg().isTabPrefixEnabled();
            boolean nameColorOn = plugin.getCfg().isTabNameColorEnabled();

            // Prefix: "[TeamName] §color" — trailing color so the name inherits it when name color is on
            String prefix = prefixOn
                ? color + "[" + t.getName() + "] " + (nameColorOn ? color : "§r")
                : (nameColorOn ? color : null);

            me.neznamy.tab.api.nametag.NameTagManager ntm = tab.getNameTagManager();
            if (ntm != null) ntm.setPrefix(tp, prefix);

            me.neznamy.tab.api.tablist.TabListFormatManager tlfm = tab.getTabListFormatManager();
            if (tlfm != null) {
                tlfm.setPrefix(tp, prefix);
                tlfm.setName(tp, nameColorOn ? color + p.getName() : null);
            }
        } catch (Exception ignored) {}
    }

    public void clearTabColor(Player p) {
        if (!TAB_AVAILABLE) return;
        try {
            me.neznamy.tab.api.TabAPI tab = me.neznamy.tab.api.TabAPI.getInstance();
            if (tab == null) return;
            me.neznamy.tab.api.TabPlayer tp = tab.getPlayer(p.getUniqueId());
            if (tp == null) return;
            me.neznamy.tab.api.nametag.NameTagManager ntm = tab.getNameTagManager();
            if (ntm != null) ntm.setPrefix(tp, null);
            me.neznamy.tab.api.tablist.TabListFormatManager tlfm = tab.getTabListFormatManager();
            if (tlfm != null) { tlfm.setPrefix(tp, null); tlfm.setName(tp, null); }
        } catch (Exception ignored) {}
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static String sbKey(String teamName) {
        String key = "sh_" + teamName;
        return key.length() > 16 ? key.substring(0, 16) : key;
    }

    public static NamedTextColor dyeToNamedColor(DyeColor dye) {
        return switch (dye) {
            case WHITE      -> NamedTextColor.WHITE;
            case ORANGE     -> NamedTextColor.GOLD;
            case MAGENTA    -> NamedTextColor.LIGHT_PURPLE;
            case LIGHT_BLUE -> NamedTextColor.AQUA;
            case YELLOW     -> NamedTextColor.YELLOW;
            case LIME       -> NamedTextColor.GREEN;
            case PINK       -> NamedTextColor.LIGHT_PURPLE;
            case GRAY       -> NamedTextColor.DARK_GRAY;
            case LIGHT_GRAY -> NamedTextColor.GRAY;
            case CYAN       -> NamedTextColor.DARK_AQUA;
            case PURPLE     -> NamedTextColor.DARK_PURPLE;
            case BLUE       -> NamedTextColor.BLUE;
            case BROWN      -> NamedTextColor.DARK_RED;
            case GREEN      -> NamedTextColor.DARK_GREEN;
            case RED        -> NamedTextColor.RED;
            case BLACK      -> NamedTextColor.BLACK;
        };
    }

    public Scoreboard getScoreboard() { return scoreboard; }
}
