package me.neznamy.tab.api;

import java.util.UUID;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TabAPI {
   private static TabAPI instance;

   @NotNull
   public static TabAPI getInstance() {
      return instance;
   }

   @Nullable
   public abstract TabPlayer getPlayer(@NonNull UUID var1);

   @Nullable
   public abstract TabPlayer getPlayer(@NonNull String var1);

   @NotNull
   public abstract TabPlayer[] getOnlinePlayers();

   @Nullable
   public abstract BossBarManager getBossBarManager();

   @Nullable
   public abstract ScoreboardManager getScoreboardManager();

   @Nullable
   public abstract NameTagManager getNameTagManager();

   @Nullable
   public abstract HeaderFooterManager getHeaderFooterManager();

   @NotNull
   public abstract PlaceholderManager getPlaceholderManager();

   @Nullable
   public abstract TabListFormatManager getTabListFormatManager();

   @Nullable
   public abstract LayoutManager getLayoutManager();

   @Nullable
   public abstract SortingManager getSortingManager();

   @Nullable
   public abstract EventBus getEventBus();

   @Generated
   public static void setInstance(TabAPI instance) {
      TabAPI.instance = instance;
   }
}
