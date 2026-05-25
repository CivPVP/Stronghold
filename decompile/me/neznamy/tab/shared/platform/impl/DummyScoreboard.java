package me.neznamy.tab.shared.platform.impl;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import org.jetbrains.annotations.NotNull;

public class DummyScoreboard extends SafeScoreboard<TabPlayer> {
   public DummyScoreboard(@NonNull TabPlayer player) {
      super(player);
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }
   }

   @Override
   public void unregisterObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }
   }

   @Override
   public void updateObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }
   }

   @Override
   public void removeScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }
   }

   @NotNull
   @Override
   public Object createTeam(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return new Object();
      }
   }

   @Override
   public void registerTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }
   }
}
