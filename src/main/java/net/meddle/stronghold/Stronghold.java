package net.meddle.stronghold;

import net.meddle.stronghold.bossbar.BossBarManager;
import net.meddle.stronghold.commands.FlagCommand;
import net.meddle.stronghold.commands.FlagsCommand;
import net.meddle.stronghold.commands.SetFlagVaultCommand;
import net.meddle.stronghold.commands.StrongholdCommand;
import net.meddle.stronghold.config.ConfigManager;
import net.meddle.stronghold.event.EventManager;
import net.meddle.stronghold.flag.FlagManager;
import net.meddle.stronghold.gui.FlagsGui;
import net.meddle.stronghold.gui.VaultsGui;
import net.meddle.stronghold.commands.VaultsCommand;
import net.meddle.stronghold.listeners.FlagCarrierListener;
import net.meddle.stronghold.listeners.FlagDespawnListener;
import net.meddle.stronghold.listeners.FlagItemListener;
import net.meddle.stronghold.listeners.PlayerSessionListener;
import net.meddle.stronghold.listeners.VaultProtectionListener;
import net.meddle.stronghold.scoreboard.TeamScoreboardManager;
import net.meddle.stronghold.team.TeamManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Stronghold extends JavaPlugin {

    private static Stronghold instance;

    private ConfigManager configManager;
    private TeamManager teamManager;
    private FlagManager flagManager;
    private EventManager eventManager;
    private BossBarManager bossBarManager;
    private TeamScoreboardManager scoreboardManager;
    private FlagsGui flagsGui;
    private VaultsGui vaultsGui;
    private FlagCarrierListener flagCarrierListener;

    private PrintWriter auditWriter;
    private static final DateTimeFormatter AUDIT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager        = new ConfigManager(this);
        teamManager          = new TeamManager(this);
        flagManager          = new FlagManager(this);
        scoreboardManager    = new TeamScoreboardManager(this);
        bossBarManager       = new BossBarManager(this);
        flagCarrierListener  = new FlagCarrierListener(this);
        eventManager         = new EventManager(this);
        flagsGui             = new FlagsGui(this);
        vaultsGui            = new VaultsGui(this);

        openAuditLog();

        teamManager.loadAll();
        flagManager.loadAll();
        scoreboardManager.applyAll();

        registerListeners();
        registerCommands();
        eventManager.resume(); // after listeners so broadcasts/chunk-loads have full plugin context
        flagCarrierListener.startBroadcastTask(); // always-on: broadcasts whenever flags are held

        getLogger().info("Stronghold enabled.");
    }

    @Override
    public void onDisable() {
        if (flagCarrierListener != null) flagCarrierListener.stopBroadcastTask();
        if (eventManager != null) eventManager.shutdown();
        if (bossBarManager != null) bossBarManager.removeAll();
        if (flagManager != null) flagManager.saveAll(); // flush any pending async saves
        if (auditWriter != null) auditWriter.close();
        getLogger().info("Stronghold disabled.");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new VaultProtectionListener(this), this);
        pm.registerEvents(new FlagItemListener(this), this);
        pm.registerEvents(new FlagDespawnListener(this), this);
        pm.registerEvents(new PlayerSessionListener(this), this);
        // FlagCarrierListener is NOT an event listener — it manages a scheduled broadcast task
    }

    private void registerCommands() {
        var sh = new StrongholdCommand(this);
        getCommand("stronghold").setExecutor(sh);
        getCommand("stronghold").setTabCompleter(sh);

        var sfv = new SetFlagVaultCommand(this);
        getCommand("setflagvault").setExecutor(sfv);
        getCommand("setflagvault").setTabCompleter(sfv);

        var fg = new FlagCommand(this);
        getCommand("flag").setExecutor(fg);
        getCommand("flag").setTabCompleter(fg);

        var fl = new FlagsCommand(this);
        getCommand("flags").setExecutor(fl);
        getCommand("flags").setTabCompleter(fl);

        var vl = new VaultsCommand(this);
        getCommand("vaults").setExecutor(vl);
        getCommand("vaults").setTabCompleter(vl);
    }

    // ── Audit log ─────────────────────────────────────────────────────────────

    private void openAuditLog() {
        File f = new File(getDataFolder(), "audit.log");
        try {
            f.getParentFile().mkdirs();
            auditWriter = new PrintWriter(new FileWriter(f, true), true);
        } catch (IOException e) {
            getLogger().warning("Could not open audit.log: " + e.getMessage());
        }
    }

    public void audit(String actor, String action) {
        if (auditWriter != null) {
            auditWriter.println("[" + LocalDateTime.now().format(AUDIT_FMT) + "] " + actor + ": " + action);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static Stronghold get()                       { return instance; }
    public ConfigManager          getCfg()               { return configManager; }
    public TeamManager            getTeamManager()       { return teamManager; }
    public FlagManager            getFlagManager()       { return flagManager; }
    public EventManager           getEventManager()      { return eventManager; }
    public BossBarManager         getBossBarManager()    { return bossBarManager; }
    public TeamScoreboardManager  getScoreboardManager() { return scoreboardManager; }
    public FlagsGui               getFlagsGui()          { return flagsGui; }
    public VaultsGui              getVaultsGui()         { return vaultsGui; }
    public FlagCarrierListener    getFlagCarrierListener(){ return flagCarrierListener; }
}
