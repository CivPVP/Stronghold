package me.neznamy.tab.shared.features.bossbar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.ToggleManager;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BossBarManagerImpl extends RefreshableFeature implements BossBarManager, JoinListener, Loadable, QuitListener, CustomThreaded {
   private final StringToComponentCache cache = new StringToComponentCache("BossBar", 1000);
   private final ThreadExecutor customThread = new ThreadExecutor("TAB BossBar Thread");
   private final Map<String, BossBarLine> registeredBossBars = new LinkedHashMap<>();
   protected BossBarLine[] lineValues;
   private final BossBarConfiguration configuration;
   private final String toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
   private final String toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();
   @Nullable
   private ToggleManager toggleManager;

   public BossBarManagerImpl(@NonNull BossBarConfiguration configuration) {
      if (configuration == null) {
         throw new NullPointerException("configuration is marked non-null but is null");
      }

      this.configuration = configuration;
      if (configuration.isRememberToggleChoice()) {
         this.toggleManager = new ToggleManager(TAB.getInstance().getConfiguration().getPlayerData(), "bossbar-off");
      }

      for (Entry<String, BossBarConfiguration.BossBarDefinition> entry : configuration.getBars().entrySet()) {
         String name = entry.getKey();
         this.registeredBossBars.put(name, new BossBarLine(this, name, entry.getValue()));
      }

      this.lineValues = this.registeredBossBars.values().toArray(new BossBarLine[0]);
   }

   @Override
   public void load() {
      TAB.getInstance().getPlatform().registerCustomCommand(this.configuration.getToggleCommand().replaceFirst("/", ""), px -> {
         if (this.isActive()) {
            if (px.hasPermission("tab.bossbar.toggle")) {
               this.toggleBossBar(px, true);
            } else {
               px.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNoPermission());
            }
         }
      });

      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(p);
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating display conditions";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      if (this.hasBossBarVisible(p)) {
         boolean conditionResultChange = false;

         for (BossBarLine line : this.lineValues) {
            if (line.isConditionMet(p) != p.bossbarData.visibleBossBars.containsKey(line)) {
               conditionResultChange = true;
            }
         }

         if (conditionResultChange) {
            for (BossBar line : this.lineValues) {
               line.removePlayer(p);
            }

            this.showBossBars(p);
         }
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      TAB.getInstance().getPlaceholderManager().getTabExpansion().setBossBarVisible(connectedPlayer, false);
      if (this.toggleManager != null) {
         this.toggleManager.convert(connectedPlayer);
      }

      this.setBossBarVisible(
         connectedPlayer, this.configuration.isHiddenByDefault() == (this.toggleManager != null && this.toggleManager.contains(connectedPlayer)), false
      );
   }

   protected void detectBossBarsAndSend(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (this.hasBossBarVisible(p)) {
         this.showBossBars(p);
      }
   }

   private void showBossBars(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      for (BossBarLine bossbar : this.lineValues) {
         if (bossbar.isConditionMet(p) && (!bossbar.isAnnouncementBar() || bossbar.isBeingAnnounced())) {
            bossbar.addPlayer(p);
         }
      }
   }

   @Override
   public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
      for (BossBarLine line : this.lineValues) {
         line.removePlayerRaw(disconnectedPlayer);
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "BossBar";
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      if (style == null) {
         throw new NullPointerException("style is marked non-null but is null");
      }

      this.ensureActive();
      return this.createBossBar(title, String.valueOf(progress), color.toString(), style.toString());
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NonNull String title, @NonNull String progress, @NonNull String color, @NonNull String style) {
      if (title == null) {
         throw new NullPointerException("title is marked non-null but is null");
      }

      if (progress == null) {
         throw new NullPointerException("progress is marked non-null but is null");
      }

      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      }

      if (style == null) {
         throw new NullPointerException("style is marked non-null but is null");
      }

      this.ensureActive();
      UUID id = UUID.randomUUID();
      BossBarLine bar = new BossBarLine(this, id.toString(), new BossBarConfiguration.BossBarDefinition(style, color, progress, title, true, null));
      this.registeredBossBars.put(id.toString(), bar);
      this.lineValues = this.registeredBossBars.values().toArray(new BossBarLine[0]);
      return bar;
   }

   @Override
   public BossBar getBossBar(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.ensureActive();
      return this.registeredBossBars.get(name);
   }

   @NotNull
   @Override
   public Map<String, BossBar> getRegisteredBossBars() {
      return Collections.unmodifiableMap(this.registeredBossBars);
   }

   @Override
   public void removeBossBar(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.ensureActive();
      BossBar bar = this.registeredBossBars.remove(name);
      if (bar == null) {
         throw new IllegalArgumentException("No registered BossBar found with name " + name);
      }

      this.lineValues = this.registeredBossBars.values().toArray(new BossBarLine[0]);

      for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
         bar.removePlayer(player);
         player.bossbarData.visibleBossBars.remove(bar);
      }
   }

   @Override
   public void removeBossBar(@NonNull BossBar bossBar) {
      if (bossBar == null) {
         throw new NullPointerException("bossBar is marked non-null but is null");
      }

      this.ensureActive();
      BossBarLine bar = (BossBarLine)bossBar;
      if (!this.registeredBossBars.remove(bar.getName(), bar)) {
         throw new IllegalArgumentException("This bossbar (" + bar.getName() + ") is not registered.");
      }

      this.lineValues = this.registeredBossBars.values().toArray(new BossBarLine[0]);

      for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
         bar.removePlayer(player);
         player.bossbarData.visibleBossBars.remove(bar);
      }
   }

   @Override
   public void toggleBossBar(@NonNull TabPlayer player, boolean sendToggleMessage) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      this.setBossBarVisible(player, !this.hasBossBarVisible(player), sendToggleMessage);
   }

   @Override
   public boolean hasBossBarVisible(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).bossbarData.visible;
   }

   @Override
   public void setBossBarVisible(@NonNull TabPlayer p, boolean visible, boolean sendToggleMessage) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer player = (TabPlayer)p;
      if (player.bossbarData.visible != visible) {
         if (visible) {
            player.bossbarData.visible = true;
            this.detectBossBarsAndSend(player);
            if (sendToggleMessage) {
               player.sendMessage(this.toggleOnMessage);
            }

            if (this.toggleManager != null) {
               if (this.configuration.isHiddenByDefault()) {
                  this.toggleManager.add(player);
               } else {
                  this.toggleManager.remove(player);
               }
            }
         } else {
            player.bossbarData.visible = false;

            for (BossBar l : this.lineValues) {
               l.removePlayer(player);
            }

            if (sendToggleMessage) {
               player.sendMessage(this.toggleOffMessage);
            }

            if (this.toggleManager != null) {
               if (this.configuration.isHiddenByDefault()) {
                  this.toggleManager.remove(player);
               } else {
                  this.toggleManager.add(player);
               }
            }
         }

         TAB.getInstance().getPlaceholderManager().getTabExpansion().setBossBarVisible(player, visible);
      }
   }

   @Override
   public void sendBossBarTemporarily(@NonNull TabPlayer player, @NonNull String bossBar, int duration) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (bossBar == null) {
         throw new NullPointerException("bossBar is marked non-null but is null");
      }

      this.ensureActive();
      BossBar line = this.registeredBossBars.get(bossBar);
      if (line == null) {
         throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
      }

      if (this.hasBossBarVisible(player)) {
         this.customThread
            .execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> line.addPlayer(player), this.getFeatureName(), "Adding temporary BossBar"));
         this.customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (((TabPlayer)player).isOnline()) {
               line.removePlayer(player);
            }
         }, this.getFeatureName(), "Removing temporary BossBar"), duration * 1000);
      }
   }

   @Override
   public void announceBossBar(@NonNull String bossBar, int duration) {
      if (bossBar == null) {
         throw new NullPointerException("bossBar is marked non-null but is null");
      }

      this.ensureActive();
      BossBarLine line = this.registeredBossBars.get(bossBar);
      if (line == null) {
         throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
      }

      if (!line.isAnnouncementBar()) {
         throw new IllegalArgumentException("BossBar " + bossBar + " is not an announcement bar");
      }

      this.customThread
         .execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> line.announce(duration), this.getFeatureName(), "Adding announced BossBar"));
      this.customThread
         .executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), line::unAnnounce, this.getFeatureName(), "Removing announced BossBar"), duration * 1000);
   }

   @NotNull
   @Override
   public List<BossBar> getAnnouncedBossBars() {
      return this.registeredBossBars.values().stream().filter(BossBarLine::isBeingAnnounced).collect(Collectors.toList());
   }

   @Generated
   public StringToComponentCache getCache() {
      return this.cache;
   }

   @Generated
   @Override
   public ThreadExecutor getCustomThread() {
      return this.customThread;
   }

   @Generated
   public BossBarConfiguration getConfiguration() {
      return this.configuration;
   }
}
