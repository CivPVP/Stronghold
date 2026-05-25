package net.meddle.stronghold.team;

import net.meddle.stronghold.Stronghold;
import org.bukkit.DyeColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {

    private final Stronghold plugin;
    private final File teamsDir;
    private final Map<String, Team> teams = new ConcurrentHashMap<>();

    public TeamManager(Stronghold plugin) {
        this.plugin   = plugin;
        this.teamsDir = new File(plugin.getDataFolder(), "teams");
        teamsDir.mkdirs();
    }

    // ── Load / Save ───────────────────────────────────────────────────────────

    public void loadAll() {
        teams.clear();
        File[] files = teamsDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            try {
                Team t = load(f);
                if (t != null) teams.put(t.getName(), t);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load team file " + f.getName() + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + teams.size() + " team(s).");
    }

    private Team load(File f) {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        String name  = c.getString("name");
        String color = c.getString("color");
        if (name == null || color == null) return null;

        DyeColor dye;
        try { dye = DyeColor.valueOf(color); }
        catch (Exception e) { return null; }

        Team t = new Team(name, dye);

        String cap = c.getString("captain");
        if (cap != null && !cap.isEmpty()) {
            try { t.setCaptain(UUID.fromString(cap)); } catch (Exception ignored) {}
        }

        List<String> members = c.getStringList("members");
        for (String m : members) {
            try { t.addMember(UUID.fromString(m)); } catch (Exception ignored) {}
        }

        t.setVaultRaw(
            c.getString("vault.world"),
            c.getInt("vault.x", 0),
            c.getInt("vault.y", 0),
            c.getInt("vault.z", 0)
        );
        t.setVaultLocked(c.getBoolean("vault.locked", false));

        return t;
    }

    public void save(Team t) {
        File f = fileFor(t.getName());
        YamlConfiguration c = new YamlConfiguration();
        c.set("name",   t.getName());
        c.set("color",  t.getColor().name());
        c.set("captain", t.getCaptain() != null ? t.getCaptain().toString() : "");

        List<String> members = new ArrayList<>();
        for (UUID u : t.getMembers()) members.add(u.toString());
        c.set("members", members);

        c.set("vault.world",  t.getVaultWorld());
        c.set("vault.x",      t.getVaultX());
        c.set("vault.y",      t.getVaultY());
        c.set("vault.z",      t.getVaultZ());
        c.set("vault.locked", t.isVaultLocked());

        try { c.save(f); }
        catch (IOException e) {
            plugin.getLogger().severe("Failed to save team " + t.getName() + ": " + e.getMessage());
        }
    }

    private File fileFor(String name) {
        return new File(teamsDir, name + ".yml");
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public Team createTeam(String name, DyeColor color) {
        Team t = new Team(name, color);
        teams.put(name, t);
        save(t);
        return t;
    }

    public boolean deleteTeam(String name) {
        Team t = teams.remove(name);
        if (t == null) return false;
        fileFor(name).delete();
        return true;
    }

    public boolean renameTeam(String oldName, String newName) {
        Team t = teams.remove(oldName);
        if (t == null) return false;
        fileFor(oldName).delete();
        t.setName(newName);
        teams.put(newName, t);
        save(t);
        return true;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Team getTeam(String name)                    { return teams.get(name); }
    public Collection<Team> getAllTeams()               { return Collections.unmodifiableCollection(teams.values()); }
    public boolean exists(String name)                  { return teams.containsKey(name); }

    public Team getTeamOf(UUID playerUUID) {
        for (Team t : teams.values()) {
            if (t.hasMember(playerUUID)) return t;
        }
        return null;
    }

    public boolean isCaptainOf(UUID playerUUID, Team team) {
        return team.isCaptain(playerUUID);
    }

    /** Returns the team whose vault is at the given block coordinates, or null. */
    public Team getTeamByVault(String world, int x, int y, int z) {
        for (Team t : teams.values()) {
            if (t.isVaultAt(world, x, y, z)) return t;
        }
        return null;
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    /** ASCII letters, digits, underscores only — also rejects blank. */
    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) return false;
        return name.matches("[A-Za-z0-9_]+");
    }

    public int count() { return teams.size(); }
}
