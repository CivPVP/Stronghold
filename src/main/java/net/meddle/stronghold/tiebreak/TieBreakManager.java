package net.meddle.stronghold.tiebreak;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.team.Team;
import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TieBreakManager {

    public static final NamespacedKey KEY_TIEBREAK =
        new NamespacedKey("stronghold", "tiebreak_flag");

    private final Stronghold plugin;
    private final File vaultsFile;
    private final List<TieBreakVault> vaults = new ArrayList<>();

    /** Teams involved in the current tie — only they may interact with tie-breaking flags. */
    private Set<String> tiedTeams = new HashSet<>();

    public TieBreakManager(Stronghold plugin) {
        this.plugin     = plugin;
        this.vaultsFile = new File(plugin.getDataFolder(), "tiebreak_vaults.yml");
    }

    // ── Vault record ──────────────────────────────────────────────────────────

    public record TieBreakVault(String world, int x, int y, int z) {
        public Location toLocation() {
            World w = Bukkit.getWorld(world);
            return w != null ? new Location(w, x, y, z) : null;
        }

        public String display() {
            return world + " " + x + ", " + y + ", " + z;
        }
    }

    // ── Vault management ──────────────────────────────────────────────────────

    public void addVault(Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        for (TieBreakVault v : vaults) {
            if (v.world().equals(world) && v.x() == x && v.y() == y && v.z() == z) return; // already registered
        }
        vaults.add(new TieBreakVault(world, x, y, z));
        saveVaults();
    }

    public boolean removeVault(int index) {
        if (index < 0 || index >= vaults.size()) return false;
        vaults.remove(index);
        saveVaults();
        return true;
    }

    public List<TieBreakVault> getVaults() {
        return Collections.unmodifiableList(vaults);
    }

    public boolean hasVaults() {
        return !vaults.isEmpty();
    }

    public boolean isVaultBlock(Block b) {
        for (TieBreakVault v : vaults) {
            if (v.world().equals(b.getWorld().getName())
                    && v.x() == b.getX() && v.y() == b.getY() && v.z() == b.getZ()) {
                return true;
            }
        }
        return false;
    }

    public void loadVaults() {
        vaults.clear();
        if (!vaultsFile.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(vaultsFile);
        for (Map<?, ?> map : c.getMapList("vaults")) {
            try {
                String world = (String) map.get("world");
                int x = ((Number) map.get("x")).intValue();
                int y = ((Number) map.get("y")).intValue();
                int z = ((Number) map.get("z")).intValue();
                // Skip duplicates that may have been written by older plugin versions
                boolean dup = false;
                for (TieBreakVault existing : vaults) {
                    if (existing.world().equals(world) && existing.x() == x && existing.y() == y && existing.z() == z) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) vaults.add(new TieBreakVault(world, x, y, z));
            } catch (Exception e) {
                plugin.getLogger().warning("[TieBreak] Skipping malformed vault entry: " + e.getMessage());
            }
        }
    }

    public void saveVaults() {
        YamlConfiguration c = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (TieBreakVault v : vaults) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("world", v.world());
            map.put("x", v.x());
            map.put("y", v.y());
            map.put("z", v.z());
            list.add(map);
        }
        c.set("vaults", list);
        try { c.save(vaultsFile); }
        catch (IOException e) {
            plugin.getLogger().severe("[TieBreak] Could not save tiebreak_vaults.yml: " + e.getMessage());
        }
    }

    // ── Tied teams ────────────────────────────────────────────────────────────

    public void setTiedTeams(Set<String> teams) {
        this.tiedTeams = new HashSet<>(teams);
    }

    public Set<String> getTiedTeams() {
        return Collections.unmodifiableSet(tiedTeams);
    }

    public void clearTiedTeams() {
        tiedTeams.clear();
    }

    public boolean isFromTiedTeam(Player p) {
        Team team = plugin.getTeamManager().getTeamOf(p.getUniqueId());
        return team != null && tiedTeams.contains(team.getName());
    }

    // ── Flag creation ─────────────────────────────────────────────────────────

    public ItemStack createFlag() {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        meta.displayName(Component.text("Tie-Breaking Flag")
            .color(Msg.LIGHT_BLUE).decorate(TextDecoration.BOLD)
            .decoration(TextDecoration.ITALIC, false));
        // Bottom half light blue on white base = half-and-half banner
        meta.addPattern(new Pattern(DyeColor.LIGHT_BLUE, PatternType.HALF_HORIZONTAL_BOTTOM));
        meta.setUnbreakable(true);
        meta.setMaxStackSize(1);
        meta.getPersistentDataContainer().set(KEY_TIEBREAK, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isTieBreakFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(KEY_TIEBREAK, PersistentDataType.BYTE);
    }

    // ── Spawn / retire ────────────────────────────────────────────────────────

    /** Spawns one tie-breaking flag in each configured vault. */
    public void spawnFlags() {
        for (TieBreakVault v : vaults) {
            Location loc = v.toLocation();
            if (loc == null) {
                plugin.getLogger().warning("[TieBreak] World not loaded for vault at " + v.display());
                continue;
            }
            if (!loc.getChunk().isLoaded()) loc.getChunk().load();
            Block b = loc.getBlock();
            if (b.getType() != Material.BARREL) b.setType(Material.BARREL);
            ((Barrel) b.getState()).getInventory().addItem(createFlag());
        }
    }

    /** Returns a tie-breaking flag to the first vault with space, recreating the barrel if needed. */
    public void returnFlagToVault() {
        for (TieBreakVault v : vaults) {
            Location loc = v.toLocation();
            if (loc == null) continue;
            if (!loc.getChunk().isLoaded()) loc.getChunk().load();
            Block b = loc.getBlock();
            if (b.getType() != Material.BARREL) b.setType(Material.BARREL);
            Barrel barrel = (Barrel) b.getState();
            if (barrel.getInventory().firstEmpty() != -1) {
                barrel.getInventory().addItem(createFlag());
                return;
            }
        }
        // All full — use first vault regardless
        if (!vaults.isEmpty()) {
            Location loc = vaults.get(0).toLocation();
            if (loc != null) {
                if (!loc.getChunk().isLoaded()) loc.getChunk().load();
                Block b = loc.getBlock();
                if (b.getType() != Material.BARREL) b.setType(Material.BARREL);
                ((Barrel) b.getState()).getInventory().addItem(createFlag());
            }
        }
    }

    /**
     * Strips the tie-break PDC key from every physical tie-breaking flag and adds
     * "[Event Ended]" lore — mirrors FlagManager.retireAllFlags(). Call at event end.
     */
    public void retireAllFlags() {
        // Player inventories
        for (Player p : Bukkit.getOnlinePlayers()) {
            retireFromInventory(p.getInventory());
        }
        // Tie-break vault barrels
        for (TieBreakVault v : vaults) {
            Location loc = v.toLocation();
            if (loc == null) continue;
            Block b = loc.getBlock();
            if (b.getType() == Material.BARREL) {
                retireFromInventory(((Barrel) b.getState()).getInventory());
            }
        }
        // Team vault barrels (players may have deposited TB flags there)
        for (net.meddle.stronghold.team.Team team : plugin.getTeamManager().getAllTeams()) {
            if (!team.isVaultSet()) continue;
            Location loc = team.getVaultLocation();
            if (loc == null) continue;
            Block b = loc.getBlock();
            if (b.getType() == Material.BARREL) {
                retireFromInventory(((Barrel) b.getState()).getInventory());
            }
        }
        // Item entities in the world
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item && isTieBreakFlag(item.getItemStack())) {
                    ItemStack stack = item.getItemStack();
                    applyRetireMeta(stack);
                    item.setItemStack(stack);
                }
            }
        }
    }

    /** Physically removes all tie-breaking flags from the world — use for full reset only. */
    public void removeAllFlags() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            var inv = p.getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (isTieBreakFlag(inv.getItem(i))) inv.setItem(i, null);
            }
        }
        for (TieBreakVault v : vaults) {
            Location loc = v.toLocation();
            if (loc == null) continue;
            Block b = loc.getBlock();
            if (b.getType() == Material.BARREL) {
                var inv = ((Barrel) b.getState()).getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    if (isTieBreakFlag(inv.getItem(i))) inv.setItem(i, null);
                }
            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item && isTieBreakFlag(item.getItemStack())) {
                    entity.remove();
                }
            }
        }
    }

    private void retireFromInventory(org.bukkit.inventory.Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isTieBreakFlag(item)) {
                applyRetireMeta(item);
                inv.setItem(i, item);
            }
        }
    }

    private void applyRetireMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().remove(KEY_TIEBREAK);
        meta.lore(List.of(
            net.kyori.adventure.text.Component.text("[Event Ended]")
                .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, true)
        ));
        item.setItemMeta(meta);
    }

    // ── Scoring ───────────────────────────────────────────────────────────────

    /**
     * Counts tie-breaking flags controlled by the given team:
     * flags held by online players of this team, and flags in the team's own vault.
     */
    public int computeScore(Team team) {
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            Team pt = plugin.getTeamManager().getTeamOf(p.getUniqueId());
            if (pt != null && pt.getName().equals(team.getName())) {
                for (ItemStack item : p.getInventory().getContents()) {
                    if (isTieBreakFlag(item)) count++;
                }
            }
        }
        if (team.isVaultSet()) {
            Location loc = team.getVaultLocation();
            if (loc != null) {
                Block b = loc.getBlock();
                if (b.getType() == Material.BARREL) {
                    var inv = ((Barrel) b.getState()).getInventory();
                    for (ItemStack item : inv.getContents()) {
                        if (isTieBreakFlag(item)) count++;
                    }
                }
            }
        }
        return count;
    }
}
