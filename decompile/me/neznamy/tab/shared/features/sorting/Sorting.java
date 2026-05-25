package me.neznamy.tab.shared.features.sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.sorting.types.Groups;
import me.neznamy.tab.shared.features.sorting.types.Permissions;
import me.neznamy.tab.shared.features.sorting.types.Placeholder;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderAtoZ;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderHighToLow;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderLowToHigh;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderZtoA;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Sorting extends RefreshableFeature implements SortingManager, JoinListener, Loadable {
   private NameTag nameTags;
   private LayoutManagerImpl layout;
   private ProxySupport proxy;
   private final Map<String, BiFunction<Sorting, String, SortingType>> types = new LinkedHashMap<>();
   @NotNull
   private final SortingConfiguration configuration;
   private final SortingType[] usedSortingTypes;

   public Sorting(@NotNull SortingConfiguration configuration) {
      this.configuration = configuration;
      this.types.put("GROUPS", Groups::new);
      this.types.put("PERMISSIONS", Permissions::new);
      this.types.put("PLACEHOLDER", (sorting, value) -> {
         Placeholder.PlaceholderSplitResult split = Placeholder.splitValue(value);
         return split == null ? null : new Placeholder(sorting, split);
      });
      this.types.put("PLACEHOLDER_A_TO_Z", PlaceholderAtoZ::new);
      this.types.put("PLACEHOLDER_Z_TO_A", PlaceholderZtoA::new);
      this.types.put("PLACEHOLDER_LOW_TO_HIGH", PlaceholderLowToHigh::new);
      this.types.put("PLACEHOLDER_HIGH_TO_LOW", PlaceholderHighToLow::new);
      this.usedSortingTypes = this.compile(configuration.getSortingTypes());
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating team names";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      String previousShortName = p.sortingData.shortTeamName;
      this.constructTeamNames(p);
      if (!p.sortingData.shortTeamName.equals(previousShortName)) {
         if (this.nameTags != null) {
            this.nameTags.updateTeamName(p, p.sortingData.getShortTeamName());
         }

         if (this.layout != null) {
            this.layout.updateTeamName(p, p.sortingData.getFullTeamName());
         }
      }
   }

   @Override
   public void load() {
      this.nameTags = TAB.getInstance().getNameTagManager();
      this.layout = TAB.getInstance().getFeatureManager().getFeature("layout");
      this.proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");

      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(all);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.constructTeamNames(connectedPlayer);
   }

   @NotNull
   private SortingType[] compile(@NotNull List<String> options) {
      List<SortingType> list = new ArrayList<>();

      for (String element : options) {
         String[] arr = element.split(":");
         if (!this.types.containsKey(arr[0].toUpperCase())) {
            TAB.getInstance().getConfigHelper().startup().invalidSortingTypeElement(arr[0].toUpperCase(), this.types.keySet());
         } else {
            SortingType type = this.types.get(arr[0].toUpperCase()).apply(this, arr.length == 1 ? "" : element.substring(arr[0].length() + 1));
            if (type != null) {
               list.add(type);
            }
         }
      }

      return list.toArray(new SortingType[0]);
   }

   public void constructTeamNames(@NotNull TabPlayer p) {
      p.sortingData.teamNameNote = "";
      StringBuilder shortName = new StringBuilder();

      for (SortingType type : this.usedSortingTypes) {
         shortName.append(type.getChars(p));
      }

      StringBuilder fullName = new StringBuilder(shortName);
      if (this.layout != null) {
         shortName.insert(0, '\uffff');
      }

      if (shortName.length() >= 16) {
         shortName.setLength(15);
      }

      String finalShortName = this.checkTeamName(p, shortName);
      p.sortingData.shortTeamName = finalShortName;
      p.sortingData.fullTeamName = fullName.append(finalShortName.charAt(finalShortName.length() - 1)).toString();
      if (p.sortingData.forcedTeamName != null) {
         p.sortingData.teamNameNote = "Set using API";
      }
   }

   @NotNull
   private String checkTeamName(@NotNull TabPlayer p, @NotNull StringBuilder currentName) {
      char id = 'A';

      while (true) {
         String potentialTeamName = currentName.toString() + id;
         boolean nameTaken = false;

         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all != p && potentialTeamName.equals(all.sortingData.shortTeamName)) {
               nameTaken = true;
               break;
            }
         }

         if (!nameTaken && this.proxy != null && this.nameTags != null) {
            for (ProxyPlayer all : this.proxy.getProxyPlayers().values()) {
               if (all.getNametag() != null && potentialTeamName.equals(all.getNametag().getResolvedTeamName())) {
                  nameTaken = true;
                  break;
               }
            }
         }

         if (!nameTaken) {
            return potentialTeamName;
         }

         id++;
      }
   }

   @NotNull
   public String typesToString() {
      return Arrays.stream(this.usedSortingTypes).map(SortingType::getDisplayName).collect(Collectors.joining(" -> "));
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Sorting";
   }

   @Override
   public void forceTeamName(@NonNull TabPlayer player, String name) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      TabPlayer p = (TabPlayer)player;
      p.ensureLoaded();
      if (!Objects.equals(p.sortingData.forcedTeamName, name)) {
         if (name != null) {
            if (name.length() > 16) {
               throw new IllegalArgumentException("Team name cannot be more than 16 characters long.");
            }

            p.sortingData.teamNameNote = "Set using API";
         }

         p.sortingData.forcedTeamName = name;
         if (this.layout != null) {
            this.layout.updateTeamName(p, p.sortingData.getFullTeamName());
         }

         if (this.nameTags != null) {
            this.nameTags.updateTeamName(p, p.sortingData.getShortTeamName());
         }
      }
   }

   @Nullable
   @Override
   public String getForcedTeamName(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      return ((TabPlayer)player).sortingData.forcedTeamName;
   }

   @NotNull
   @Override
   public String getOriginalTeamName(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.ensureActive();
      ((TabPlayer)player).ensureLoaded();
      return ((TabPlayer)player).sortingData.shortTeamName;
   }

   @NotNull
   @Generated
   public SortingConfiguration getConfiguration() {
      return this.configuration;
   }
}
