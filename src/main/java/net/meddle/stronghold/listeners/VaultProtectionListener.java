package net.meddle.stronghold.listeners;

import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.team.Team;
import net.meddle.stronghold.team.TeamManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;

public class VaultProtectionListener implements Listener {

    private final Stronghold plugin;
    private final TeamManager teams;
    private final FlagManager flags;

    public VaultProtectionListener(Stronghold plugin) {
        this.plugin = plugin;
        this.teams  = plugin.getTeamManager();
        this.flags  = plugin.getFlagManager();
    }

    // ── Block break ───────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() != Material.BARREL) return;

        // Protect tie-breaking vaults — only ops may break them
        if (plugin.getTieBreakManager().isVaultBlock(b)) {
            if (!e.getPlayer().isOp()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(plugin.getCfg().msg("not_op"));
            }
            return;
        }

        Team vault = getVaultTeam(b);
        if (vault == null) return;

        Player p = e.getPlayer();

        if (p.isOp()) {
            flags.onVaultDestroyed(vault);
            return;
        }

        Team playerTeam = teams.getTeamOf(p.getUniqueId());

        // Friendly or teamless → deny
        if (playerTeam == null || playerTeam.getName().equals(vault.getName())) {
            e.setCancelled(true);
            p.sendMessage(plugin.getCfg().msg("not_op"));
            return;
        }

