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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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

        Team vault = getVaultTeam(b);
        if (vault == null) return;

        Player p = e.getPlayer();

        if (p.isOp()) {
            // Operators can break — handle any flags inside
            flags.onVaultDestroyed(vault);
            // Clear vault registration
            vault.setVaultRaw(null, 0, 0, 0);
            vault.setVaultLocked(false);
            teams.save(vault);
            return;
        }

        Team playerTeam = teams.getTeamOf(p.getUniqueId());

        // Friendly or teamless → deny
        if (playerTeam == null || playerTeam.getName().equals(vault.getName())) {
            e.setCancelled(true);
            p.sendMessage(plugin.getCfg().msg("not_op"));
            return;
        }

        // Enemy → allowed; flag state will be handled by item drop naturally
        flags.onVaultDestroyed(vault);
    }

    // ── Explosion protection ──────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!plugin.getCfg().isPreventVaultExplosion()) return;
        e.blockList().removeIf(b -> b.getType() == Material.BARREL && getVaultTeam(b) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!plugin.getCfg().isPreventVaultExplosion()) return;
        e.blockList().removeIf(b -> b.getType() == Material.BARREL && getVaultTeam(b) != null);
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

        ItemStack cursor  = e.getCursor();
        ItemStack current = e.getCurrentItem();

        // 1. Prevent placing a flag into any non-vault container
        if (plugin.getCfg().isPreventFlagInNonVaultContainers()) {
            if (flags.isFlag(cursor)) {
                if (!isVaultInventory(e.getInventory())) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (flags.isFlag(current) && isShiftMoveToNonVault(e)) {
                e.setCancelled(true);
                return;
            }
        }

        // 2. Vault-specific rules
        Team vault = getVaultFromInventory(e.getInventory());
        if (vault == null) return;

        Team playerTeam = teams.getTeamOf(p.getUniqueId());

        // Teamless players: deny flag removal and flag placement
        if (playerTeam == null) {
            if (isRemovingFlag(e) || flags.isFlag(cursor)) e.setCancelled(true);
            return;
        }

        // Same team: deny removing flags, allow non-flag removal
        if (playerTeam.getName().equals(vault.getName())) {
            if (isRemovingFlag(e)) {
                e.setCancelled(true);
                return;
            }
            // Cursor drag into vault slot
            if (flags.isFlag(cursor) && e.getClickedInventory() == e.getInventory()) {
                String owner = flags.getFlagOwner(cursor);
                if (owner != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        flags.onFlagStoredInVault(owner, vault.getName());
                        plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                    });
                }
            }
            // Shift-click from player inventory into vault
            if (flags.isFlag(current)
                    && e.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
                    && e.getClickedInventory() != e.getInventory()) {
                String owner = flags.getFlagOwner(current);
                if (owner != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        flags.onFlagStoredInVault(owner, vault.getName());
                        plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                    });
                }
            }
            return;
        }

        // Enemy team: allow all (they can steal flags)
        // Track flag removal from vault
        if (isRemovingFlag(e)) {
            ItemStack removed = current != null && flags.isFlag(current) ? current : cursor;
            if (flags.isFlag(removed)) {
                String owner = flags.getFlagOwner(removed);
                if (owner != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        flags.onFlagPickedUp(owner, p);
                        plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                    });
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!flags.isFlag(e.getOldCursor())) return;

        // Dragging a flag into a non-vault inventory is blocked
        if (plugin.getCfg().isPreventFlagInNonVaultContainers() && !isVaultInventory(e.getInventory())) {
            e.setCancelled(true);
            return;
        }

        Team vault = getVaultFromInventory(e.getInventory());
        if (vault == null) return;

        // Track flag stored in vault + remove glow
        String owner = flags.getFlagOwner(e.getOldCursor());
        if (owner != null) {
            Player dragger = (Player) e.getWhoClicked();
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

        // Block hopper extraction from any vault
        if (getVaultFromInventory(e.getSource()) != null) {
            e.setCancelled(true);
            return;
        }

        // Block hopper insertion into any non-vault container
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

    private boolean isRemovingFlag(InventoryClickEvent e) {
        // Removing = taking an item FROM the vault inventory (clicked inv == the vault)
        ItemStack current = e.getCurrentItem();
        if (current != null && flags.isFlag(current)
                && e.getClickedInventory() == e.getInventory()) {
            return switch (e.getAction()) {
                case PICKUP_ALL, PICKUP_HALF, PICKUP_SOME, PICKUP_ONE,
                     MOVE_TO_OTHER_INVENTORY, DROP_ALL_SLOT, DROP_ONE_SLOT -> true;
                default -> false;
            };
        }
        return false;
    }

    private boolean isShiftMoveToNonVault(InventoryClickEvent e) {
        return e.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
            && e.getClickedInventory() != null
            && e.getClickedInventory() != e.getInventory()
            && !isVaultInventory(e.getInventory());
    }
}
