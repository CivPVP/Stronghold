package net.meddle.stronghold.commands;

import net.meddle.stronghold.Msg;
import net.meddle.stronghold.Stronghold;
import net.meddle.stronghold.config.ConfigManager;
import net.meddle.stronghold.event.EventPhase;
import net.meddle.stronghold.flag.FlagRecord;
import net.meddle.stronghold.flag.FlagState;
import net.meddle.stronghold.team.Team;
import net.meddle.stronghold.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class StrongholdCommand implements CommandExecutor, TabCompleter {

    private final Stronghold plugin;

    private static final List<String> TOP_LEVEL = List.of(
        "newteam", "deleteteam", "renameteam", "recolorteam",
        "setcaptain", "addmember", "removemember",
        "setvault", "giveflag", "returnflag", "resetflag",
        "start", "pause", "resume", "stop", "reset",
        "status", "inspect", "tpvault", "listteams",
        "backup", "restore", "repair", "reload",
        "addtbvault", "removetbvault", "listtbvaults"
    );

    private static final List<String> DYE_COLORS = Arrays.stream(DyeColor.values())
        .map(Enum::name).collect(Collectors.toList());

    public StrongholdCommand(Stronghold plugin) {
        this.plugin = plugin;
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(plugin.getCfg().msg("not_op"));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "newteam"     -> cmdNewTeam(sender, args);
            case "deleteteam"  -> cmdDeleteTeam(sender, args);
            case "renameteam"  -> cmdRenameTeam(sender, args);
            case "recolorteam" -> cmdRecolorTeam(sender, args);
            case "setcaptain"  -> cmdSetCaptain(sender, args);
            case "addmember"   -> cmdAddMember(sender, args);
            case "removemember"-> cmdRemoveMember(sender, args);
            case "setvault"    -> cmdSetVault(sender, args);
            case "giveflag"    -> cmdGiveFlag(sender, args);
            case "returnflag"  -> cmdReturnFlag(sender, args);
            case "resetflag"   -> cmdResetFlag(sender, args);
            case "start"       -> cmdStart(sender, args);
            case "pause"       -> cmdPause(sender);
            case "resume"      -> cmdResume(sender);
            case "stop"        -> cmdStop(sender);
            case "reset"       -> cmdReset(sender);
            case "status"      -> cmdStatus(sender);
            case "inspect"     -> cmdInspect(sender, args);
            case "tpvault"     -> cmdTpVault(sender, args);
            case "listteams"   -> cmdListTeams(sender);
            case "backup"      -> cmdBackup(sender);
            case "restore"     -> cmdRestore(sender, args);
            case "repair"         -> cmdRepair(sender);
            case "reload"         -> cmdReload(sender);
            case "addtbvault"     -> cmdAddTbVault(sender);
            case "removetbvault"  -> cmdRemoveTbVault(sender, args);
            case "listtbvaults"   -> cmdListTbVaults(sender);
            default            -> sendHelp(sender);
        }
        return true;
    }

    // ── Team commands ─────────────────────────────────────────────────────────

    private void cmdNewTeam(CommandSender s, String[] args) {
        if (args.length < 3) { s.sendMessage("Usage: /stronghold newteam <name> <color>"); return; }
        String name  = args[1];
        String color = args[2].toUpperCase();

        if (!TeamManager.isValidName(name)) { s.sendMessage(plugin.getCfg().msg("invalid_team_name")); return; }
        if (plugin.getTeamManager().exists(name)) { s.sendMessage(plugin.getCfg().msg("team_name_taken")); return; }
        if (plugin.getTeamManager().count() >= plugin.getCfg().getMaxTeams()) {
            s.sendMessage(plugin.getCfg().msg("max_teams_reached").replace("{max}", String.valueOf(plugin.getCfg().getMaxTeams())));
            return;
        }

        DyeColor dye;
        try { dye = DyeColor.valueOf(color); }
        catch (Exception e) { s.sendMessage(plugin.getCfg().msg("invalid_color")); return; }

        Team t = plugin.getTeamManager().createTeam(name, dye);
        plugin.getScoreboardManager().ensureScoreboardTeam(t);
        s.sendMessage(plugin.getCfg().msg("team_created").replace("{team}", name));
        plugin.audit(s.getName(), "Created team " + name + " (" + color + ")");
    }

    private void cmdDeleteTeam(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold deleteteam <team>"); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;

        // Force-return any active flag
        if (plugin.getFlagManager().hasRecord(t.getName()))
            plugin.getFlagManager().returnFlagToVault(t.getName());
        plugin.getFlagManager().removeRecord(t.getName());

        plugin.getScoreboardManager().removeScoreboardTeam(t);
        plugin.getTeamManager().deleteTeam(t.getName());
        s.sendMessage(plugin.getCfg().msg("team_deleted").replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Deleted team " + t.getName());
    }

    private void cmdRenameTeam(CommandSender s, String[] args) {
        if (args.length < 3) { s.sendMessage("Usage: /stronghold renameteam <team> <newName>"); return; }
        if (plugin.getEventManager().getPhase() != EventPhase.IDLE && plugin.getEventManager().getPhase() != EventPhase.ENDED) { s.sendMessage(plugin.getCfg().msg("idle_only")); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;
        String newName = args[2];
        if (!TeamManager.isValidName(newName)) { s.sendMessage(plugin.getCfg().msg("invalid_team_name")); return; }
        if (plugin.getTeamManager().exists(newName)) { s.sendMessage(plugin.getCfg().msg("team_name_taken")); return; }

        String old = t.getName();
        plugin.getScoreboardManager().removeScoreboardTeam(t);
        plugin.getTeamManager().renameTeam(old, newName);
        plugin.getScoreboardManager().ensureScoreboardTeam(t);

        s.sendMessage(plugin.getCfg().msg("team_renamed").replace("{old}", old).replace("{new}", newName));
        plugin.audit(s.getName(), "Renamed team " + old + " to " + newName);
    }

    private void cmdRecolorTeam(CommandSender s, String[] args) {
        if (args.length < 3) { s.sendMessage("Usage: /stronghold recolorteam <team> <color>"); return; }
        if (plugin.getEventManager().getPhase() != EventPhase.IDLE && plugin.getEventManager().getPhase() != EventPhase.ENDED) { s.sendMessage(plugin.getCfg().msg("idle_only")); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;
        DyeColor dye;
        try { dye = DyeColor.valueOf(args[2].toUpperCase()); }
        catch (Exception e) { s.sendMessage(plugin.getCfg().msg("invalid_color")); return; }

        t.setColor(dye);
        plugin.getTeamManager().save(t);
        plugin.getScoreboardManager().ensureScoreboardTeam(t);

        s.sendMessage(plugin.getCfg().msg("team_recolored").replace("{team}", t.getName()).replace("{color}", dye.name()));
        plugin.audit(s.getName(), "Recolored team " + t.getName() + " to " + dye.name());
    }

    private void cmdSetCaptain(CommandSender s, String[] args) {
        if (args.length < 3) { s.sendMessage("Usage: /stronghold setcaptain <team> <player>"); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);
        if (!t.hasMember(op.getUniqueId())) {
            s.sendMessage(plugin.getCfg().msg("player_not_in_team").replace("{team}", t.getName())); return;
        }
        t.setCaptain(op.getUniqueId());
        plugin.getTeamManager().save(t);
        s.sendMessage(plugin.getCfg().msg("captain_set").replace("{player}", args[2]).replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Set captain of " + t.getName() + " to " + args[2]);
    }

    private void cmdAddMember(CommandSender s, String[] args) {
        if (args.length < 3) { s.sendMessage("Usage: /stronghold addmember <team> <player>"); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);

        if (t.hasMember(op.getUniqueId())) {
            s.sendMessage(plugin.getCfg().msg("player_already_in_team").replace("{team}", t.getName())); return;
        }
        Team other = plugin.getTeamManager().getTeamOf(op.getUniqueId());
        if (other != null) {
            s.sendMessage(plugin.getCfg().msg("player_in_other_team")); return;
        }

        t.addMember(op.getUniqueId());
        plugin.getTeamManager().save(t);

        Player online = Bukkit.getPlayer(op.getUniqueId());
        if (online != null) plugin.getScoreboardManager().addPlayer(online, t);

        s.sendMessage(plugin.getCfg().msg("member_added").replace("{player}", args[2]).replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Added " + args[2] + " to " + t.getName());
    }

    private void cmdRemoveMember(CommandSender s, String[] args) {
        if (args.length < 3) { s.sendMessage("Usage: /stronghold removemember <team> <player>"); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;
        OfflinePlayer op = Bukkit.getOfflinePlayer(args[2]);

        if (!t.hasMember(op.getUniqueId())) {
            s.sendMessage(plugin.getCfg().msg("player_not_in_team").replace("{team}", t.getName())); return;
        }

        t.removeMember(op.getUniqueId());
        plugin.getTeamManager().save(t);

        Player online = Bukkit.getPlayer(op.getUniqueId());
        if (online != null) plugin.getScoreboardManager().removePlayerFromTeam(online, t);

        s.sendMessage(plugin.getCfg().msg("member_removed").replace("{player}", args[2]).replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Removed " + args[2] + " from " + t.getName());
    }

    // ── Vault commands ────────────────────────────────────────────────────────

    private void cmdSetVault(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold setvault <team>"); return; }
        if (!(s instanceof Player p)) { s.sendMessage("Must be a player."); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;

        Block target = p.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.BARREL) {
            s.sendMessage(plugin.getCfg().msg("vault_not_barrel")); return;
        }

        // Transfer flags from old vault to new vault
        if (t.isVaultSet()) {
            transferFlagsToNewVault(t, target);
        }

        t.setVault(target.getLocation());
        plugin.getTeamManager().save(t);
        s.sendMessage(plugin.getCfg().msg("vault_changed").replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Moved vault for " + t.getName() + " to " +
            target.getWorld().getName() + " " + target.getX() + "," + target.getY() + "," + target.getZ());
    }

    private void transferFlagsToNewVault(Team t, Block newBarrel) {
        var oldLoc = t.getVaultLocation();
        if (oldLoc == null) return;
        Block oldBlock = oldLoc.getBlock();
        if (oldBlock.getType() != Material.BARREL) return;

        var oldBarrel = (org.bukkit.block.Barrel) oldBlock.getState();
        var newBarrelState = (org.bukkit.block.Barrel) newBarrel.getState();

        for (ItemStack item : oldBarrel.getInventory().getContents()) {
            if (item == null) continue;
            if (!plugin.getFlagManager().isFlag(item)) continue;
            oldBarrel.getInventory().remove(item);
            newBarrelState.getInventory().addItem(item);
        }
    }

    // ── Flag admin commands ───────────────────────────────────────────────────

    private void cmdGiveFlag(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold giveflag <team>"); return; }
        if (!(s instanceof Player p)) { s.sendMessage("Must be a player."); return; }
        if (!requireActivePhase(s)) return;
        Team t = requireTeam(s, args[1]); if (t == null) return;

        // Capture former holder before state changes
        FlagRecord r = plugin.getFlagManager().getRecord(t.getName());
        java.util.UUID formerHolder = (r != null && r.getState() == FlagState.HELD) ? r.getHolderUUID() : null;

        plugin.getFlagManager().removeFlagFromWorld(t.getName());
        ItemStack flag = plugin.getFlagManager().createFlagItem(t);
        p.getInventory().addItem(flag);

        if (r != null) r.setHeld(p.getUniqueId(), p.getName());
        plugin.getFlagManager().saveAll();

        // Clear glow for former holder, apply for op
        if (formerHolder != null) {
            org.bukkit.entity.Player fh = org.bukkit.Bukkit.getPlayer(formerHolder);
            if (fh != null) plugin.getFlagCarrierListener().applyGlowForPlayer(fh);
        }
        plugin.getFlagCarrierListener().applyGlowForPlayer(p);

        s.sendMessage(plugin.getCfg().msg("flag_given").replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Took " + t.getName() + "'s flag into hand");
    }

    private void cmdReturnFlag(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold returnflag <team>"); return; }
        if (!requireActivePhase(s)) return;
        Team t = requireTeam(s, args[1]); if (t == null) return;

        FlagRecord r = plugin.getFlagManager().getRecord(t.getName());
        java.util.UUID formerHolder = (r != null && r.getState() == FlagState.HELD) ? r.getHolderUUID() : null;

        plugin.getFlagManager().returnFlagToVault(t.getName());

        if (formerHolder != null) {
            org.bukkit.entity.Player fh = org.bukkit.Bukkit.getPlayer(formerHolder);
            if (fh != null) plugin.getFlagCarrierListener().applyGlowForPlayer(fh);
        }

        s.sendMessage(plugin.getCfg().msg("flag_returned_admin").replace("{team}", t.getName()));
        Msg.broadcast(Msg.teamName(t).append(net.kyori.adventure.text.Component.text("'s Flag has returned to its vault.", Msg.LIGHT_BLUE)));
        plugin.audit(s.getName(), "Force-returned " + t.getName() + "'s flag to vault");
    }

    private void cmdResetFlag(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold resetflag <team>"); return; }
        if (!requireActivePhase(s)) return;
        Team t = requireTeam(s, args[1]); if (t == null) return;
        if (!t.isVaultSet()) { s.sendMessage(plugin.getCfg().msg("no_vault").replace("{team}", t.getName())); return; }

        FlagRecord r = plugin.getFlagManager().getRecord(t.getName());
        java.util.UUID formerHolder = (r != null && r.getState() == FlagState.HELD) ? r.getHolderUUID() : null;

        // Remove existing flag from world
        plugin.getFlagManager().removeFlagFromWorld(t.getName());
        plugin.getFlagManager().removeRecord(t.getName());

        if (formerHolder != null) {
            org.bukkit.entity.Player fh = org.bukkit.Bukkit.getPlayer(formerHolder);
            if (fh != null) plugin.getFlagCarrierListener().applyGlowForPlayer(fh);
        }

        // Spawn fresh flag
        plugin.getFlagManager().spawnFlagInVault(t);
        s.sendMessage(plugin.getCfg().msg("flag_reset").replace("{team}", t.getName()));
        plugin.audit(s.getName(), "Reset flag for " + t.getName());
    }

    // ── Event commands ────────────────────────────────────────────────────────

    private void cmdStart(CommandSender s, String[] args) {
        EventPhase ph = plugin.getEventManager().getPhase();
        if (ph != EventPhase.IDLE && ph != EventPhase.ENDED) {
            s.sendMessage(plugin.getCfg().msg("event_already_running")); return;
        }
        String durStr = args.length >= 2 ? args[1] : plugin.getCfg().getDefaultCountdownDuration();
        long ms = ConfigManager.parseDurationMs(durStr);
        if (ms <= 0) { s.sendMessage("Invalid duration. Example: 1d12h30m"); return; }

        plugin.getEventManager().startCountdown(ms);
        plugin.audit(s.getName(), "Started event countdown: " + durStr);
    }

    private void cmdPause(CommandSender s) {
        if (!plugin.getEventManager().pause()) {
            s.sendMessage(plugin.getCfg().msg("event_not_running")); return;
        }
        plugin.audit(s.getName(), "Paused event");
    }

    private void cmdResume(CommandSender s) {
        if (!plugin.getEventManager().resumeFromPause()) {
            s.sendMessage(plugin.getCfg().msg("event_not_paused")); return;
        }
        plugin.audit(s.getName(), "Resumed event");
    }

    private void cmdStop(CommandSender s) {
        EventPhase ph = plugin.getEventManager().getPhase();
        if (ph == EventPhase.IDLE || ph == EventPhase.ENDED) {
            s.sendMessage(plugin.getCfg().msg("event_not_running")); return;
        }
        plugin.getEventManager().stopEvent();
        plugin.audit(s.getName(), "Stopped event");
    }

    private void cmdReset(CommandSender s) {
        plugin.getEventManager().resetEvent();
        plugin.audit(s.getName(), "Reset event");
    }

    // ── Info commands ─────────────────────────────────────────────────────────

    private void cmdStatus(CommandSender s) {
        EventPhase ph = plugin.getEventManager().getPhase();
        s.sendMessage("§6[Stronghold Status]");
        s.sendMessage("§7Phase: §f" + ph.name());

        long now = System.currentTimeMillis();
        switch (ph) {
            case COUNTDOWN -> s.sendMessage("§7Deploy in: §f" + ConfigManager.formatDuration(plugin.getEventManager().getCountdownEnd() - now));
            case ACTIVE    -> s.sendMessage("§7Ends in: §f"   + ConfigManager.formatDuration(plugin.getEventManager().getEndgameEnd() - now));
            case PAUSED    -> s.sendMessage("§7Remaining: §f" + ConfigManager.formatDuration(plugin.getEventManager().getPausedRemainingMs()));
            default -> {}
        }

        s.sendMessage("§7Flags:");
        for (FlagRecord r : plugin.getFlagManager().getAllRecords()) {
            String state = switch (r.getState()) {
                case IN_VAULT -> "§aIn vault (" + r.getLastVaultTeam() + ")";
                case HELD     -> "§eHeld by " + r.getHolderName();
                case DROPPED  -> "§cDropped at " + (int)r.getDroppedX() + "," + (int)r.getDroppedY() + "," + (int)r.getDroppedZ() + " in " + r.getDroppedWorld();
            };
            s.sendMessage("  §f" + r.getOwnerTeam() + "§7: " + state);
        }
    }

    private void cmdInspect(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold inspect <team>"); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;

        s.sendMessage("§6[Team: " + t.getName() + "]");
        s.sendMessage("§7Color: §f" + t.getColor().name());

        String cap = t.getCaptain() != null ? Bukkit.getOfflinePlayer(t.getCaptain()).getName() : "None";
        s.sendMessage("§7Captain: §f" + cap);
        s.sendMessage("§7Members (" + t.getMembers().size() + "):");
        for (UUID uid : t.getMembers()) {
            String name = Bukkit.getOfflinePlayer(uid).getName();
            s.sendMessage("  §f" + (name != null ? name : uid.toString()));
        }
        if (t.isVaultSet()) {
            s.sendMessage("§7Vault: §f" + t.getVaultWorld() + " " + t.getVaultX() + "," + t.getVaultY() + "," + t.getVaultZ()
                + (t.isVaultLocked() ? " §c[LOCKED]" : " §a[unlocked]"));
        } else {
            s.sendMessage("§7Vault: §cNot set");
        }
        FlagRecord r = plugin.getFlagManager().getRecord(t.getName());
        if (r != null) {
            String state = switch (r.getState()) {
                case IN_VAULT -> "§aIn vault (" + r.getLastVaultTeam() + ")";
                case HELD     -> "§eHeld by " + r.getHolderName();
                case DROPPED  -> "§cDropped";
            };
            s.sendMessage("§7Flag: " + state);
        } else {
            s.sendMessage("§7Flag: §7Not deployed");
        }
        s.sendMessage("§7Score: §f" + plugin.getFlagManager().computeScore(t));
    }

    private void cmdTpVault(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold tpvault <team>"); return; }
        if (!(s instanceof Player p)) { s.sendMessage("Must be a player."); return; }
        Team t = requireTeam(s, args[1]); if (t == null) return;
        if (!t.isVaultSet()) { s.sendMessage("§cTeam has no vault set."); return; }
        var loc = t.getVaultLocation();
        if (loc == null) { s.sendMessage("§cVault world not loaded."); return; }
        p.teleport(loc.add(0.5, 1, 0.5));
        s.sendMessage("§aTeleported to " + t.getName() + "'s vault.");
    }

    private void cmdListTeams(CommandSender s) {
        var teams = plugin.getTeamManager().getAllTeams();
        if (teams.isEmpty()) { s.sendMessage("§7No teams."); return; }
        s.sendMessage("§6Teams (" + teams.size() + "):");
        for (Team t : teams) s.sendMessage("  §f" + t.getName() + " §7(" + t.getColor().name() + ")");
    }

    // ── Backup / Restore ──────────────────────────────────────────────────────

    private void cmdBackup(CommandSender s) {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        backupDir.mkdirs();
        String filename = "backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".zip";
        File zip = new File(backupDir, filename);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            zipFolder(plugin.getDataFolder(), plugin.getDataFolder(), zos, "backups");
        } catch (IOException e) {
            s.sendMessage("§cBackup failed: " + e.getMessage()); return;
        }
        s.sendMessage(plugin.getCfg().msg("backup_created").replace("{file}", filename));
        plugin.audit(s.getName(), "Created backup: " + filename);
    }

    private void cmdRestore(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("Usage: /stronghold restore <filename>"); return; }
        if (plugin.getEventManager().getPhase() != EventPhase.IDLE && plugin.getEventManager().getPhase() != EventPhase.ENDED) { s.sendMessage(plugin.getCfg().msg("idle_only")); return; }

        File zip = new File(new File(plugin.getDataFolder(), "backups"), args[1]);
        if (!zip.exists()) { s.sendMessage("§cBackup file not found: " + args[1]); return; }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File out = new File(plugin.getDataFolder(), entry.getName());
                out.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    zis.transferTo(fos);
                }
            }
        } catch (IOException e) {
            s.sendMessage("§cRestore failed: " + e.getMessage()); return;
        }

        // Reload everything
        plugin.getTeamManager().loadAll();
        plugin.getFlagManager().loadAll();
        plugin.getScoreboardManager().applyAll();

        s.sendMessage(plugin.getCfg().msg("restore_complete").replace("{file}", args[1]));
        plugin.audit(s.getName(), "Restored from backup: " + args[1]);
    }

    private static void zipFolder(File root, File folder, ZipOutputStream zos, String... exclude) throws IOException {
        Set<String> skip = new HashSet<>(Arrays.asList(exclude));
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File f : files) {
            String rel = root.toURI().relativize(f.toURI()).getPath();
            if (skip.contains(rel.split("/")[0])) continue;
            if (f.isDirectory()) {
                zipFolder(root, f, zos, exclude);
            } else {
                zos.putNextEntry(new ZipEntry(rel));
                try (FileInputStream fis = new FileInputStream(f)) { fis.transferTo(zos); }
                zos.closeEntry();
            }
        }
    }

    // ── Repair ────────────────────────────────────────────────────────────────

    private void cmdRepair(CommandSender s) {
        int fixed = 0;

        for (FlagRecord r : plugin.getFlagManager().getAllRecords()) {
            // HELD by offline player → reset to vault
            if (r.getState() == FlagState.HELD && r.getHolderUUID() != null
                    && Bukkit.getPlayer(r.getHolderUUID()) == null) {
                r.setInVault(r.getLastVaultTeam());
                fixed++;
            }

            // IN_VAULT but vault barrel is missing → recreate
            if (r.getState() == FlagState.IN_VAULT) {
                Team vt = plugin.getTeamManager().getTeam(r.getLastVaultTeam());
                if (vt != null && vt.isVaultSet()) {
                    var loc = vt.getVaultLocation();
                    if (loc != null && loc.getBlock().getType() != Material.BARREL) {
                        loc.getBlock().setType(Material.BARREL);
                        var barrel = (org.bukkit.block.Barrel) loc.getBlock().getState();
                        barrel.getInventory().addItem(plugin.getFlagManager().createFlagItem(
                            plugin.getTeamManager().getTeam(r.getOwnerTeam())));
                        fixed++;
                    }
                }
            }
        }

        // Scoreboard: re-add all online players to their teams
        for (Player p : Bukkit.getOnlinePlayers()) {
            Team t = plugin.getTeamManager().getTeamOf(p.getUniqueId());
            if (t != null) plugin.getScoreboardManager().addPlayer(p, t);
        }

        plugin.getFlagManager().saveAll();
        s.sendMessage(plugin.getCfg().msg("repair_complete").replace("{count}", String.valueOf(fixed)));
        plugin.audit(s.getName(), "Ran repair — " + fixed + " issue(s) fixed");
    }

    // ── Reload ────────────────────────────────────────────────────────────────

    private void cmdReload(CommandSender s) {
        plugin.getCfg().reload();
        plugin.getTeamManager().loadAll();
        plugin.getFlagManager().loadAll();
        plugin.getScoreboardManager().applyAll();
        plugin.getFlagCarrierListener().reapplyAllGlow();
        s.sendMessage(plugin.getCfg().msg("reloaded"));
        plugin.audit(s.getName(), "Reloaded configuration");
    }

    // ── Tie-break vault commands ──────────────────────────────────────────────

    private void cmdAddTbVault(CommandSender s) {
        if (!(s instanceof Player p)) { s.sendMessage("§cOnly players can use this command."); return; }
        EventPhase phase = plugin.getEventManager().getPhase();
        if (phase == EventPhase.TIE_BREAK_ACTIVE || phase == EventPhase.ENDED) {
            s.sendMessage("§cTie-breaking flags are already deployed. Vaults can only be added before the tie-break active phase.");
            return;
        }
        Block target = p.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.BARREL) {
            s.sendMessage(plugin.getCfg().msg("vault_not_barrel")); return;
        }
        plugin.getTieBreakManager().addVault(target.getLocation());
        s.sendMessage("§bTie-breaker vault added at " + target.getX() + ", " + target.getY() + ", " + target.getZ() + ".");
        plugin.audit(s.getName(), "Added tie-break vault at " + target.getLocation());
    }

    private void cmdRemoveTbVault(CommandSender s, String[] args) {
        if (args.length < 2) { s.sendMessage("§cUsage: /stronghold removetbvault <index>"); return; }
        int idx;
        try { idx = Integer.parseInt(args[1]) - 1; } // 1-based for players
        catch (NumberFormatException e) { s.sendMessage("§cInvalid index."); return; }
        if (!plugin.getTieBreakManager().removeVault(idx)) {
            s.sendMessage("§cNo vault at that index."); return;
        }
        s.sendMessage("§cTie-breaker vault removed.");
        plugin.audit(s.getName(), "Removed tie-break vault #" + (idx + 1));
    }

    private void cmdListTbVaults(CommandSender s) {
        var vaults = plugin.getTieBreakManager().getVaults();
        if (vaults.isEmpty()) { s.sendMessage("§cNo tie-breaker vaults configured."); return; }
        s.sendMessage("§bTie-breaker vaults (" + vaults.size() + "):");
        for (int i = 0; i < vaults.size(); i++) {
            var v = vaults.get(i);
            s.sendMessage("  §f" + (i + 1) + ". §7" + v.display());
        }
    }

    // ── Tab completion ────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) return Collections.emptyList();

        if (args.length == 1) return filter(TOP_LEVEL, args[0]);

        String sub = args[0].toLowerCase();
        List<String> teamNames = new ArrayList<>(plugin.getTeamManager().getAllTeams().stream()
            .map(Team::getName).toList());

        return switch (sub) {
            case "newteam"                          -> args.length == 3 ? filter(DYE_COLORS, args[2]) : Collections.emptyList();
            case "deleteteam", "setvault", "giveflag",
                 "returnflag", "resetflag", "inspect",
                 "tpvault", "renameteam"            -> args.length == 2 ? filter(teamNames, args[1]) : Collections.emptyList();
            case "recolorteam"                      -> args.length == 2 ? filter(teamNames, args[1]) : args.length == 3 ? filter(DYE_COLORS, args[2]) : Collections.emptyList();
            case "setcaptain", "addmember",
                 "removemember"                     -> args.length == 2 ? filter(teamNames, args[1]) : Collections.emptyList();
            case "start"                            -> args.length == 2 ? List.of("1h", "6h", "12h", "1d", "2d") : Collections.emptyList();
            case "restore"                          -> args.length == 2 ? listBackupFiles(args[1]) : Collections.emptyList();
            default                                 -> Collections.emptyList();
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Team requireTeam(CommandSender s, String name) {
        Team t = plugin.getTeamManager().getTeam(name);
        if (t == null) s.sendMessage(plugin.getCfg().msg("team_not_found").replace("{team}", name));
        return t;
    }

    private boolean requireActivePhase(CommandSender s) {
        EventPhase ph = plugin.getEventManager().getPhase();
        if (ph != EventPhase.ACTIVE && ph != EventPhase.PAUSED) {
            s.sendMessage(plugin.getCfg().msg("active_only"));
            return false;
        }
        return true;
    }

    private static List<String> filter(List<String> list, String prefix) {
        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }

    private List<String> listBackupFiles(String prefix) {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) return Collections.emptyList();
        File[] files = backupDir.listFiles((d, n) -> n.endsWith(".zip") && n.startsWith(prefix));
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("§6§lStronghold Commands:");
        s.sendMessage("§e/stronghold newteam <name> <color>");
        s.sendMessage("§e/stronghold deleteteam|renameteam|recolorteam <team> ...");
        s.sendMessage("§e/stronghold setcaptain|addmember|removemember <team> <player>");
        s.sendMessage("§e/stronghold setvault|giveflag|returnflag|resetflag <team>");
        s.sendMessage("§e/stronghold start [duration] | pause | resume | stop | reset");
        s.sendMessage("§e/stronghold status | inspect <team> | listteams | tpvault <team>");
        s.sendMessage("§e/stronghold backup | restore <file> | repair | reload");
    }
}
