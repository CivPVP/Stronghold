package me.neznamy.tab.shared.placeholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.Nullable;

public class PlaceholderRefreshTask implements Runnable {
   private final Collection<Placeholder> placeholdersToRefresh;
   private final Map<ServerPlaceholderImpl, String> serverPlaceholderResults = new HashMap<>();
   private final Map<PlayerPlaceholderImpl, Map<TabPlayer, String>> playerPlaceholderResults = new HashMap<>();
   @Nullable
   private Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> relationalPlaceholderResults;
   private final Map<String, Long> usedTime = new HashMap<>();

   @Override
   public void run() {
      boolean trackUsage = TAB.getInstance().getCpu().isTrackUsage();
      TabPlayer[] players = TAB.getInstance().getOnlinePlayers();

      for (Placeholder placeholder : this.placeholdersToRefresh) {
         long nanoTime = 0L;
         if (placeholder instanceof ServerPlaceholderImpl) {
            ServerPlaceholderImpl serverPlaceholder = (ServerPlaceholderImpl)placeholder;
            long startTime = System.nanoTime();
            String result = serverPlaceholder.request();
            nanoTime += System.nanoTime() - startTime;
            this.serverPlaceholderResults.put(serverPlaceholder, result);
         }

         if (placeholder instanceof PlayerPlaceholderImpl) {
            PlayerPlaceholderImpl playerPlaceholder = (PlayerPlaceholderImpl)placeholder;
            Map<TabPlayer, String> playerResults = new HashMap<>();

            for (TabPlayer player : players) {
               long startTime = System.nanoTime();
               String result = playerPlaceholder.request(player);
               nanoTime += System.nanoTime() - startTime;
               playerResults.put(player, result);
            }

            this.playerPlaceholderResults.put(playerPlaceholder, playerResults);
         }

         if (placeholder instanceof RelationalPlaceholderImpl) {
            RelationalPlaceholderImpl relationalPlaceholder = (RelationalPlaceholderImpl)placeholder;
            Map<TabPlayer, Map<TabPlayer, String>> viewerMap = new HashMap<>();

            for (TabPlayer viewer : players) {
               Map<TabPlayer, String> targetMap = new HashMap<>();

               for (TabPlayer target : players) {
                  long startTime = System.nanoTime();
                  String result = relationalPlaceholder.request(viewer, target);
                  nanoTime += System.nanoTime() - startTime;
                  targetMap.put(target, result);
               }

               viewerMap.put(viewer, targetMap);
            }

            if (this.relationalPlaceholderResults == null) {
               this.relationalPlaceholderResults = new HashMap<>();
            }

            this.relationalPlaceholderResults.put(relationalPlaceholder, viewerMap);
         }

         if (trackUsage) {
            this.usedTime.put(placeholder.getIdentifier(), nanoTime);
         }
      }
   }

   @Generated
   public PlaceholderRefreshTask(Collection<Placeholder> placeholdersToRefresh) {
      this.placeholdersToRefresh = placeholdersToRefresh;
   }

   @Generated
   public Collection<Placeholder> getPlaceholdersToRefresh() {
      return this.placeholdersToRefresh;
   }

   @Generated
   public Map<ServerPlaceholderImpl, String> getServerPlaceholderResults() {
      return this.serverPlaceholderResults;
   }

   @Generated
   public Map<PlayerPlaceholderImpl, Map<TabPlayer, String>> getPlayerPlaceholderResults() {
      return this.playerPlaceholderResults;
   }

   @Nullable
   @Generated
   public Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> getRelationalPlaceholderResults() {
      return this.relationalPlaceholderResults;
   }

   @Generated
   public Map<String, Long> getUsedTime() {
      return this.usedTime;
   }
}
