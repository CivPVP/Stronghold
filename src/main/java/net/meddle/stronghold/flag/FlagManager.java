package net.meddle.stronghold.flag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.scoreboard.TeamScoreboardManager;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlagManager {

    public static final NamespacedKey KEY_OWNER = new NamespacedKey("stronghold", "flag_owner");
    public static final NamespacedKey KEY_UUID  = new NamespacedKey("stronghold", "flag_uuid");

    private final Stronghold plugin;
    private final File flagsFile;
    private final Map<String, FlagRecord> records = new ConcurrentHashMap<>();

    public FlagManager(Stronghold plugin) {
        this.plugin    = plugin;
        this.flagsFile = new File(plugin.getDataFolder(), "flags.yml");
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void loadAll() {
        records.clear();
        if (!flagsFile.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(flagsFile);
        for (String team : c.getKeys(false)) {
            try {
                UUID uuid  = UUID.fromString(Objects.requireNonNull(c.getString(team + ".uuid")));
                FlagRecord r = new FlagRecord(team, uuid);

                String stateStr = c.getString(team + ".state", "IN_VAULT");
                r.setState(FlagState.valueOf(stateStr));

                String holder = c.getString(team + ".holderUUID");
                if (holder != null && !holder.isEmpty()) {
                    r.setHeld(UUID.fromString(holder), c.getString(team + ".holderName", ""));
                    r.setState(FlagState.HELD);
                }

                String dw = c.getString(team + ".droppedWorld");
                if (dw != null && !dw.isEmpty()) {
                    String deuStr = c.getString(team + ".droppedEntityUUID");
                    UUID deu = (deuStr != null && !deuStr.isEmpty()) ? UUID.fromString(deuStr) : null;
                    r.setDropped(dw, c.getDouble(team + ".droppedX"), c.getDouble(team + ".droppedY"),
                                 c.getDouble(team + ".droppedZ"), deu);
                }

                String lv = c.getString(team + ".lastVaultTeam");
                if (lv != null) r.setLastVaultTeam(lv);

                records.put(team, r);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not load flag record for team " + team + ": " + e.getMessage());
            }
        }

        // Leave HELD records as-is for offline players — the physical item is still in their
        // saved inventory. Mutating to IN_VAULT here would desync state vs. the actual item.
        // Reconciliation happens on player join via PlayerSessionListener.
    }

    /** Synchronous save — use only at shutdown or initial load. */
    public void saveAll() {
        try { buildSnapshot().save(flagsFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save flags.yml: " + e.getMessage()); }
    }

    /** Async atomic save — used on every state transition to avoid blocking the main thread. */
    private void save(FlagRecord ignored) {
        YamlConfiguration snapshot = buildSnapshot();
        File tmp = new File(flagsFile.getParent(), "flags.tmp.yml");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                snapshot.save(tmp);
                Files.move(tmp.toPath(), flagsFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save flags.yml: " + e.getMessage());
            }
        });
    }

    private YamlConfiguration buildSnapshot() {
        YamlConfiguration c = new YamlConfiguration();
        for (FlagRecord r : records.values()) {
            String t = r.getOwnerTeam();
            c.set(t + ".uuid",              r.getFlagUuid().toString());
            c.set(t + ".state",             r.getState().name());
            c.set(t + ".holderUUID",        r.getHolderUUID() != null ? r.getHolderUUID().toString() : "");
            c.set(t + ".holderName",        r.getHolderName() != null ? r.getHolderName() : "");
            c.set(t + ".droppedWorld",      r.getDroppedWorld() != null ? r.getDroppedWorld() : "");
            c.set(t + ".droppedX",          r.getDroppedX());
            c.set(t + ".droppedY",          r.getDroppedY());
            c.set(t + ".droppedZ",          r.getDroppedZ());
            c.set(t + ".droppedEntityUUID", r.getDroppedEntityUUID() != null ? r.getDroppedEntityUUID().toString() : "");
            c.set(t + ".lastVaultTeam",     r.getLastVaultTeam());
        }
        return c;
    }

    // ── Flag creation ─────────────────────────────────────────────────────────

    public ItemStack createFlagItem(Team team) {
        Material mat = dyeToMaterial(team.getColor());
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(flagName(team));
        meta.getPersistentDataContainer().set(KEY_OWNER, PersistentDataType.STRING, team.getName());
        UUID uid = records.containsKey(team.getName())
            ? records.get(team.getName()).getFlagUuid()
            : UUID.randomUUID();
        meta.getPersistentDataContainer().set(KEY_UUID, PersistentDataType.STRING, uid.toString());
        meta.setUnbreakable(true);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        return item;
    }

    /** Creates a brand-new flag (new UUID). Use for /resetflag. */
    public ItemStack createFreshFlagItem(Team team) {
        Material mat = dyeToMaterial(team.getColor());
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(flagName(team));
        UUID uid = UUID.randomUUID();
        meta.getPersistentDataContainer().set(KEY_OWNER, PersistentDataType.STRING, team.getName());
        meta.getPersistentDataContainer().set(KEY_UUID,  PersistentDataType.STRING, uid.toString());
        meta.setUnbreakable(true);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        return item;
    }

    /** Bold, team-colored display name for the flag item. */
    private static Component flagName(Team team) {
        return Component.text(team.getName() + "'s Flag")
            .color(TeamScoreboardManager.dyeToNamedColor(team.getColor()))
            .decorate(TextDecoration.BOLD);
    }

    // ── Spawning in world ─────────────────────────────────────────────────────

    /** Spawns a flag into a team's vault barrel. Creates FlagRecord if not present. */
    public void spawnFlagInVault(Team team) {
        Location loc = team.getVaultLocation();
        if (loc == null) {
            plugin.getLogger().warning("No vault for team " + team.getName() + " — flag not spawned.");
            return;
        }
        Block b = loc.getBlock();
        if (b.getType() != Material.BARREL) {
            plugin.getLogger().warning("Vault for " + team.getName() + " is not a barrel — placing barrel first.");
            b.setType(Material.BARREL);
        }

        ItemStack flag = createFlagItem(team);
        String uid = flag.getItemMeta().getPersistentDataContainer().get(KEY_UUID, PersistentDataType.STRING);

        // Insert into barrel
        Barrel barrel = (Barrel) b.getState();
        barrel.getInventory().addItem(flag);

        FlagRecord r = new FlagRecord(team.getName(), UUID.fromString(uid));
        r.setInVault(team.getName());
        records.put(team.getName(), r);
        save(r);

        // Lock the vault
        if (!team.isVaultLocked()) {
            team.lockVault();
            plugin.getTeamManager().save(team);
        }
    }

    // ── State transitions ─────────────────────────────────────────────────────

    public void onFlagPickedUp(String teamName, Player picker) {
        FlagRecord r = records.get(teamName);
        if (r == null) return;
        r.setHeld(picker.getUniqueId(), picker.getName());
        save(r);
        plugin.getLogger().info("[Flag] " + teamName + "'s flag picked up by " + picker.getName());
    }

    public void onFlagDropped(String teamName, Location loc, UUID itemEntityUUID) {
        FlagRecord r = records.get(teamName);
        if (r == null) return;
        r.setDropped(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), itemEntityUUID);
        save(r);
        plugin.getLogger().info("[Flag] " + teamName + "'s flag dropped at " + locStr(loc));
    }

    public void onFlagStoredInVault(String teamName, String vaultTeamName) {
        FlagRecord r = records.get(teamName);
        if (r == null) return;
        r.setInVault(vaultTeamName);
        save(r);
        plugin.getLogger().info("[Flag] " + teamName + "'s flag stored in " + vaultTeamName + "'s vault");

        // Lock vault of the team that owns it
        Team ownerTeam = plugin.getTeamManager().getTeam(teamName);
        if (ownerTeam != null && !ownerTeam.isVaultLocked()) {
            ownerTeam.lockVault();
            plugin.getTeamManager().save(ownerTeam);
        }
    }

    /** Force-returns flag to the lastVaultTeam's vault. Recreates barrel if needed. */
    public void returnFlagToVault(String teamName) {
        removeFlagFromWorld(teamName);
        placeInLastVault(teamName);
    }

    /**
     * Removes the flag from wherever it currently is, then places it into its last vault.
     * Loads the chunk and recreates the barrel if missing.
     * Returns the vault Team on success, null if no vault could be found.
     */
    public Team placeInLastVault(String teamName) {
        removeFlagFromWorld(teamName); // ensure no duplicate physical item exists
        FlagRecord r = records.get(teamName);
        if (r == null) return null;

        String vaultTeamName = r.getLastVaultTeam();
        Team vaultTeam = plugin.getTeamManager().getTeam(vaultTeamName);
        if (vaultTeam == null) vaultTeam = plugin.getTeamManager().getTeam(teamName);
        if (vaultTeam == null || !vaultTeam.isVaultSet()) {
            plugin.getLogger().warning("[Flag] Cannot return " + teamName + "'s flag — no vault found.");
            return null;
        }

        Location loc = vaultTeam.getVaultLocation();
        if (loc == null) return null;

        if (!loc.getChunk().isLoaded()) loc.getChunk().load();

        Block b = loc.getBlock();
        if (b.getType() != Material.BARREL) {
            b.setType(Material.BARREL);
            plugin.getLogger().info("[Flag] Recreated barrel at " + vaultTeam.getName() + "'s vault.");
        }

        Barrel barrel = (Barrel) b.getState();
        barrel.getInventory().addItem(createFlagItem(plugin.getTeamManager().getTeam(teamName)));

        r.setInVault(vaultTeam.getName());
        save(r);
        plugin.getLogger().info("[Flag] " + teamName + "'s flag placed in " + vaultTeam.getName() + "'s vault.");
        return vaultTeam;
    }

    /** Removes the physical flag from wherever it is in the world (inventory, item entity). */
    public void removeFlagFromWorld(String teamName) {
        FlagRecord r = records.get(teamName);
        if (r == null) return;

        switch (r.getState()) {
            case HELD -> {
                Player p = r.getHolderUUID() != null ? Bukkit.getPlayer(r.getHolderUUID()) : null;
                if (p != null) removeFromInventory(p.getInventory(), teamName);
            }
            case IN_VAULT -> {
                Team vt = plugin.getTeamManager().getTeam(r.getLastVaultTeam());
                if (vt != null && vt.isVaultSet()) {
                    Location loc = vt.getVaultLocation();
                    if (loc != null) {
                        Block b = loc.getBlock();
                        if (b.getType() == Material.BARREL) {
                            Barrel barrel = (Barrel) b.getState();
                            removeFromInventory(barrel.getInventory(), teamName);
                        }
                    }
                }
            }
            case DROPPED -> {
                if (r.getDroppedEntityUUID() != null) {
                    Entity e = Bukkit.getEntity(r.getDroppedEntityUUID());
                    if (e instanceof Item) e.remove();
                }
                // Also scan nearby to catch edge cases
                if (r.getDroppedWorld() != null) {
                    var world = Bukkit.getWorld(r.getDroppedWorld());
                    if (world != null) {
                        Location dl = new Location(world, r.getDroppedX(), r.getDroppedY(), r.getDroppedZ());
                        for (Entity e : world.getNearbyEntities(dl, 2, 2, 2)) {
                            if (e instanceof Item item && isFlagOf(item.getItemStack(), teamName)) e.remove();
                        }
                    }
                }
            }
        }
    }

    private void removeFromInventory(Inventory inv, String teamName) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s != null && isFlagOf(s, teamName)) inv.setItem(i, null);
        }
    }

    // ── Vault destroyed handler ───────────────────────────────────────────────

    public void onVaultDestroyed(Team team) {
        // Find any flags stored in this vault and mark as dropped
        for (FlagRecord r : records.values()) {
            if (r.getState() == FlagState.IN_VAULT && team.getName().equals(r.getLastVaultTeam())) {
                Location vl = team.getVaultLocation();
                if (vl != null) r.setDropped(vl.getWorld().getName(), vl.getX(), vl.getY(), vl.getZ(), null);
                else r.setInVault(r.getOwnerTeam()); // fallback — try to re-home it
                save(r);
            }
        }
    }

    // ── Item checks ───────────────────────────────────────────────────────────

    public boolean isFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_OWNER, PersistentDataType.STRING);
    }

    public String getFlagOwner(ItemStack item) {
        if (!isFlag(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(KEY_OWNER, PersistentDataType.STRING);
    }

    public UUID getFlagUUID(ItemStack item) {
        if (!isFlag(item)) return null;
        String s = item.getItemMeta().getPersistentDataContainer().get(KEY_UUID, PersistentDataType.STRING);
        try { return s != null ? UUID.fromString(s) : null; } catch (Exception e) { return null; }
    }

    public boolean isFlagOf(ItemStack item, String teamName) {
        return teamName.equals(getFlagOwner(item));
    }

    /** Returns true if the item is a flag but its UUID doesn't match the active record (dupe). */
    public boolean isDuplicate(ItemStack item) {
        String owner = getFlagOwner(item);
        if (owner == null) return false;
        FlagRecord r = records.get(owner);
        if (r == null) return false;
        UUID itemUUID = getFlagUUID(item);
        return itemUUID != null && !itemUUID.equals(r.getFlagUuid());
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public FlagRecord getRecord(String teamName)          { return records.get(teamName); }
    public Collection<FlagRecord> getAllRecords()         { return Collections.unmodifiableCollection(records.values()); }
    public boolean hasRecord(String teamName)             { return records.containsKey(teamName); }

    /**
     * Live location of a dropped flag: entity position if loaded, stored coords otherwise.
     * Returns null if the flag is not DROPPED or has no location data.
     */
    public Location getDroppedLocation(String teamName) {
        FlagRecord r = records.get(teamName);
        if (r == null || r.getState() != FlagState.DROPPED) return null;
        if (r.getDroppedEntityUUID() != null) {
            Entity entity = Bukkit.getEntity(r.getDroppedEntityUUID());
            if (entity != null) return entity.getLocation();
        }
        if (r.getDroppedWorld() == null) return null;
        var world = Bukkit.getWorld(r.getDroppedWorld());
        if (world == null) return null;
        return new Location(world, r.getDroppedX(), r.getDroppedY(), r.getDroppedZ());
    }

    /** Returns all flags currently held by the given player (by team name). */
    public List<String> getFlagsHeldBy(UUID playerUUID) {
        List<String> list = new ArrayList<>();
        for (FlagRecord r : records.values()) {
            if (r.getState() == FlagState.HELD && playerUUID.equals(r.getHolderUUID())) {
                list.add(r.getOwnerTeam());
            }
        }
        return list;
    }

    /** Returns all flags currently in the given team's vault. */
    public List<String> getFlagsInVaultOf(String teamName) {
        List<String> list = new ArrayList<>();
        for (FlagRecord r : records.values()) {
            if (r.getState() == FlagState.IN_VAULT && teamName.equals(r.getLastVaultTeam())) {
                list.add(r.getOwnerTeam());
            }
        }
        return list;
    }

    public void removeRecord(String teamName) {
        records.remove(teamName);
        saveAll();
    }

    // ── Event end: retire all flags ───────────────────────────────────────────

    /**
     * Strips PDC keys from every physical flag item (so isFlag() returns false)
     * and adds a visible "[Event Ended]" lore line. Clears all FlagRecords.
     */
    public void retireAllFlags() {
        for (FlagRecord r : new ArrayList<>(records.values())) {
            retireFlagItem(r);
        }
        records.clear();
        saveAll();

        // Clear glow from all online players
        for (var p : Bukkit.getOnlinePlayers()) p.setGlowing(false);
    }

    private void retireFlagItem(FlagRecord r) {
        switch (r.getState()) {
            case IN_VAULT -> {
                Team vt = plugin.getTeamManager().getTeam(r.getLastVaultTeam());
                if (vt != null && vt.isVaultSet()) {
                    Location loc = vt.getVaultLocation();
                    if (loc != null) {
                        Block b = loc.getBlock();
                        if (b.getType() == Material.BARREL)
                            retireFromInventory(((Barrel) b.getState()).getInventory(), r.getOwnerTeam());
                    }
                }
            }
            case HELD -> {
                Player p = r.getHolderUUID() != null ? Bukkit.getPlayer(r.getHolderUUID()) : null;
                if (p != null) retireFromInventory(p.getInventory(), r.getOwnerTeam());
            }
            case DROPPED -> {
                if (r.getDroppedEntityUUID() != null) {
                    Entity e = Bukkit.getEntity(r.getDroppedEntityUUID());
                    if (e instanceof Item item) {
                        ItemStack stack = item.getItemStack();
                        applyRetireMeta(stack);
                        item.setItemStack(stack);
                    }
                }
                // Nearby scan as fallback
                if (r.getDroppedWorld() != null) {
                    var world = Bukkit.getWorld(r.getDroppedWorld());
                    if (world != null) {
                        Location dl = new Location(world, r.getDroppedX(), r.getDroppedY(), r.getDroppedZ());
                        for (Entity e : world.getNearbyEntities(dl, 2, 2, 2)) {
                            if (e instanceof Item item && isFlagOf(item.getItemStack(), r.getOwnerTeam())) {
                                ItemStack stack = item.getItemStack();
                                applyRetireMeta(stack);
                                item.setItemStack(stack);
                            }
                        }
                    }
                }
            }
        }
    }

    private void retireFromInventory(Inventory inv, String teamName) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isFlagOf(item, teamName)) {
                applyRetireMeta(item);
                inv.setItem(i, item);
            }
        }
    }

    private static void applyRetireMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().remove(KEY_OWNER);
        meta.getPersistentDataContainer().remove(KEY_UUID);
        meta.lore(List.of(
            Component.text("[Event Ended]")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, true)
        ));
        item.setItemMeta(meta);
    }

    // ── Scoring ───────────────────────────────────────────────────────────────

    /**
     * Score = enemy flags physically in own vault barrel
     *       + enemy flags physically in online team members' inventories
     *       + enemy flags recorded as HELD by offline team members (fallback).
     * Uses a Set keyed by owner-team to avoid double-counting.
     */
    public int computeScore(Team team) {
        Set<String> counted = new HashSet<>();

        // 1. Physical vault scan
        if (team.isVaultSet()) {
            Location vaultLoc = team.getVaultLocation();
            if (vaultLoc != null) {
                Block b = vaultLoc.getBlock();
                if (b.getType() == Material.BARREL) {
                    Barrel barrel = (Barrel) b.getState();
                    for (ItemStack item : barrel.getInventory().getContents()) {
                        String owner = getFlagOwner(item);
                        if (owner != null) counted.add(owner);
                    }
                }
            }
        }

        // 2. Physical inventory scan of online members
        for (UUID memberUUID : team.getMembers()) {
            Player p = Bukkit.getPlayer(memberUUID);
            if (p == null) continue;
            for (ItemStack item : p.getInventory().getContents()) {
                String owner = getFlagOwner(item);
                if (owner != null) counted.add(owner);
            }
        }

        // 3. FlagRecord fallback for offline holders
        for (FlagRecord r : records.values()) {
            if (counted.contains(r.getOwnerTeam())) continue;
            if (r.getState() == FlagState.HELD
                    && r.getHolderUUID() != null
                    && team.getMembers().contains(r.getHolderUUID())
                    && Bukkit.getPlayer(r.getHolderUUID()) == null) {
                counted.add(r.getOwnerTeam());
            }
        }

        return counted.size();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    public static Material dyeToMaterial(org.bukkit.DyeColor dye) {
        return switch (dye) {
            case WHITE      -> Material.WHITE_BANNER;
            case ORANGE     -> Material.ORANGE_BANNER;
            case MAGENTA    -> Material.MAGENTA_BANNER;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_BANNER;
            case YELLOW     -> Material.YELLOW_BANNER;
            case LIME       -> Material.LIME_BANNER;
            case PINK       -> Material.PINK_BANNER;
            case GRAY       -> Material.GRAY_BANNER;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_BANNER;
            case CYAN       -> Material.CYAN_BANNER;
            case PURPLE     -> Material.PURPLE_BANNER;
            case BLUE       -> Material.BLUE_BANNER;
            case BROWN      -> Material.BROWN_BANNER;
            case GREEN      -> Material.GREEN_BANNER;
            case RED        -> Material.RED_BANNER;
            case BLACK      -> Material.BLACK_BANNER;
        };
    }

    private static String locStr(Location l) {
        return l.getWorld().getName() + " " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }
}
