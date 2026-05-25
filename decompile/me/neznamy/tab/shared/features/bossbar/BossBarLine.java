package me.neznamy.tab.shared.features.bossbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

public class BossBarLine implements BossBar {
   private final BossBarManagerImpl manager;
   private final String name;
   private final Condition displayCondition;
   private final UUID uniqueId = UUID.randomUUID();
   private String style;
   private String color;
   private String title;
   private String progress;
   private final boolean announcementBar;
   private final Set<TabPlayer> players = new HashSet<>();
   private final BossBarLine.TextRefresher textRefresher;
   private final BossBarLine.ProgressRefresher progressRefresher;
   private final BossBarLine.ColorRefresher colorRefresher;
   private final BossBarLine.StyleRefresher styleRefresher;
   private int announceTimeTotalSeconds;
   private long announceEndSystemTime;
   @NotNull
   private final ServerPlaceholder announceEndPlaceholder;

   public BossBarLine(@NonNull BossBarManagerImpl manager, @NonNull String name, @NonNull BossBarConfiguration.BossBarDefinition configuration) {
      if (manager == null) {
         throw new NullPointerException("manager is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (configuration == null) {
         throw new NullPointerException("configuration is marked non-null but is null");
      }

      this.manager = manager;
      this.name = name;
      this.displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisplayCondition());
      if (this.displayCondition != null) {
         manager.addUsedPlaceholder(TabConstants.Placeholder.condition(this.displayCondition.getName()));
      }

      this.color = configuration.getColor();
      this.style = configuration.getStyle();
      this.title = configuration.getText();
      this.progress = configuration.getProgress();
      this.announcementBar = configuration.isAnnouncementOnly();
      TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarTitle(name), this.textRefresher = new BossBarLine.TextRefresher());
      TAB.getInstance()
         .getFeatureManager()
         .registerFeature(TabConstants.Feature.bossBarProgress(name), this.progressRefresher = new BossBarLine.ProgressRefresher());
      TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarColor(name), this.colorRefresher = new BossBarLine.ColorRefresher());
      TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarStyle(name), this.styleRefresher = new BossBarLine.StyleRefresher());
      this.announceEndPlaceholder = TAB.getInstance()
         .getPlaceholderManager()
         .registerInternalServerPlaceholder(
            TabConstants.Placeholder.bossbarAnnounceTotal(name), -1, () -> PerformanceUtil.toString(this.announceTimeTotalSeconds)
         );
      TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder(TabConstants.Placeholder.bossbarAnnounceLeft(name), 100, () -> {
         long seconds = TimeUnit.MILLISECONDS.toSeconds(this.announceEndSystemTime - System.currentTimeMillis());
         return seconds < 0L ? "0" : PerformanceUtil.toString((int)seconds);
      });
   }

   public boolean isConditionMet(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return this.displayCondition == null ? true : this.displayCondition.isMet(p);
      }
   }

   @NotNull
   public BarColor parseColor(@NotNull TabPlayer player, @NonNull String color) {
      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      try {
         return BarColor.valueOf(color);
      } catch (IllegalArgumentException e) {
         TAB.getInstance()
            .getConfigHelper()
            .runtime()
            .invalidBossBarProperty(
               this,
               color,
               player.bossbarData.visibleBossBars.get(this).colorProperty.getCurrentRawValue(),
               player,
               "color",
               "one of the pre-defined values " + Arrays.toString(BarColor.values())
            );
         return BarColor.PURPLE;
      }
   }

   @NotNull
   public BarStyle parseStyle(@NotNull TabPlayer player, @NonNull String style) {
      if (style == null) {
         throw new NullPointerException("style is marked non-null but is null");
      }

      try {
         return BarStyle.valueOf(style);
      } catch (IllegalArgumentException e) {
         TAB.getInstance()
            .getConfigHelper()
            .runtime()
            .invalidBossBarProperty(
               this,
               style,
               player.bossbarData.visibleBossBars.get(this).styleProperty.getCurrentRawValue(),
               player,
               "style",
               "one of the pre-defined values " + Arrays.toString(BarStyle.values())
            );
         return BarStyle.PROGRESS;
      }
   }

   public float parseProgress(@NotNull TabPlayer player, @NotNull String progress) {
      try {
         float value = Float.parseFloat(progress);
         if (value < 0.0F) {
            value = 0.0F;
         }

         if (value > 100.0F) {
            value = 100.0F;
         }

         return value;
      } catch (NumberFormatException e) {
         TAB.getInstance()
            .getConfigHelper()
            .runtime()
            .invalidBossBarProperty(
               this,
               progress,
               player.bossbarData.visibleBossBars.get(this).progressProperty.getCurrentRawValue(),
               player,
               "progress",
               "a number between 0 and 100"
            );
         return 100.0F;
      }
   }

   public void removePlayerRaw(@NotNull TabPlayer player) {
      this.players.remove(player);
   }

   public void announce(int timeSeconds) {
      this.announceTimeTotalSeconds = timeSeconds;
      this.announceEndSystemTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeSeconds);
      this.announceEndPlaceholder.update();

      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         if (this.manager.hasBossBarVisible(all) && this.isConditionMet(all)) {
            this.addPlayer(all);
         }
      }
   }

   public boolean isBeingAnnounced() {
      return this.announceEndSystemTime > System.currentTimeMillis();
   }

   public void unAnnounce() {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         if (this.manager.hasBossBarVisible(all)) {
            this.removePlayer(all);
         }
      }
   }

   @Override
   public void setTitle(@NonNull String title) {
      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (!this.title.equals(title)) {
         this.title = title;

         for (TabPlayer p : this.players) {
            p.bossbarData.visibleBossBars.get(this).textProperty.changeRawValue(title);
            p.getBossBar().update(this.uniqueId, this.manager.getCache().get(p.bossbarData.visibleBossBars.get(this).textProperty.get()));
         }
      }
   }

   @Override
   public void setProgress(@NonNull String progress) {
      if (progress == null) {
         throw new NullPointerException("progress is marked non-null but is null");
      }

      if (!this.progress.equals(progress)) {
         this.progress = progress;

         for (TabPlayer p : this.players) {
            p.bossbarData.visibleBossBars.get(this).progressProperty.changeRawValue(progress);
            p.getBossBar().update(this.uniqueId, this.parseProgress(p, p.bossbarData.visibleBossBars.get(this).progressProperty.get()) / 100.0F);
         }
      }
   }

   @Override
   public void setProgress(float progress) {
      this.setProgress(String.valueOf(progress));
   }

   @Override
   public void setColor(@NonNull String color) {
      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      if (!this.color.equals(color)) {
         this.color = color;

         for (TabPlayer p : this.players) {
            p.bossbarData.visibleBossBars.get(this).colorProperty.changeRawValue(color);
            p.getBossBar().update(this.uniqueId, this.parseColor(p, p.bossbarData.visibleBossBars.get(this).colorProperty.get()));
         }
      }
   }

   @Override
   public void setColor(@NonNull BarColor color) {
      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      this.setColor(color.toString());
   }

   @Override
   public void setStyle(@NonNull String style) {
      if (style == null) {
         throw new NullPointerException("style is marked non-null but is null");
      }

      if (!this.style.equals(style)) {
         this.style = style;

         for (TabPlayer p : this.players) {
            p.bossbarData.visibleBossBars.get(this).styleProperty.changeRawValue(style);
            p.getBossBar().update(this.uniqueId, this.parseStyle(p, p.bossbarData.visibleBossBars.get(this).styleProperty.get()));
         }
      }
   }

   @Override
   public void setStyle(@NonNull BarStyle style) {
      if (style == null) {
         throw new NullPointerException("style is marked non-null but is null");
      }

      this.setStyle(style.toString());
   }

   @Override
   public void addPlayer(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      TabPlayer player = (TabPlayer)p;
      if (!player.bossbarData.visibleBossBars.containsKey(this)) {
         BossBarLinePlayerProperties properties = new BossBarLinePlayerProperties(
            new Property(this.textRefresher, player, this.title),
            new Property(this.progressRefresher, player, this.progress),
            new Property(this.colorRefresher, player, this.color),
            new Property(this.styleRefresher, player, this.style)
         );
         player.bossbarData.visibleBossBars.put(this, properties);
         player.getBossBar()
            .create(
               this.uniqueId,
               this.manager.getCache().get(properties.textProperty.get()),
               this.parseProgress(player, properties.progressProperty.get()) / 100.0F,
               this.parseColor(player, properties.colorProperty.get()),
               this.parseStyle(player, properties.styleProperty.get())
            );
         this.players.add(player);
      }
   }

   @Override
   public void removePlayer(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      TabPlayer player = (TabPlayer)p;
      if (player.bossbarData.visibleBossBars.containsKey(this)) {
         this.players.remove(player);
         player.bossbarData.visibleBossBars.remove(this);
         player.getBossBar().remove(this.uniqueId);
      }
   }

   @NotNull
   @Override
   public List<TabPlayer> getPlayers() {
      return new ArrayList<>(this.players);
   }

   @Override
   public boolean containsPlayer(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else {
         return ((TabPlayer)player).bossbarData.visibleBossBars.containsKey(this);
      }
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   @Override
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   @Override
   public String getStyle() {
      return this.style;
   }

   @Generated
   @Override
   public String getColor() {
      return this.color;
   }

   @Generated
   @Override
   public String getTitle() {
      return this.title;
   }

   @Generated
   @Override
   public String getProgress() {
      return this.progress;
   }

   @Generated
   @Override
   public boolean isAnnouncementBar() {
      return this.announcementBar;
   }

   private class ColorRefresher extends RefreshableFeature implements CustomThreaded {
      private ColorRefresher() {
      }

      @Override
      public void refresh(@NotNull TabPlayer refreshed, boolean force) {
         if (refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) {
            refreshed.getBossBar()
               .update(
                  BossBarLine.this.uniqueId,
                  BossBarLine.this.parseColor(refreshed, refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).colorProperty.updateAndGet())
               );
         }
      }

      @NotNull
      @Override
      public ThreadExecutor getCustomThread() {
         return BossBarLine.this.manager.getCustomThread();
      }

      @NotNull
      @Override
      public String getFeatureName() {
         return "BossBar";
      }

      @NotNull
      @Override
      public String getRefreshDisplayName() {
         return "Updating color";
      }
   }

   private class ProgressRefresher extends RefreshableFeature implements CustomThreaded {
      private ProgressRefresher() {
      }

      @Override
      public void refresh(@NotNull TabPlayer refreshed, boolean force) {
         if (refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) {
            refreshed.getBossBar()
               .update(
                  BossBarLine.this.uniqueId,
                  BossBarLine.this.parseProgress(refreshed, refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).progressProperty.updateAndGet())
                     / 100.0F
               );
         }
      }

      @NotNull
      @Override
      public ThreadExecutor getCustomThread() {
         return BossBarLine.this.manager.getCustomThread();
      }

      @NotNull
      @Override
      public String getFeatureName() {
         return "BossBar";
      }

      @NotNull
      @Override
      public String getRefreshDisplayName() {
         return "Updating progress";
      }
   }

   private class StyleRefresher extends RefreshableFeature implements CustomThreaded {
      private StyleRefresher() {
      }

      @Override
      public void refresh(@NotNull TabPlayer refreshed, boolean force) {
         if (refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) {
            refreshed.getBossBar()
               .update(
                  BossBarLine.this.uniqueId,
                  BossBarLine.this.parseStyle(refreshed, refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).styleProperty.updateAndGet())
               );
         }
      }

      @NotNull
      @Override
      public ThreadExecutor getCustomThread() {
         return BossBarLine.this.manager.getCustomThread();
      }

      @NotNull
      @Override
      public String getFeatureName() {
         return "BossBar";
      }

      @NotNull
      @Override
      public String getRefreshDisplayName() {
         return "Updating style";
      }
   }

   private class TextRefresher extends RefreshableFeature implements CustomThreaded {
      private TextRefresher() {
      }

      @Override
      public void refresh(@NotNull TabPlayer refreshed, boolean force) {
         if (refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) {
            refreshed.getBossBar()
               .update(
                  BossBarLine.this.uniqueId,
                  BossBarLine.this.manager.getCache().get(refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).textProperty.updateAndGet())
               );
         }
      }

      @NotNull
      @Override
      public ThreadExecutor getCustomThread() {
         return BossBarLine.this.manager.getCustomThread();
      }

      @NotNull
      @Override
      public String getFeatureName() {
         return "BossBar";
      }

      @NotNull
      @Override
      public String getRefreshDisplayName() {
         return "Updating text";
      }
   }
}
