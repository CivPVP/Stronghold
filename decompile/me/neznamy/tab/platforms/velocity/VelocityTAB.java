package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import java.nio.file.Path;
import lombok.Generated;
import me.neznamy.tab.libs.org.bstats.velocity.Metrics;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Plugin(
   id = "tab",
   name = "TAB",
   version = "5.4.0",
   description = "An all-in-one solution that works",
   url = "https://github.com/NEZNAMY/TAB",
   authors = "NEZNAMY",
   dependencies = @Dependency(id = "velocity-scoreboard-api", optional = true)
)
public class VelocityTAB {
   @Inject
   private ProxyServer server;
   @Inject
   private Metrics.Factory metricsFactory;
   @Inject
   private Logger logger;
   @Inject
   @DataDirectory
   private Path dataFolder;

   @Subscribe
   public void onProxyInitialization(@Nullable ProxyInitializeEvent event) {
      try {
         TabListEntry.class.getMethod("setShowHat", boolean.class);
         TAB.create(new VelocityPlatform(this));
      } catch (ReflectiveOperationException e) {
         this.logger.warn("====================================================================================================");
         this.logger.warn("The plugin requires Velocity build #485 (released on March 30th, 2025) and up to work.");
         this.logger.warn("====================================================================================================");
      }
   }

   @Subscribe
   public void onProxyShutdown(@Nullable ProxyShutdownEvent event) {
      if (TAB.getInstance() != null) {
         TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().unload());
      }
   }

   @Generated
   public ProxyServer getServer() {
      return this.server;
   }

   @Generated
   public Metrics.Factory getMetricsFactory() {
      return this.metricsFactory;
   }

   @Generated
   public Logger getLogger() {
      return this.logger;
   }

   @Generated
   public Path getDataFolder() {
      return this.dataFolder;
   }
}
