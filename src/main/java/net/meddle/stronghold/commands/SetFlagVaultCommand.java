package net.meddle.stronghold.commands;

import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.team.Team;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SetFlagVaultCommand implements CommandExecutor, TabCompleter {

    private final Stronghold plugin;

    public SetFlagVaultCommand(Stronghold plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Team team = plugin.getTeamManager().getTeamOf(p.getUniqueId());
        if (team == null) {
            p.sendMessage(plugin.getCfg().msg("not_in_team"));
            return true;
        }
        if (!team.isCaptain(p.getUniqueId())) {
            p.sendMessage(plugin.getCfg().msg("not_captain"));
            return true;
        }
        if (team.isVaultLocked()) {
            p.sendMessage(plugin.getCfg().msg("vault_locked"));
            return true;
        }

        Block target = p.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.BARREL) {
            p.sendMessage(plugin.getCfg().msg("vault_not_barrel"));
            return true;
        }

        team.setVault(target.getLocation());
        plugin.getTeamManager().save(team);

        plugin.audit(p.getName(), "Set vault for " + team.getName() + " at " +
            target.getWorld().getName() + " " + target.getX() + "," + target.getY() + "," + target.getZ());

        p.sendMessage(plugin.getCfg().msg("vault_set").replace("{team}", team.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
