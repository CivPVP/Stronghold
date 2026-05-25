package me.neznamy.tab.shared.features.header;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeaderFooter extends RefreshableFeature implements HeaderFooterManager, JoinListener, Loadable, UnLoadable, CustomThreaded {
   private final StringToComponentCache headerCache = new StringToComponentCache("Header", 1000);
   private final StringToComponentCache footerCache = new StringToComponentCache("Footer", 1000);
   private final ThreadExecutor customThread = new ThreadExecutor("TAB Header/Footer Thread");
   private final Map<String, HeaderFooterDesign> registeredDesigns = new LinkedHashMap<>();
   private HeaderFooterDesign[] definedDesigns;
   @NonNull
   private final HeaderFooterConfiguration configuration;

   @Override
   public void load() {
      for (Entry<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> entry : this.configuration.getDesigns().entrySet()) {
         String designName = entry.getKey();
         HeaderFooterDesign design = new HeaderFooterDesign(this, designName, entry.getValue());
         this.registeredDesigns.put(designName, design);
         TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.design(designName), design);
      }

      this.definedDesigns = this.registeredDesigns.values().toArray(new HeaderFooterDesign[0]);

      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(p);
      }

      TAB.getInstance().getCpu().getTablistEntryCheckThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
         for (TabPlayer px : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList)px.getTabList()).checkHeaderFooter();
         }
      }, this.getFeatureName(), "Periodic task"), 500);
   }

   @Override
   public void unload() {
      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         if (p.headerFooterData.activeDesign != null) {
            p.getTabList().setPlayerListHeaderFooter(null, null);
         }
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.sendHighestDesign(connectedPlayer);
   }

   private void sendHighestDesign(@NotNull TabPlayer player) {
      HeaderFooterDesign highest = this.detectHighestDesign(player);
      HeaderFooterDesign current = player.headerFooterData.activeDesign;
      if (highest != current) {
         player.headerFooterData.activeDesign = highest;
         if (highest != null) {
            this.sendHeaderFooter(player);
         } else {
            player.getTabList().setPlayerListHeaderFooter(null, null);
         }
      }
   }

   @Nullable
   private HeaderFooterDesign detectHighestDesign(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      for (HeaderFooterDesign design : this.definedDesigns) {
         if (design.isConditionMet(p)) {
            return design;
         }
      }

      return null;
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Switching designs";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      this.sendHighestDesign(p);
   }

   public void sendHeaderFooter(@NotNull TabPlayer player) {
      String header;
      if (player.headerFooterData.forcedHeader != null) {
         header = player.headerFooterData.forcedHeader.updateAndGet();
      } else if (player.headerFooterData.activeDesign != null) {
         Property prop = player.headerFooterData.headerProperties.get(player.headerFooterData.activeDesign);
         if (prop == null) {
            prop = new Property(
               player.headerFooterData.activeDesign, player, String.join("\n", player.headerFooterData.activeDesign.getDefinition().getHeader())
            );
            player.headerFooterData.headerProperties.put(player.headerFooterData.activeDesign, prop);
         }

         header = prop.updateAndGet();
      } else {
         header = "";
      }

      String footer;
      if (player.headerFooterData.forcedFooter != null) {
         footer = player.headerFooterData.forcedFooter.updateAndGet();
      } else if (player.headerFooterData.activeDesign != null) {
         Property prop = player.headerFooterData.footerProperties.get(player.headerFooterData.activeDesign);
         if (prop == null) {
            prop = new Property(
               player.headerFooterData.activeDesign, player, String.join("\n", player.headerFooterData.activeDesign.getDefinition().getFooter())
            );
            player.headerFooterData.footerProperties.put(player.headerFooterData.activeDesign, prop);
         }

         footer = prop.updateAndGet();
      } else {
         footer = "";
      }

      player.getTabList().setPlayerListHeaderFooter(this.headerCache.get(header), this.footerCache.get(footer));
   }

   @Override
   public void setHeader(@NotNull TabPlayer p, @Nullable String header) {
      this.ensureActive();
      this.customThread.execute(() -> {
         TabPlayer player = (TabPlayer)p;
         if (header != null) {
            player.headerFooterData.forcedHeader = new Property(this, player, header);
         } else {
            player.headerFooterData.forcedHeader = null;
         }

         this.sendHeaderFooter(player);
      });
   }

   @Override
   public void setFooter(@NotNull TabPlayer p, @Nullable String footer) {
      this.ensureActive();
      this.customThread.execute(() -> {
         TabPlayer player = (TabPlayer)p;
         if (footer != null) {
            player.headerFooterData.forcedFooter = new Property(this, player, footer);
         } else {
            player.headerFooterData.forcedFooter = null;
         }

         this.sendHeaderFooter(player);
      });
   }

   @Override
   public void setHeaderAndFooter(@NotNull TabPlayer p, @Nullable String header, @Nullable String footer) {
      this.ensureActive();
      this.customThread.execute(() -> {
         TabPlayer player = (TabPlayer)p;
         if (header != null) {
            player.headerFooterData.forcedHeader = new Property(this, player, header);
         } else {
            player.headerFooterData.forcedHeader = null;
         }

         if (footer != null) {
            player.headerFooterData.forcedFooter = new Property(this, player, footer);
         } else {
            player.headerFooterData.forcedFooter = null;
         }

         this.sendHeaderFooter(player);
      });
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Header/Footer";
   }

   @Generated
   public HeaderFooter(@NonNull HeaderFooterConfiguration configuration) {
      if (configuration == null) {
         throw new NullPointerException("configuration is marked non-null but is null");
      }

      this.configuration = configuration;
   }

   @Generated
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }
}
