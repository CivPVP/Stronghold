package me.neznamy.tab.shared.hook;

import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Function;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuckPermsHook {
   private static final LuckPermsHook instance = new LuckPermsHook();
   private final boolean installed = ReflectionUtils.classExists("net.luckperms.api.LuckPerms");
   private final Function<TabPlayer, String> groupFunction = p -> {
      if (p.luckPermsUser == null) {
         p.luckPermsUser = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
      }

      if (p.luckPermsUser == null) {
         TAB.getInstance().debug("LuckPerms returned null user for player " + p.getName() + "( " + p.getUniqueId() + ")");
         return "NONE";
      } else {
         return p.luckPermsUser.getPrimaryGroup();
      }
   };

   @NotNull
   public String getPrefix(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      CachedMetaData data = this.getCachedMetaData(p);
      if (data == null) {
         return "";
      }

      String value = data.getPrefix();
      return value == null ? "" : value;
   }

   @NotNull
   public String getPrefixes(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return Optional.ofNullable(this.getCachedMetaData(p)).<SortedMap>map(CachedMetaData::getPrefixes).map(pr -> String.join("", pr.values())).orElse("");
      }
   }

   @NotNull
   public String getSuffix(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      CachedMetaData data = this.getCachedMetaData(p);
      if (data == null) {
         return "";
      }

      String value = data.getSuffix();
      return value == null ? "" : value;
   }

   @NotNull
   public String getSuffixes(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return Optional.ofNullable(this.getCachedMetaData(p)).<SortedMap>map(CachedMetaData::getSuffixes).map(s -> String.join("", s.values())).orElse("");
      }
   }

   @Nullable
   private CachedMetaData getCachedMetaData(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.luckPermsUser == null) {
         p.luckPermsUser = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
      }

      if (p.luckPermsUser == null) {
         return null;
      }

      Optional<QueryOptions> options = LuckPermsProvider.get().getContextManager().getQueryOptions(p.luckPermsUser);
      return options.<CachedMetaData>map(queryOptions -> p.luckPermsUser.getCachedData().getMetaData(queryOptions)).orElse(null);
   }

   public int getWeight(@NonNull TabPlayer tabPlayer) {
      if (tabPlayer == null) {
         throw new NullPointerException("tabPlayer is marked non-null but is null");
      }

      if (tabPlayer.luckPermsUser == null) {
         tabPlayer.luckPermsUser = LuckPermsProvider.get().getUserManager().getUser(tabPlayer.getUniqueId());
      }

      if (tabPlayer.luckPermsUser == null) {
         return 0;
      }

      Group primaryGroup = LuckPermsProvider.get().getGroupManager().getGroup(tabPlayer.luckPermsUser.getPrimaryGroup());
      return primaryGroup == null ? 0 : primaryGroup.getWeight().orElse(0);
   }

   @Generated
   public boolean isInstalled() {
      return this.installed;
   }

   @Generated
   public Function<TabPlayer, String> getGroupFunction() {
      return this.groupFunction;
   }

   @Generated
   public static LuckPermsHook getInstance() {
      return instance;
   }
}
