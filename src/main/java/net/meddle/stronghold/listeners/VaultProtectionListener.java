package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.team.Team;
import net.meddle.stronghold.team.TeamManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.UUID;
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
        // Barrel is already AIR — look up by coords directly for both vault types
        Team vault = teams.getTeamByVault(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
        boolean isTBVault = plugin.getTieBreakManager().isVaultBlock(b);
        if (vault == null && !isTBVault) return;
        var tbm = plugin.getTieBreakManager();
        for (org.bukkit.entity.Item item : e.getItems()) {
            String owner = flags.getFlagOwner(item.getItemStack());
            if (owner != null) {
                flags.onFlagDropped(owner, item.getLocation(), item.getUniqueId());
                continue;
            }
            if (tbm.isTieBreakFlag(item.getItemStack())) {
                UUID tbId = tbm.getTBFlagUUID(item.getItemStack());
                if (tbId != null) tbm.onTBDropped(tbId, item.getLocation(), item.getUniqueId());
            }
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
        if (isAnyFlag(e.getItemInHand())) e.setCancelled(true);
    }

    // ── Inventory interactions ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // COLLECT_TO_CURSOR (double-click): block if it would sweep any flag out of a vault
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            if (isVaultInventory(e.getInventory()) && inventoryHasAnyFlag(e.getInventory())) {
                e.setCancelled(true);
                return;
            }
        }

        ItemStack cursor  = e.getCursor();
        ItemStack current = e.getCurrentItem();

        // Prevent any flag from being inserted into a bundle
        if (isAnyFlag(cursor) && current != null && current.getType() == Material.BUNDLE) {
            e.setCancelled(true);
            return;
        }

        var tbm = plugin.getTieBreakManager();

        // ── Tie-breaking flag deposit restrictions ────────────────────────────
        // TB flags may only be placed in the player's own team vault.
        if (tbm.isTieBreakFlag(cursor)
                && e.getClickedInventory() != null
                && !e.getClickedInventory().equals(p.getInventory())) {
            if (!isOwnTeamVault(e.getInventory(), p)) {
                e.setCancelled(true);
                return;
            }
        }
        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(p.getInventory())
                && tbm.isTieBreakFlag(current)) {
            if (!isOwnTeamVault(e.getInventory(), p)) {
                e.setCancelled(true);
                return;
            }
        }

        // ── Tie-breaking flag inside any vault (team OR tie-break vault) ──────
        // Unified: access control + removal tracking. Covers both vault types.
        if (tbm.isTieBreakFlag(current)) {
            boolean inTeamVault = getVaultFromInventory(e.getInventory()) != null;
            boolean inTBVault   = isTBVaultInventory(e.getInventory());
            if (inTeamVault || inTBVault) {
                if (!tbm.isFromTiedTeam(p)) {
                    e.setCancelled(true);
                    return;
                }
                // Own team cannot retrieve a secured TB flag from their vault
                if (inTeamVault && isOwnTeamVault(e.getInventory(), p)
                        && e.getClickedInventory() == e.getInventory()) {
                    e.setCancelled(true);
                    return;
                }
                // Removal: player clicked on the vault side — track and broadcast
                if (e.getClickedInventory() == e.getInventory()) {
                    UUID tbFlagId = tbm.getTBFlagUUID(current);
                    if (tbFlagId != null) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            tbm.onTBPickedUp(tbFlagId, p);
                            plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                        });
                        Msg.broadcast(
                            Component.text(p.getName(), Msg.WHITE)
                                .append(Component.text(" picked up the ", Msg.LIGHT_BLUE))
                                .append(Component.text("Tie-Breaking Flag", Msg.WHITE)
                                    .decorate(TextDecoration.BOLD))
                                .append(Component.text("!", Msg.LIGHT_BLUE)));
                    }
                    return;
                }
                // Deposit (shift-click from player-inventory side): fall through so the
                // same-team block and trackTBFlagDeposit can register the IN_VAULT state.
            }
        }

        // Prevent placing a regular flag into any non-vault container
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

        // Same team: deny regular-flag removal, allow and track deposit of both flag types
        if (playerTeam.getName().equals(vault.getName())) {
            if (isRemovingFlag(e)) {
                e.setCancelled(true);
                return;
            }
            trackFlagDeposit(e, vault, p);
            trackTBFlagDeposit(e, vault, p);
            return;
        }

        // Enemy team: track regular-flag removal and deposit; block own flag into enemy vault
        if (isRemovingFlag(e)) {
            String owner = flags.getFlagOwner(current);
            if (owner != null) {
                Team ownerTeam = teams.getTeam(owner);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    flags.onFlagPickedUp(owner, p);
                    plugin.getFlagCarrierListener().applyGlowForPlayer(p);
                });
                Msg.broadcast(
                    Component.text(p.getName(), Msg.WHITE)
                        .append(Component.text(" took ", Msg.LIGHT_BLUE))
                        .append(ownerTeam != null ? Msg.teamName(ownerTeam) : Component.text(owner, Msg.LIGHT_BLUE))
                        .append(Component.text("'s Flag from ", Msg.LIGHT_BLUE))
                        .append(Msg.teamName(vault))
                        .append(Component.text("'s vault!", Msg.LIGHT_BLUE)));
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
        ItemStack dragged = e.getOldCursor();

        // TB flag drag: only allowed into own team vault
        if (plugin.getTieBreakManager().isTieBreakFlag(dragged)) {
            if (!isOwnTeamVault(e.getInventory(), dragger)) {
                e.setCancelled(true);
                return;
            }
            UUID flagId = plugin.getTieBreakManager().getTBFlagUUID(dragged);
            if (flagId != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getTieBreakManager().onTBInVault(flagId);
                    plugin.getFlagCarrierListener().applyGlowForPlayer(dragger);
                });
            }
            return;
        }

        if (!flags.isFlag(dragged)) return;

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
        String owner = flags.getFlagOwner(dragged);
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
        // Block hopper movement of TB flags entirely
        if (plugin.getTieBreakManager().isTieBreakFlag(e.getItem())) {
            e.setCancelled(true);
            return;
        }
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
        Team ownerTeam = teams.getTeam(owner);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            flags.onFlagStoredInVault(owner, vault.getName());
            plugin.getFlagCarrierListener().applyGlowForPlayer(p);
        });
        Msg.broadcast(
            Component.text(p.getName(), Msg.WHITE)
                .append(Component.text(" deposited ", Msg.LIGHT_BLUE))
                .append(ownerTeam != null ? Msg.teamName(ownerTeam) : Component.text(owner, Msg.LIGHT_BLUE))
                .append(Component.text("'s Flag in ", Msg.LIGHT_BLUE))
                .append(Msg.teamName(vault))
                .append(Component.text("'s vault!", Msg.LIGHT_BLUE)));
    }

    private boolean isAnyFlag(ItemStack item) {
        return flags.isFlag(item) || plugin.getTieBreakManager().isTieBreakFlag(item);
    }

    private boolean inventoryHasAnyFlag(org.bukkit.inventory.Inventory inv) {
        var tbm = plugin.getTieBreakManager();
        for (ItemStack item : inv.getContents()) {
            if (flags.isFlag(item) || tbm.isTieBreakFlag(item)) return true;
        }
        return false;
    }

    private boolean isTBVaultInventory(org.bukkit.inventory.Inventory inv) {
        if (!(inv.getHolder() instanceof BlockInventoryHolder bih)) return false;
        return plugin.getTieBreakManager().isVaultBlock(bih.getBlock());
    }

    private void trackTBFlagDeposit(InventoryClickEvent e, Team vault, Player p) {
        var tbm = plugin.getTieBreakManager();
        UUID flagId = null;
        // Cursor click with TB flag into vault slot
        if (tbm.isTieBreakFlag(e.getCursor()) && e.getClickedInventory() == e.getInventory()) {
            flagId = tbm.getTBFlagUUID(e.getCursor());
        }
        // Shift-click TB flag from player inventory into vault
        else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(p.getInventory())
                && tbm.isTieBreakFlag(e.getCurrentItem())) {
            flagId = tbm.getTBFlagUUID(e.getCurrentItem());
        }
        // Hotbar-swap: hotbar has TB flag being swapped into vault slot
        else if (e.getAction() == InventoryAction.HOTBAR_SWAP
                && e.getClickedInventory() == e.getInventory()
                && !tbm.isTieBreakFlag(e.getCurrentItem())) {
            int slot = e.getHotbarButton();
            if (slot >= 0 && tbm.isTieBreakFlag(p.getInventory().getItem(slot))) {
                flagId = tbm.getTBFlagUUID(p.getInventory().getItem(slot));
            }
        }
        if (flagId == null) return;
        final UUID id = flagId;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            tbm.onTBInVault(id);
            plugin.getFlagCarrierListener().applyGlowForPlayer(p);
        });
        Msg.broadcast(
            Component.text(p.getName(), Msg.WHITE)
                .append(Component.text(" deposited the ", Msg.LIGHT_BLUE))
                .append(Component.text("Tie-Breaking Flag", Msg.WHITE).decorate(TextDecoration.BOLD))
                .append(Component.text(" in ", Msg.LIGHT_BLUE))
                .append(Msg.teamName(vault))
                .append(Component.text("'s vault!", Msg.LIGHT_BLUE)));
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
