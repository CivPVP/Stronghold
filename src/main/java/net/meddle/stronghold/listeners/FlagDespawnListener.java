package net.meddle.stronghold.listeners;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.team.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class FlagDespawnListener implements Listener {

    private final Stronghold plugin;
    private final FlagManager flags;

    public FlagDespawnListener(Stronghold plugin) {
        this.plugin = plugin;
        this.flags  = plugin.getFlagManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        Item item = e.getEntity();
        if (!flags.isFlag(item.getItemStack())) return;

        // Cancel the despawn — we'll handle it ourselves
        e.setCancelled(true);

        String owner = flags.getFlagOwner(item.getItemStack());
        if (owner == null) { item.remove(); return; }

        FlagRecord r = flags.getRecord(owner);
        if (r == null) { item.remove(); return; }

        // Determine target vault: the vault it was last stored in
        String vaultTeamName = r.getLastVaultTeam();
        Team vaultTeam = plugin.getTeamManager().getTeam(vaultTeamName);
        if (vaultTeam == null || !vaultTeam.isVaultSet()) {
            // Fallback to owner's vault
            vaultTeam = plugin.getTeamManager().getTeam(owner);
        }

        item.remove();

        if (vaultTeam == null || !vaultTeam.isVaultSet()) {
            plugin.getLogger().warning("[Flag] " + owner + "'s flag despawned but no vault to return to.");
            return;
        }

        Location vaultLoc = vaultTeam.getVaultLocation();
        if (vaultLoc == null) return;

        // Recreate barrel if missing
        Block b = vaultLoc.getBlock();
        if (b.getType() != Material.BARREL) {
            b.setType(Material.BARREL);
            plugin.getLogger().info("[Flag] Recreated barrel for " + vaultTeam.getName() + "'s vault.");
        }

        // Insert flag into barrel
        org.bukkit.block.Barrel barrel = (org.bukkit.block.Barrel) b.getState();
        barrel.getInventory().addItem(flags.createFlagItem(plugin.getTeamManager().getTeam(owner)));

        r.setInVault(vaultTeam.getName());
        flags.saveAll();

        Team ownerTeam = plugin.getTeamManager().getTeam(owner);
        Component returnMsg = (ownerTeam != null ? Msg.teamName(ownerTeam) : Component.text(owner, Msg.LIGHT_BLUE))
            .append(Component.text("'s Flag has returned to its vault.", Msg.LIGHT_BLUE));
        Msg.broadcast(returnMsg);
        plugin.getLogger().info("[Flag] " + owner + "'s flag returned to " + vaultTeam.getName() + "'s vault (despawn).");
    }
}