        // Enemy → allowed; flag state handled by BlockDropItemEvent
        flags.onVaultDestroyed(vault);
    }

    // ── Vault barrel drop tracking ────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent e) {
        Block b = e.getBlock();
        // Barrel is already AIR here — look up by coords directly
        Team vault = teams.getTeamByVault(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
        if (vault == null) return;
        for (org.bukkit.entity.Item item : e.getItems()) {
            String owner = flags.getFlagOwner(item.getItemStack());
            if (owner == null) continue;
            flags.onFlagDropped(owner, item.getLocation(), item.getUniqueId());
        }
    }

    // ── Explosion protection ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> b.getType() == Material.BARREL
            && (getVaultTeam(b) != null || plugin.getTieBreakManager().isVaultBlock(b)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(b -> b.getType() == Material.BARREL
            && (getVaultTeam(b) != null || plugin.getTieBreakManager().isVaultBlock(b)));
    }

    // ── Block place ───────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (flags.isFlag(e.getItemInHand())) e.setCancelled(true);
    }

    // ── Inventory interactions ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // COLLECT_TO_CURSOR (double-click): block if it would sweep flags out of a vault
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            if (isVaultInventory(e.getInventory()) && inventoryHasFlag(e.getInventory())) {
                e.setCancelled(true);
                return;
            }
        }

        ItemStack cursor  = e.getCursor();
        ItemStack current = e.getCurrentItem();

        // Prevent flags from being inserted into bundles
        if (flags.isFlag(cursor) && current != null && current.getType() == Material.BUNDLE) {
            e.setCancelled(true);
            return;
        }

        // Tie-breaking flags may only be placed in the player's own team vault (not enemy vaults, chests, etc.)
        var tbm = plugin.getTieBreakManager();
        if (tbm.isTieBreakFlag(cursor)
                && e.getClickedInventory() != null
                && !e.getClickedInventory().equals(p.getInventory())) {
            if (!isOwnTeamVault(e.getInventory(), p)) {
                e.setCancelled(true);
                return;
            }
        }
        if (tbm.isTieBreakFlag(current)
                && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(p.getInventory())) {
            if (!isOwnTeamVault(e.getInventory(), p)) {
                e.setCancelled(true);
                return;
            }
        }
        // Only tied-team players may interact with tie-breaking flags sitting in any vault
        if (tbm.isTieBreakFlag(current) && getVaultFromInventory(e.getInventory()) != null) {
            if (!tbm.isFromTiedTeam(p)) {
                e.setCancelled(true);
                return;
            }
        }

        // Prevent placing a flag into any non-vault container
        if (plugin.getCfg().isPreventFlagInNonVaultContainers()) {
            if (flags.isFlag(cursor) && !isVaultInventory(e.getInventory())) {
                e.setCancelled(true);
                return;
            }
            if (flags.isFlag(current) && isShiftMoveToNonVault(e)) {
                e.setCancelled(true);
                return;
            }
        }

        Team vault = getVaultFromInventory(e.getInventory());
        if (vault == null) return;

        Team playerTeam = teams.getTeamOf(p.getUniqueId());

        // Teamless: deny all flag interactions with the vault
        if (playerTeam == null) {
            if (isRemovingFlag(e) || isDepositingFlag(e, p)) {
                e.setCancelled(true);
            }
            return;
        }

        // Same team: deny removal, allow and track deposit
        if (playerTeam.getName().equals(vault.getName())) {
            if (isRemovingFlag(e)) {
                e.setCancelled(true);
                return;
            }
            trackFlagDeposit(e, vault, p);
            return;
        }

        // Enemy team: track removal and deposit; block own flag into enemy vault
        if (isRemovingFlag(e)) {
            // current is guaranteed non-null and a flag by isRemovingFlag
            String owner = flags.getFlagOwner(current);
            if (owner != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    flags.onFlagPickedUp(owner, p);
                    plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                });
            }
        }
        String depositOwner = getDepositedFlagOwner(e, p);
        if (depositOwner != null && depositOwner.equals(playerTeam.getName())) {
            e.setCancelled(true);
            return;
        }
        trackFlagDeposit(e, vault, p);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player dragger)) return;
        if (!flags.isFlag(e.getOldCursor())) return;

        if (plugin.getCfg().isPreventFlagInNonVaultContainers() && !isVaultInventory(e.getInventory())) {
            e.setCancelled(true);
            return;
        }

        Team vault = getVaultFromInventory(e.getInventory());
        if (vault == null) return;

        if (teams.getTeamOf(dragger.getUniqueId()) == null) {
            e.setCancelled(true);
            return;
        }
        String owner = flags.getFlagOwner(e.getOldCursor());
        if (owner != null) {
            Team dragTeam = teams.getTeamOf(dragger.getUniqueId());
            if (dragTeam != null && owner.equals(dragTeam.getName()) && !vault.getName().equals(dragTeam.getName())) {
                e.setCancelled(true);
                return;
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                flags.onFlagStoredInVault(owner, vault.getName());
                plugin.getFlagCarrierListener().applyGlowForPlayer(dragger);
            });
        }
    }

    // ── Hopper protection ─────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        if (!flags.isFlag(e.getItem())) return;
        if (getVaultFromInventory(e.getSource()) != null) {
            e.setCancelled(true);
            return;
        }
        if (plugin.getCfg().isPreventFlagInNonVaultContainers()
                && getVaultFromInventory(e.getDestination()) == null) {
            e.setCancelled(true);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Team getVaultTeam(Block b) {
        if (b.getType() != Material.BARREL) return null;
        return teams.getTeamByVault(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
    }

    private Team getVaultFromInventory(org.bukkit.inventory.Inventory inv) {
        if (!(inv.getHolder() instanceof BlockInventoryHolder bih)) return null;
        Block b = bih.getBlock();
        return teams.getTeamByVault(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
    }

    private boolean isVaultInventory(org.bukkit.inventory.Inventory inv) {
        return getVaultFromInventory(inv) != null;
    }

    /**
     * Deny-by-default: any click on a vault slot that contains a flag is a removal.
     * Covers PICKUP_*, HOTBAR_SWAP, SWAP_WITH_CURSOR, DROP_*_SLOT, MOVE_TO_OTHER_INVENTORY, etc.
     */
    private boolean isRemovingFlag(InventoryClickEvent e) {
        if (e.getClickedInventory() != e.getInventory()) return false;
        ItemStack current = e.getCurrentItem();
        return current != null && flags.isFlag(current);
    }

    /** True if a flag from the cursor, hotbar, or player inventory is heading into the vault. */
    private boolean isDepositingFlag(InventoryClickEvent e, Player p) {
        if (flags.isFlag(e.getCursor())) return true;
        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && e.getClickedInventory() != e.getInventory()
                && flags.isFlag(e.getCurrentItem())) return true;
        if (e.getAction() == InventoryAction.HOTBAR_SWAP
                && e.getClickedInventory() == e.getInventory()) {
            int slot = e.getHotbarButton();
            if (slot >= 0) return flags.isFlag(p.getInventory().getItem(slot));
        }
        return false;
    }

    private String getDepositedFlagOwner(InventoryClickEvent e, Player p) {
        if (flags.isFlag(e.getCursor()) && e.getClickedInventory() == e.getInventory())
            return flags.getFlagOwner(e.getCursor());
        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && e.getClickedInventory() != e.getInventory()
                && flags.isFlag(e.getCurrentItem()))
            return flags.getFlagOwner(e.getCurrentItem());
        if (e.getAction() == InventoryAction.HOTBAR_SWAP
                && e.getClickedInventory() == e.getInventory()) {
            int slot = e.getHotbarButton();
            if (slot >= 0) {
                ItemStack hotbar = p.getInventory().getItem(slot);
                if (flags.isFlag(hotbar)) return flags.getFlagOwner(hotbar);
            }
        }
        return null;
    }

    private void trackFlagDeposit(InventoryClickEvent e, Team vault, Player p) {
        // Cursor click into vault slot
        if (flags.isFlag(e.getCursor()) && e.getClickedInventory() == e.getInventory()) {
            scheduleStoreInVault(flags.getFlagOwner(e.getCursor()), vault, p);
            return;
        }
        // Shift-click from player inventory into vault
        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && e.getClickedInventory() != e.getInventory()
                && flags.isFlag(e.getCurrentItem())) {
            scheduleStoreInVault(flags.getFlagOwner(e.getCurrentItem()), vault, p);
            return;
        }
        // Hotbar-swap flag into vault slot
        if (e.getAction() == InventoryAction.HOTBAR_SWAP
                && e.getClickedInventory() == e.getInventory()) {
            int slot = e.getHotbarButton();
            if (slot >= 0) {
                ItemStack hotbar = p.getInventory().getItem(slot);
                if (flags.isFlag(hotbar)) scheduleStoreInVault(flags.getFlagOwner(hotbar), vault, p);
            }
        }
    }

    private void scheduleStoreInVault(String owner, Team vault, Player p) {
        if (owner == null) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            flags.onFlagStoredInVault(owner, vault.getName());
            plugin.getFlagCarrierListener().applyGlowForPlayer(p);
        });
    }

    private boolean inventoryHasFlag(org.bukkit.inventory.Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (flags.isFlag(item)) return true;
        }
        return false;
    }

    private boolean isOwnTeamVault(org.bukkit.inventory.Inventory inv, Player p) {
        Team vault = getVaultFromInventory(inv);
        if (vault == null) return false;
        Team playerTeam = teams.getTeamOf(p.getUniqueId());
        return playerTeam != null && playerTeam.getName().equals(vault.getName());
    }

    private boolean isShiftMoveToNonVault(InventoryClickEvent e) {
        return e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
            && e.getClickedInventory() != null
            && e.getClickedInventory() != e.getInventory()
            && !isVaultInventory(e.getInventory());
    }
}
