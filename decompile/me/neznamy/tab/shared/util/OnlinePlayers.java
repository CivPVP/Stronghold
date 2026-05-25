package me.neznamy.tab.shared.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class OnlinePlayers {
   private final Set<TabPlayer> playerSet;
   private TabPlayer[] players;

   public OnlinePlayers(@NotNull TabPlayer[] players) {
      this.playerSet = Arrays.stream(players).collect(Collectors.toSet());
      this.players = (TabPlayer[])players.clone();
   }

   public void addPlayer(@NotNull TabPlayer player) {
      this.playerSet.add(player);
      this.players = this.playerSet.toArray(new TabPlayer[0]);
   }

   public void removePlayer(@NotNull TabPlayer player) {
      this.playerSet.remove(player);
      this.players = this.playerSet.toArray(new TabPlayer[0]);
   }

   public boolean contains(@NotNull TabPlayer player) {
      return this.playerSet.contains(player);
   }

   @Generated
   public TabPlayer[] getPlayers() {
      return this.players;
   }
}
