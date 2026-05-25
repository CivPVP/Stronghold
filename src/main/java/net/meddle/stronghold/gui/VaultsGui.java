package net.meddle.stronghold.gui;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class VaultsGui implements Listener {

    private static final String TITLE = "§8§lVault Status";

    private final Stronghold plugin;

    public VaultsGui(Stronghold plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, FlagsGui.sec(TITLE));

        // Filler
        Material filler = FlagsGui.matOrFallback(plugin.getCfg().getFillerMaterial(), Material.BLACK_STAINED_GLASS_PANE);
        ItemStack fillerItem = new ItemStack(filler);
        ItemMeta fillerMeta = fillerItem.getItemMeta();
        fillerMeta.displayName(Component.empty());
        fillerItem.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) inv.setItem(i, fillerItem);

        // One icon per team — same slot layout as FlagsGui
        int slot = 10;
        for (Team team : plugin.getTeamManager().getAllTeams()) {
            if (slot >= 44) break;
            inv.setItem(slot, buildIcon(team));
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        p.openInventory(inv);
    }

    private ItemStack buildIcon(Team team) {
        boolean hasVault = team.isVaultSet();

        // BARREL if vault exists, BARRIER if not
        Material mat = hasVault ? Material.BARREL : Material.BARRIER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(FlagsGui.amp("&f" + team.getName() + "'s Vault"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (!hasVault) {
            lore.add(FlagsGui.amp("&cNo vault configured"));
        } else {
            lore.add(FlagsGui.amp("&7World: &f" + team.getVaultWorld()));
            lore.add(FlagsGui.amp("&7Location: &f" + team.getVaultX() + ", " + team.getVaultY() + ", " + team.getVaultZ()));
            lore.add(FlagsGui.amp("&7Locked: " + (team.isVaultLocked() ? "&cYes" : "&aNo")));

            // Flags currently stored in this vault
            List<String> storedFlags = plugin.getFlagManager().getFlagsInVaultOf(team.getName());
            lore.add(Component.empty());
            if (storedFlags.isEmpty()) {
                lore.add(FlagsGui.amp("&7Stored flags: &8None"));
            } else {
                lore.add(FlagsGui.amp("&7Stored flags:"));
                for (String flagTeam : storedFlags) {
                    boolean isOwn = flagTeam.equals(team.getName());
                    String color = isOwn ? "&a" : "&e";
                    lore.add(FlagsGui.amp("  " + color + flagTeam + "'s Flag" + (isOwn ? " &7(own)" : "")));
                }
            }
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().title().equals(FlagsGui.sec(TITLE))) {
            e.setCancelled(true);
        }
    }
}
