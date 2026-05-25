package me.neznamy.tab.libs.org.bstats.bungeecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import me.neznamy.tab.libs.org.bstats.MetricsBase;
import me.neznamy.tab.libs.org.bstats.charts.CustomChart;
import me.neznamy.tab.libs.org.bstats.json.JsonObjectBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Metrics {
   private final Plugin plugin;
   private final MetricsBase metricsBase;
   private boolean enabled;
   private String serverUUID;
   private boolean logErrors = false;
   private boolean logSentData;
   private boolean logResponseStatusText;

   public Metrics(Plugin plugin, int serviceId) {
      this.plugin = plugin;

      try {
         this.loadConfig();
      } catch (IOException e) {
         plugin.getLogger().log(Level.WARNING, "Failed to load bStats config!", e);
         this.metricsBase = null;
         return;
      }

      this.metricsBase = new MetricsBase(
         "bungeecord",
         this.serverUUID,
         serviceId,
         this.enabled,
         this::appendPlatformData,
         this::appendServiceData,
         null,
         () -> true,
         (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error),
         message -> this.plugin.getLogger().log(Level.INFO, message),
         this.logErrors,
         this.logSentData,
         this.logResponseStatusText,
         false
      );
   }

   private void loadConfig() throws IOException {
      File bStatsFolder = new File(this.plugin.getDataFolder().getParentFile(), "bStats");
      bStatsFolder.mkdirs();
      File configFile = new File(bStatsFolder, "config.yml");
      if (!configFile.exists()) {
         this.writeFile(
            configFile,
            "# bStats (https://bStats.org) collects some basic information for plugin authors, like how",
            "# many people use their plugin and their total player count. It's recommended to keep bStats",
            "# enabled, but if you're not comfortable with this, you can turn this setting off. There is no",
            "# performance penalty associated with having metrics enabled, and data sent to bStats is fully",
            "# anonymous.",
            "enabled: true",
            "serverUuid: \"" + UUID.randomUUID() + "\"",
            "logFailedRequests: false",
            "logSentData: false",
            "logResponseStatusText: false"
         );
      }

      Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
      this.enabled = configuration.getBoolean("enabled", true);
      this.serverUUID = configuration.getString("serverUuid");
      this.logErrors = configuration.getBoolean("logFailedRequests", false);
      this.logSentData = configuration.getBoolean("logSentData", false);
      this.logResponseStatusText = configuration.getBoolean("logResponseStatusText", false);
   }

   private void writeFile(File file, String... lines) throws IOException {
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

      try {
         for (String line : lines) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
         }
      } catch (Throwable var9) {
         try {
            bufferedWriter.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      bufferedWriter.close();
   }

   public void shutdown() {
      this.metricsBase.shutdown();
   }

   public void addCustomChart(CustomChart chart) {
      this.metricsBase.addCustomChart(chart);
   }

   private void appendPlatformData(JsonObjectBuilder builder) {
      builder.appendField("playerAmount", this.plugin.getProxy().getOnlineCount());
      builder.appendField("managedServers", this.plugin.getProxy().getServers().size());
      builder.appendField("onlineMode", this.plugin.getProxy().getConfig().isOnlineMode() ? 1 : 0);
      builder.appendField("bungeecordVersion", this.plugin.getProxy().getVersion());
      builder.appendField("bungeecordName", this.plugin.getProxy().getName());
      builder.appendField("javaVersion", System.getProperty("java.version"));
      builder.appendField("osName", System.getProperty("os.name"));
      builder.appendField("osArch", System.getProperty("os.arch"));
      builder.appendField("osVersion", System.getProperty("os.version"));
      builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
   }

   private void appendServiceData(JsonObjectBuilder builder) {
      builder.appendField("pluginVersion", this.plugin.getDescription().getVersion());
   }
}
