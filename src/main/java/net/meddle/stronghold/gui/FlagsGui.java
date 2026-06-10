package net.meddle.stronghold.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.team.Team;
import net.meddle.stronghold.tiebreak.TieBreakManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlagsGui implements Listener {

    private final Stronghold plugin;

    public FlagsGui(Stronghold plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        // Title from config is already §-encoded by ConfigManager.color()
        String title = plugin.getCfg().getFlagsGuiTitle();
        Inventory inv = Bukkit.createInventory(null, 54, sec(title));

        // Filler
        Material filler = matOrFallback(plugin.getCfg().getFillerMaterial(), Material.BLACK_STAINED_GLASS_PANE);
        ItemStack fillerItem = new ItemStack(filler);
        ItemMeta fillerMeta = fillerItem.getItemMeta();
        fillerMeta.displayName(Component.empty());
        fillerItem.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) inv.setItem(i, fillerItem);

        // One icon per team — columns 1-7 of rows 1-3 (slots 10-16, 19-25, 28-34)
        int slot = 10;
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            if (slot >= 44) break;
            inv.setItem(slot, buildIcon(team, plugin.getFlagManager().getRecord(team.getName())));
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2; // skip border columns
        }

        // Tie-breaking flags — row 5 inner slots (46-52), one per deployed flag
        var tbRecords = plugin.getTieBreakManager().getAllTBRecords();
        if (!tbRecords.isEmpty()) {
            int tbSlot = 46;
            for (Map.Entry<UUID, TieBreakManager.TBRecord> entry : tbRecords.entrySet()) {
                if (tbSlot > 52) break;
                inv.setItem(tbSlot, buildTBIcon(entry.getKey(), entry.getValue()));
                tbSlot++;
            }
        }

        p.openInventory(inv);
    }

    private ItemStack buildIcon(Team team, FlagRecord r) {
        // Icon material always matches the team's real flag color
        Material mat = teamBannerMaterial(team);
        List<String> lore = new ArrayList<>();

        if (r == null) {
            lore.add("&7Status: &aIn vault &7(not deployed)");
        } else {
            switch (r.getState()) {
                case IN_VAULT -> {
                    lore.add("&7Status: &aIn vault");
                    lore.add("&7Held by: &f" + r.getLastVaultTeam() + "'s vault");
                }
                case HELD -> {
                    lore.add("&7Status: &eHeld by player");
                    lore.add("&7Carrier: &f" + (r.getHolderName() != null ? r.getHolderName() : "Unknown"));
                }
                case DROPPED -> {
                    Location loc = plugin.getFlagManager().getDroppedLocation(team.getName());
                    lore.add("&7Status: &cDropped in world");
                    if (loc != null) {
                        lore.add("&7World: &f" + loc.getWorld().getName());
                        lore.add("&7Location: &f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                    } else {
                        lore.add("&7Location: &funknown");
                    }
                }
            }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        // Display name: bold + team color, matching the real item
        meta.displayName(
            net.kyori.adventure.text.Component.text(team.getName() + "'s Flag")
                .color(net.meddle.stronghold.scoreboard.TeamScoreboardManager.dyeToNamedColor(team.getColor()))
                .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
        );

        List<Component> loreComponents = new ArrayList<>();
        loreComponents.add(Component.empty());
        for (String line : lore) loreComponents.add(amp(line));
        meta.lore(loreComponents);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildTBIcon(UUID flagId, TieBreakManager.TBRecord r) {
        ItemStack item = new ItemStack(Material.WHITE_BANNER);
        var meta = (org.bukkit.inventory.meta.BannerMeta) item.getItemMeta();
        meta.displayName(Component.text("Tie-Breaking Flag")
            .color(net.meddle.stronghold.Msg.LIGHT_BLUE)
            .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
        meta.addPattern(new Pattern(DyeColor.LIGHT_BLUE, PatternType.HALF_HORIZONTAL_BOTTOM));

        List<String> lore = new ArrayList<>();
        switch (r.state) {
            case IN_VAULT -> lore.add("&7Status: &aIn vault");
            case HELD -> {
                lore.add("&7Status: &eHeld by player");
                String carrier = r.holderName != null ? r.holderName : "Unknown";
                Player online = r.holderUUID != null ? Bukkit.getPlayer(r.holderUUID) : null;
                if (online != null) {
                    var loc = online.getLocation();
                    lore.add("&7Carrier: &f" + carrier);
                    lore.add("&7Location: &f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                        + " (" + loc.getWorld().getName() + ")");
                } else {
                    lore.add("&7Carrier: &f" + carrier);
                }
            }
            case DROPPED -> {
                lore.add("&7Status: &cDropped in world");
                Location loc = plugin.getTieBreakManager().getTBDroppedLocation(flagId);
                if (loc != null) {
                    lore.add("&7Location: &f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                        + " (" + loc.getWorld().getName() + ")");
                } else {
                    lore.add("&7Location: &funknown");
                }
            }
        }

        List<Component> loreComponents = new ArrayList<>();
        loreComponents.add(Component.empty());
        for (String line : lore) loreComponents.add(amp(line));
        meta.lore(loreComponents);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().title().equals(sec(plugin.getCfg().getFlagsGuiTitle()))) {
            e.setCancelled(true);
        }
    }

    // ── Serializers ───────────────────────────────────────────────────────────

    /** For §-encoded strings (already converted by ConfigManager.color()). */
    static Component sec(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }

    /** For &-encoded strings (hardcoded lore / display names). */
    static Component amp(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    private static Material teamBannerMaterial(Team team) {
        return net.meddle.stronghold.flag.FlagManager.dyeToMaterial(team.getColor());
    }

    static Material matOrFallback(String name, Material fallback) {
        if (name == null) return fallback;
        try { return Material.valueOf(name.toUpperCase()); }
        catch (Exception e) { return fallback; }
    }
}
