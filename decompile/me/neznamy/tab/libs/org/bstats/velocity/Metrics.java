package me.neznamy.tab.libs.org.bstats.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import me.neznamy.tab.libs.org.bstats.MetricsBase;
import me.neznamy.tab.libs.org.bstats.charts.CustomChart;
import me.neznamy.tab.libs.org.bstats.config.MetricsConfig;
import me.neznamy.tab.libs.org.bstats.json.JsonObjectBuilder;
import org.slf4j.Logger;

public class Metrics {
   private final PluginContainer pluginContainer;
   private final ProxyServer server;
   private MetricsBase metricsBase;

   private Metrics(Object plugin, ProxyServer server, Logger logger, Path dataDirectory, int serviceId) {
      this.pluginContainer = (PluginContainer)server.getPluginManager()
         .fromInstance(plugin)
         .orElseThrow(() -> new IllegalArgumentException("The provided instance is not a plugin"));
      this.server = server;
      File configFile = dataDirectory.getParent().resolve("bStats").resolve("config.txt").toFile();

      MetricsConfig config;
      try {
         config = new MetricsConfig(configFile, true);
      } catch (IOException e) {
         logger.error("Failed to create bStats config", e);
         return;
      }

      this.metricsBase = new MetricsBase(
         "velocity",
         config.getServerUUID(),
         serviceId,
         config.isEnabled(),
         this::appendPlatformData,
         this::appendServiceData,
         task -> server.getScheduler().buildTask(plugin, task).schedule(),
         () -> true,
         logger::warn,
         logger::info,
         config.isLogErrorsEnabled(),
         config.isLogSentDataEnabled(),
         config.isLogResponseStatusTextEnabled(),
         false
      );
      if (!config.didExistBefore()) {
         logger.info("Velocity and some of its plugins collect metrics and send them to bStats (https://bStats.org).");
         logger.info("bStats collects some basic information for plugin authors, like how many people use");
         logger.info("their plugin and their total player count. It's recommend to keep bStats enabled, but");
         logger.info("if you're not comfortable with this, you can opt-out by editing the config.txt file in");
         logger.info("the '/plugins/bStats/' folder and setting enabled to false.");
      }
   }

   public void shutdown() {
      this.metricsBase.shutdown();
   }

   public void addCustomChart(CustomChart chart) {
      if (this.metricsBase != null) {
         this.metricsBase.addCustomChart(chart);
      }
   }

   private void appendPlatformData(JsonObjectBuilder builder) {
      builder.appendField("playerAmount", this.server.getPlayerCount());
      builder.appendField("managedServers", this.server.getAllServers().size());
      builder.appendField("onlineMode", this.server.getConfiguration().isOnlineMode() ? 1 : 0);
      builder.appendField("velocityVersionVersion", this.server.getVersion().getVersion());
      builder.appendField("velocityVersionName", this.server.getVersion().getName());
      builder.appendField("velocityVersionVendor", this.server.getVersion().getVendor());
      builder.appendField("javaVersion", System.getProperty("java.version"));
      builder.appendField("osName", System.getProperty("os.name"));
      builder.appendField("osArch", System.getProperty("os.arch"));
      builder.appendField("osVersion", System.getProperty("os.version"));
      builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
   }

   private void appendServiceData(JsonObjectBuilder builder) {
      builder.appendField("pluginVersion", this.pluginContainer.getDescription().getVersion().orElse("unknown"));
   }

   public static class Factory {
      private final ProxyServer server;
      private final Logger logger;
      private final Path dataDirectory;

      @Inject
      private Factory(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
         this.server = server;
         this.logger = logger;
         this.dataDirectory = dataDirectory;
      }

      public Metrics make(Object plugin, int serviceId) {
         return new Metrics(plugin, this.server, this.logger, this.dataDirectory, serviceId);
      }
   }
}
