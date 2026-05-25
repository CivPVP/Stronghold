package net.meddle.stronghold.commands;

import net.kyori.adventure.text.Component;
import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class FlagCommand implements CommandExecutor, TabCompleter {

    private final Stronghold plugin;

    public FlagCommand(Stronghold plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /flag <team>");
            return true;
        }

        Team team = plugin.getTeamManager().getTeam(args[0]);
        if (team == null) {
            sender.sendMessage(plugin.getCfg().msg("team_not_found").replace("{team}", args[0]));
            return true;
        }

        FlagRecord r = plugin.getFlagManager().getRecord(team.getName());
        if (r == null) {
            sender.sendMessage(
                Msg.teamName(team).append(Component.text("'s flag is not currently deployed.", Msg.LIGHT_BLUE))
            );
            return true;
        }

        Component teamPart = Msg.teamName(team).append(Component.text("'s flag — ", Msg.LIGHT_BLUE));
        Component detail;

        switch (r.getState()) {
            case IN_VAULT -> {
                Team vaultTeam = plugin.getTeamManager().getTeam(r.getLastVaultTeam());
                String vaultName = vaultTeam != null ? vaultTeam.getName() + "'s vault" : r.getLastVaultTeam() + "'s vault";
                if (vaultTeam != null && vaultTeam.isVaultSet()) {
                    var loc = vaultTeam.getVaultLocation();
                    detail = Component.text("in " + vaultName + " at ", Msg.LIGHT_BLUE)
                        .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                            + " (" + loc.getWorld().getName() + ")", Msg.WHITE));
                } else {
                    detail = Component.text("in " + vaultName, Msg.LIGHT_BLUE);
                }
            }
            case HELD -> {
                Player carrier = r.getHolderUUID() != null ? Bukkit.getPlayer(r.getHolderUUID()) : null;
                String name = carrier != null ? carrier.getName()
                    : (r.getHolderName() != null ? r.getHolderName() : "Unknown");
                if (carrier != null) {
                    var loc = carrier.getLocation();
                    detail = Component.text("carried by ", Msg.LIGHT_BLUE)
                        .append(Component.text(name, Msg.WHITE))
                        .append(Component.text(" at ", Msg.LIGHT_BLUE))
                        .append(Component.text(loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                            + " (" + loc.getWorld().getName() + ")", Msg.WHITE));
                } else {
                    detail = Component.text("last seen carried by ", Msg.LIGHT_BLUE)
                        .append(Component.text(name + " (offline)", Msg.WHITE));
                }
            }
            case DROPPED -> {
                detail = Component.text("dropped at ", Msg.LIGHT_BLUE)
                    .append(Component.text((int) r.getDroppedX() + ", " + (int) r.getDroppedY()
                        + ", " + (int) r.getDroppedZ() + " (" + r.getDroppedWorld() + ")", Msg.WHITE));
            }
            default -> detail = Component.text("unknown location", Msg.LIGHT_BLUE);
        }

        sender.sendMessage(teamPart.append(detail));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return plugin.getTeamManager().getAllTeams().stream()
                .map(Team::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
}
