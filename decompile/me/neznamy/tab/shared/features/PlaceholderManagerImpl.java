package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshTask;
import me.neznamy.tab.shared.placeholders.conditions.ConditionManager;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderManagerImpl extends RefreshableFeature implements PlaceholderManager, JoinListener, Loadable {
   private static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
   @NotNull
   private final PlaceholderRefreshConfiguration configuration;
   private final Map<String, Placeholder> registeredPlaceholders = new HashMap<>();
   private final Map<String, Set<RefreshableFeature>> placeholderUsage = new ConcurrentHashMap<>();
   private Placeholder[] usedPlaceholders = new Placeholder[0];
   private long loopTime;
   @NotNull
   private final TabExpansion tabExpansion;
   private final CpuManager cpu;
   private final Map<String, Integer> bridgePlaceholders = new ConcurrentHashMap<>();
   private final ConditionManager conditionManager = new ConditionManager();

   public PlaceholderManagerImpl(@NotNull CpuManager cpu, @NotNull PlaceholderRefreshConfiguration configuration) {
      this.cpu = cpu;
      this.configuration = configuration;
      this.tabExpansion = TAB.getInstance().getConfiguration().getConfig().getPlaceholders().isRegisterTabExpansion()
         ? TAB.getInstance().getPlatform().createTabExpansion()
         : new EmptyTabExpansion();
   }

   private void refresh() {
      this.loopTime += 50L;
      List<Placeholder> placeholders = new ArrayList<>();

      for (Placeholder placeholder : this.usedPlaceholders) {
         if (placeholder.getRefresh() != -1 && this.loopTime % placeholder.getRefresh() == 0L) {
            placeholders.add(placeholder);
         }
      }

      if (!placeholders.isEmpty()) {
         PlaceholderRefreshTask task = new PlaceholderRefreshTask(placeholders);
         this.cpu.getPlaceholderThread().execute(new TimedCaughtTask(this.cpu, () -> {
            task.run();
            this.cpu.getProcessingThread().execute(() -> this.processRefreshResults(task));
         }, this.getFeatureName(), "Phase #2 - Requesting new values"));
      }
   }

   private void processRefreshResults(@NotNull PlaceholderRefreshTask task) {
      long time = System.nanoTime();
      Map<RefreshableFeature, Collection<TabPlayer>> update = new HashMap<>();

      for (RefreshableFeature f : this.updateServerPlaceholders(task.getServerPlaceholderResults())) {
         update.put(f, new HashSet<>(TAB.getInstance().getData().values()));
      }

      this.updatePlayerPlaceholders(task.getPlayerPlaceholderResults(), update);
      Map<RefreshableFeature, Collection<TabPlayer>> forceUpdate = this.updateRelationalPlaceholders(task.getRelationalPlaceholderResults());
      this.cpu.addTime(this.getFeatureName(), "Phase #3 - Saving results", System.nanoTime() - time);
      this.cpu.addPlaceholderTimes(task.getUsedTime());
      this.refreshFeatures(forceUpdate, update);
   }

   private void refreshFeatures(
      @NotNull Map<RefreshableFeature, Collection<TabPlayer>> forceUpdate, @NotNull Map<RefreshableFeature, Collection<TabPlayer>> update
   ) {
      for (Entry<RefreshableFeature, Collection<TabPlayer>> entry : update.entrySet()) {
         TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer player : entry.getValue()) {
               entry.getKey().refresh(player, false);
            }
         }, entry.getKey().getFeatureName(), entry.getKey().getRefreshDisplayName());
         if (entry.getKey() instanceof CustomThreaded) {
            ((CustomThreaded)entry.getKey()).getCustomThread().execute(task);
         } else {
            task.run();
         }
      }

      for (Entry<RefreshableFeature, Collection<TabPlayer>> entry : forceUpdate.entrySet()) {
         TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer player : entry.getValue()) {
               entry.getKey().refresh(player, true);
            }
         }, entry.getKey().getFeatureName(), entry.getKey().getRefreshDisplayName());
         if (entry.getKey() instanceof CustomThreaded) {
            ((CustomThreaded)entry.getKey()).getCustomThread().execute(task);
         } else {
            task.run();
         }
      }
   }

   @NotNull
   private Map<RefreshableFeature, Collection<TabPlayer>> updateRelationalPlaceholders(
      @Nullable Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> results
   ) {
      if (results == null) {
         return Collections.emptyMap();
      }

      Map<RefreshableFeature, Collection<TabPlayer>> update = new HashMap<>();

      for (Entry<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> entry : results.entrySet()) {
         RelationalPlaceholderImpl placeholder = entry.getKey();
         Collection<RefreshableFeature> placeholderUsage = this.getPlaceholderUsage(placeholder.getIdentifier());

         for (Entry<TabPlayer, Map<TabPlayer, String>> viewerResult : entry.getValue().entrySet()) {
            TabPlayer viewer = viewerResult.getKey();
            if (viewer.isOnline()) {
               for (Entry<TabPlayer, String> targetResult : viewerResult.getValue().entrySet()) {
                  TabPlayer target = targetResult.getKey();
                  if (target.isOnline() && placeholder.hasValueChanged(viewer, target, targetResult.getValue())) {
                     placeholder.updateParents(target);

                     for (RefreshableFeature f : placeholderUsage) {
                        update.computeIfAbsent(f, c -> new HashSet<>()).add(target);
                     }
                  }
               }
            }
         }
      }

      return update;
   }

   private void updatePlayerPlaceholders(
      @NotNull Map<PlayerPlaceholderImpl, Map<TabPlayer, String>> results, @NotNull Map<RefreshableFeature, Collection<TabPlayer>> update
   ) {
      if (!results.isEmpty()) {
         for (Entry<PlayerPlaceholderImpl, Map<TabPlayer, String>> entry : results.entrySet()) {
            PlayerPlaceholderImpl placeholder = entry.getKey();
            Set<RefreshableFeature> placeholderUsage = this.getPlaceholderUsage(placeholder.getIdentifier());

            for (Entry<TabPlayer, String> playerResult : entry.getValue().entrySet()) {
               TabPlayer player = playerResult.getKey();
               if (player.isOnline() && placeholder.hasValueChanged(player, playerResult.getValue(), true)) {
                  placeholder.updateParents(player);

                  for (RefreshableFeature f : placeholderUsage) {
                     update.computeIfAbsent(f, c -> new HashSet<>()).add(player);
                  }

                  if (placeholder.getIdentifier().equals("%vanished%")) {
                     TAB.getInstance().getFeatureManager().onVanishStatusChange(player);
                  }

                  if (placeholder.getIdentifier().equals("%gamemode%")) {
                     TAB.getInstance().getFeatureManager().onGameModeChange(player);
                  }
               }
            }
         }
      }
   }

   @NotNull
   private Set<RefreshableFeature> updateServerPlaceholders(@NotNull Map<ServerPlaceholderImpl, String> results) {
      Set<RefreshableFeature> set = new HashSet<>();

      for (Entry<ServerPlaceholderImpl, String> entry : results.entrySet()) {
         ServerPlaceholderImpl placeholder = entry.getKey();
         if (placeholder.hasValueChanged(entry.getValue())) {
            set.addAll(this.getPlaceholderUsage(placeholder.getIdentifier()));

            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
               placeholder.updateParents(all);
            }
         }
      }

      return set;
   }

   @NotNull
   public Collection<Placeholder> getAllPlaceholders() {
      return new ArrayList<>(this.registeredPlaceholders.values());
   }

   public synchronized <T extends Placeholder> T registerPlaceholder(@NotNull T placeholder) {
      boolean override = this.registeredPlaceholders.containsKey(placeholder.getIdentifier());
      this.registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
      this.recalculateUsedPlaceholders();
      if (override && this.placeholderUsage.containsKey(placeholder.getIdentifier())) {
         for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.isLoaded()) {
               for (RefreshableFeature f : this.placeholderUsage.get(placeholder.getIdentifier())) {
                  TimedCaughtTask task = new TimedCaughtTask(this.cpu, () -> f.refresh(p, true), f.getFeatureName(), f.getRefreshDisplayName());
                  if (f instanceof CustomThreaded) {
                     ((CustomThreaded)f).getCustomThread().execute(task);
                  } else {
                     task.run();
                  }
               }
            }
         }
      }

      return placeholder;
   }

   @Override
   public void load() {
      this.cpu.getProcessingThread().repeatTask(new TimedCaughtTask(this.cpu, this::refresh, this.getFeatureName(), "Phase #1 - Preparing for request"), 50);

      for (Placeholder pl : this.usedPlaceholders) {
         if (pl instanceof ServerPlaceholderImpl) {
            ((ServerPlaceholderImpl)pl).update();
         }
      }

      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(p);
      }
   }

   @NotNull
   public static List<String> detectPlaceholders(@NonNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (!text.contains("%")) {
         return Collections.emptyList();
      }

      if (text.charAt(0) == '%' && text.charAt(text.length() - 1) == '%') {
         int count = 0;
         char[] array = text.toCharArray();

         for (char c : array) {
            if (c == '%') {
               count++;
            }
         }

         if (count == 2) {
            return Collections.singletonList(text);
         }
      }

      List<String> placeholders = new ArrayList<>();
      Matcher m = placeholderPattern.matcher(text);

      while (m.find()) {
         placeholders.add(m.group());
      }

      return placeholders;
   }

   public synchronized void addUsedPlaceholder(@NonNull String identifier, @NonNull RefreshableFeature feature) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      if (this.placeholderUsage.computeIfAbsent(identifier, x -> new HashSet<>()).add(feature)) {
         this.recalculateUsedPlaceholders();
         TabPlaceholder p = this.getPlaceholder(identifier);

         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            this.tabExpansion.setPlaceholderValue(all, p.getIdentifier(), p.getLastValueSafe(all));
         }
      }
   }

   private void recalculateUsedPlaceholders() {
      this.usedPlaceholders = this.placeholderUsage.keySet().stream().map(this::getPlaceholder).distinct().toArray(Placeholder[]::new);
   }

   @NotNull
   public String findReplacement(@NonNull String placeholder, @NonNull String output) {
      if (placeholder == null) {
         throw new NullPointerException("placeholder is marked non-null but is null");
      } else if (output == null) {
         throw new NullPointerException("output is marked non-null but is null");
      } else {
         return this.getPlaceholder(placeholder).getReplacements().findReplacement(output);
      }
   }

   @NotNull
   public Set<RefreshableFeature> getPlaceholderUsage(@NotNull String identifier) {
      Set<RefreshableFeature> usage = this.placeholderUsage.getOrDefault(identifier, new HashSet<>());

      for (String parent : this.getPlaceholder(identifier).getParents()) {
         usage.addAll(this.getPlaceholderUsage(parent));
      }

      return usage;
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      for (Placeholder p : this.usedPlaceholders) {
         if (p instanceof ServerPlaceholderImpl) {
            this.tabExpansion.setPlaceholderValue(connectedPlayer, p.getIdentifier(), ((ServerPlaceholderImpl)p).getLastValue());
         }
      }

      ((PlayerPlaceholderImpl)this.registeredPlaceholders.get("%vanished%")).update(connectedPlayer);
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Other";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
   }

   @Nullable
   public Placeholder getPlaceholderRaw(@NotNull String identifier) {
      return this.registeredPlaceholders.get(identifier);
   }

   @NotNull
   public ServerPlaceholderImpl registerInternalServerPlaceholder(@NonNull String identifier, int defaultRefresh, @NonNull Supplier<String> supplier) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      } else if (supplier == null) {
         throw new NullPointerException("supplier is marked non-null but is null");
      } else {
         return this.registerServerPlaceholder(identifier, this.configuration.getRefreshInterval(identifier, defaultRefresh), supplier);
      }
   }

   @NotNull
   public PlayerPlaceholderImpl registerInternalPlayerPlaceholder(@NonNull String identifier, int defaultRefresh, @NonNull Function<TabPlayer, String> function) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      } else if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      } else {
         return this.registerPlayerPlaceholder(identifier, this.configuration.getRefreshInterval(identifier, defaultRefresh), function);
      }
   }

   @NotNull
   public RelationalPlaceholderImpl registerInternalRelationalPlaceholder(
      @NonNull String identifier, int defaultRefresh, @NonNull BiFunction<TabPlayer, TabPlayer, String> function
   ) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      } else if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      } else {
         return this.registerRelationalPlaceholder(identifier, this.configuration.getRefreshInterval(identifier, defaultRefresh), function);
      }
   }

   @NotNull
   public ServerPlaceholderImpl registerServerPlaceholder(@NonNull String identifier, @NonNull Supplier<String> supplier) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      } else if (supplier == null) {
         throw new NullPointerException("supplier is marked non-null but is null");
      } else {
         return this.registerServerPlaceholder(identifier, this.configuration.getRefreshInterval(identifier), supplier);
      }
   }

   @NotNull
   public PlayerPlaceholderImpl registerPlayerPlaceholder(@NonNull String identifier, @NonNull Function<TabPlayer, String> function) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      } else if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      } else {
         return this.registerPlayerPlaceholder(identifier, this.configuration.getRefreshInterval(identifier), function);
      }
   }

   @NotNull
   public RelationalPlaceholderImpl registerRelationalPlaceholder(@NonNull String identifier, @NonNull BiFunction<TabPlayer, TabPlayer, String> function) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      } else if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      } else {
         return this.registerRelationalPlaceholder(identifier, this.configuration.getRefreshInterval(identifier), function);
      }
   }

   @NotNull
   public ServerPlaceholderImpl registerServerPlaceholder(@NonNull String identifier, int refresh, @NonNull Supplier<String> supplier) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (supplier == null) {
         throw new NullPointerException("supplier is marked non-null but is null");
      }

      this.ensureActive();
      this.bridgePlaceholders.remove(identifier);
      return this.registerPlaceholder(new ServerPlaceholderImpl(identifier, refresh, supplier));
   }

   @NotNull
   public PlayerPlaceholderImpl registerPlayerPlaceholder(@NonNull String identifier, int refresh, @NonNull Function<TabPlayer, String> function) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      }

      this.ensureActive();
      this.bridgePlaceholders.remove(identifier);
      return this.registerPlaceholder(new PlayerPlaceholderImpl(identifier, refresh, function));
   }

   @NotNull
   public RelationalPlaceholderImpl registerRelationalPlaceholder(
      @NonNull String identifier, int refresh, @NonNull BiFunction<TabPlayer, TabPlayer, String> function
   ) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      }

      this.ensureActive();
      this.bridgePlaceholders.remove(identifier);
      return this.registerPlaceholder(new RelationalPlaceholderImpl(identifier, refresh, function));
   }

   @NotNull
   public PlayerPlaceholderImpl registerBridgePlaceholder(@NonNull String identifier, int backendRefresh) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      this.ensureActive();
      this.bridgePlaceholders.put(identifier, backendRefresh);
      return this.registerPlaceholder(new PlayerPlaceholderImpl(identifier, -1, player -> null));
   }

   @NotNull
   public RelationalPlaceholderImpl registerRelationalBridgePlaceholder(@NonNull String identifier, int backendRefresh) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      this.ensureActive();
      this.bridgePlaceholders.put(identifier, backendRefresh);
      return this.registerPlaceholder(new RelationalPlaceholderImpl(identifier, -1, (viewer, target) -> null));
   }

   @NotNull
   public synchronized TabPlaceholder getPlaceholder(@NonNull String identifier) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (identifier.charAt(0) == '%' && identifier.charAt(identifier.length() - 1) == '%') {
         TabPlaceholder p = (TabPlaceholder)this.registeredPlaceholders.get(identifier);
         if (p == null) {
            TabPlaceholderRegisterEvent event = new TabPlaceholderRegisterEvent(identifier);
            if (TAB.getInstance().getEventBus() != null) {
               TAB.getInstance().getEventBus().fire(event);
            }

            if (event.getServerPlaceholder() != null) {
               this.registerServerPlaceholder(identifier, event.getServerPlaceholder());
            } else if (event.getPlayerPlaceholder() != null) {
               this.registerPlayerPlaceholder(identifier, event.getPlayerPlaceholder());
            } else if (event.getRelationalPlaceholder() != null) {
               this.registerRelationalPlaceholder(identifier, event.getRelationalPlaceholder());
            } else {
               TAB.getInstance().getPlatform().registerUnknownPlaceholder(identifier);
            }

            this.addUsedPlaceholder(identifier, this);
            return this.getPlaceholder(identifier);
         } else {
            if (!this.placeholderUsage.containsKey(identifier)) {
               this.addUsedPlaceholder(identifier, this);
            }

            return p;
         }
      } else {
         throw new IllegalArgumentException("Placeholder identifier must start and end with %");
      }
   }

   @Override
   public void unregisterPlaceholder(@NonNull Placeholder placeholder) {
      if (placeholder == null) {
         throw new NullPointerException("placeholder is marked non-null but is null");
      }

      this.ensureActive();
      this.unregisterPlaceholder(placeholder.getIdentifier());
   }

   @Override
   public void unregisterPlaceholder(@NonNull String identifier) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      this.ensureActive();
      this.registeredPlaceholders.remove(identifier);
      this.placeholderUsage.remove(identifier);
      this.recalculateUsedPlaceholders();
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Refreshing placeholders";
   }

   @NotNull
   @Generated
   public PlaceholderRefreshConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public Map<String, Placeholder> getRegisteredPlaceholders() {
      return this.registeredPlaceholders;
   }

   @Generated
   public Map<String, Set<RefreshableFeature>> getPlaceholderUsage() {
      return this.placeholderUsage;
   }

   @Generated
   public Placeholder[] getUsedPlaceholders() {
      return this.usedPlaceholders;
   }

   @Generated
   public long getLoopTime() {
      return this.loopTime;
   }

   @NotNull
   @Generated
   public TabExpansion getTabExpansion() {
      return this.tabExpansion;
   }

   @Generated
   public CpuManager getCpu() {
      return this.cpu;
   }

   @Generated
   public Map<String, Integer> getBridgePlaceholders() {
      return this.bridgePlaceholders;
   }

   @Generated
   public ConditionManager getConditionManager() {
      return this.conditionManager;
   }
}
