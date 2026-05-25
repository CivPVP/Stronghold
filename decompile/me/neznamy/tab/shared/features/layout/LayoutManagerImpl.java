package me.neznamy.tab.shared.features.layout;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.pingspoof.PingSpoof;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.features.types.TabListClearListener;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LayoutManagerImpl
   extends RefreshableFeature
   implements LayoutManager,
   JoinListener,
   QuitListener,
   VanishListener,
   Loadable,
   UnLoadable,
   TabListClearListener {
   private final LayoutConfiguration configuration;
   private final LayoutSkinManager skinManager;
   private final Map<Integer, UUID> uuids = new HashMap<>();
   private final Map<String, LayoutPattern> layouts = new LinkedHashMap<>();
   private final Map<TabPlayer, String> sortedPlayers = Collections.synchronizedMap(new TreeMap<>(Comparator.comparing(p -> p.layoutData.sortingString)));
   private PlayerList playerList;
   private PingSpoof pingSpoof;
   private static boolean teamsEnabled;

   public LayoutManagerImpl(@NotNull LayoutConfiguration configuration) {
      this.configuration = configuration;
      this.skinManager = new LayoutSkinManager(
         TAB.getInstance().getConfiguration().getSkinManager(), configuration.getDefaultSkin(), configuration.getDefaultSkinHashMap()
      );

      for (int slot = 1; slot <= 80; slot++) {
         this.uuids.put(slot, new UUID(0L, configuration.getDirection().translateSlot(slot)));
      }

      for (Entry<String, LayoutConfiguration.LayoutDefinition> entry : configuration.getLayouts().entrySet()) {
         LayoutPattern pattern = new LayoutPattern(this, entry.getKey(), entry.getValue());
         this.layouts.put(pattern.getName(), pattern);
         TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layout(entry.getKey()), pattern);
      }
   }

   @Override
   public void load() {
      this.playerList = TAB.getInstance().getFeatureManager().getFeature("PlayerList");
      this.pingSpoof = TAB.getInstance().getFeatureManager().getFeature("PingSpoof");
      teamsEnabled = TAB.getInstance().getNameTagManager() != null && TAB.getInstance().getPlatform().supportsScoreboards();
      if (this.pingSpoof == null) {
         TAB.getInstance().getFeatureManager().registerFeature("layout-latency", new LayoutLatencyRefresher());
      }

      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(p);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer p) {
      p.layoutData.sortingString = p.sortingData.fullTeamName;
      this.sortedPlayers.put(p, p.sortingData.fullTeamName);
      LayoutPattern highest = this.getHighestLayout(p);
      if (highest != null) {
         LayoutView view = new LayoutView(this, highest, p);
         p.layoutData.currentLayout = new LayoutManagerImpl.LayoutData(view);
         view.send();
      }

      this.tickAllLayouts();
      if (highest != null) {
         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            p.getTabList().updateDisplayName(all, null);
         }
      }
   }

   @Override
   public void onQuit(@NotNull TabPlayer p) {
      this.sortedPlayers.remove(p);

      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         if (all != p && all.layoutData.currentLayout != null) {
            all.layoutData.currentLayout.view.tick();
         }
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Switching layouts";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      LayoutPattern highest = this.getHighestLayout(p);
      LayoutPattern current = p.layoutData.currentLayout == null ? null : p.layoutData.currentLayout.view.getPattern();
      if (highest != current) {
         if (current != null) {
            p.layoutData.currentLayout.view.destroy();
         }

         p.layoutData.currentLayout = null;
         if (highest != null) {
            LayoutView view = new LayoutView(this, highest, p);
            p.layoutData.currentLayout = new LayoutManagerImpl.LayoutData(view);
            view.send();
         }
      }
   }

   @Override
   public void unload() {
      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         if (p.getVersion().getMinorVersion() >= 8 && !p.isBedrockPlayer()) {
            for (UUID id : this.uuids.values()) {
               p.getTabList().removeEntry(id);
            }
         }
      }
   }

   @Override
   public void onVanishStatusChange(@NotNull TabPlayer p) {
      this.tickAllLayouts();
   }

   @Nullable
   private LayoutPattern getHighestLayout(@NotNull TabPlayer p) {
      if (p.layoutData.forcedLayout != null) {
         return p.layoutData.forcedLayout;
      }

      for (LayoutPattern pattern : this.layouts.values()) {
         if (pattern.isConditionMet(p)) {
            return pattern;
         }
      }

      return null;
   }

   @NotNull
   public UUID getUUID(int slot) {
      return this.uuids.get(slot);
   }

   public void updateTeamName(@NotNull TabPlayer p, @NotNull String teamName) {
      this.sortedPlayers.remove(p);
      p.layoutData.sortingString = teamName;
      this.sortedPlayers.put(p, teamName);
      this.tickAllLayouts();
   }

   @Override
   public void onTabListClear(@NotNull TabPlayer player) {
      if (player.layoutData.currentLayout != null) {
         player.layoutData.currentLayout.view.send();
      }
   }

   public void tickAllLayouts() {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         if (all.layoutData.currentLayout != null) {
            all.layoutData.currentLayout.view.tick();
         }
      }
   }

   @NotNull
   @Override
   public Layout createNewLayout(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.ensureActive();
      return new LayoutPattern(this, name, new LayoutConfiguration.LayoutDefinition(null, null, Collections.emptyList(), new LinkedHashMap<>()));
   }

   @Nullable
   @Override
   public Layout getLayout(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.layouts.get(name);
      }
   }

   @Override
   public void sendLayout(@NonNull TabPlayer player, @Nullable Layout layout) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      p.layoutData.forcedLayout = (LayoutPattern)layout;
      this.refresh(p, false);
   }

   @Override
   public void resetLayout(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      p.layoutData.forcedLayout = null;
      this.refresh(p, false);
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Layout";
   }

   @Generated
   public LayoutConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public LayoutSkinManager getSkinManager() {
      return this.skinManager;
   }

   @Generated
   public Map<Integer, UUID> getUuids() {
      return this.uuids;
   }

   @Generated
   public Map<String, LayoutPattern> getLayouts() {
      return this.layouts;
   }

   @Generated
   public Map<TabPlayer, String> getSortedPlayers() {
      return this.sortedPlayers;
   }

   @Generated
   public PlayerList getPlayerList() {
      return this.playerList;
   }

   @Generated
   public PingSpoof getPingSpoof() {
      return this.pingSpoof;
   }

   @Generated
   public static boolean isTeamsEnabled() {
      return teamsEnabled;
   }

   public static class LayoutData {
      @NotNull
      public final LayoutView view;
      @NotNull
      public final Map<FixedSlot, Property> fixedSlotTexts = new IdentityHashMap<>();
      @NotNull
      public final Map<FixedSlot, Property> fixedSlotSkins = new IdentityHashMap<>();

      @Generated
      public LayoutData(@NotNull LayoutView view) {
         if (view == null) {
            throw new NullPointerException("view is marked non-null but is null");
         }

         this.view = view;
      }
   }

   public static class PlayerData {
      public String sortingString;
      @Nullable
      public LayoutManagerImpl.LayoutData currentLayout;
      @Nullable
      public LayoutPattern forcedLayout;
   }
}
