package me.neznamy.tab.shared.platform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Scoreboard {
   void registerObjective(@NonNull String var1, @NonNull TabComponent var2, @NonNull Scoreboard.HealthDisplay var3, @Nullable TabComponent var4);

   void setDisplaySlot(@NonNull String var1, @NonNull Scoreboard.DisplaySlot var2);

   void unregisterObjective(@NonNull String var1);

   void updateObjective(@NonNull String var1, @NonNull TabComponent var2, Scoreboard.HealthDisplay var3, @Nullable TabComponent var4);

   void setScore(@NonNull String var1, @NonNull String var2, int var3, @Nullable TabComponent var4, @Nullable TabComponent var5);

   void removeScore(@NonNull String var1, @NonNull String var2);

   void registerTeam(
      @NonNull String var1,
      @NonNull TabComponent var2,
      @NonNull TabComponent var3,
      @NonNull Scoreboard.NameVisibility var4,
      @NonNull Scoreboard.CollisionRule var5,
      @NonNull Collection<String> var6,
      int var7,
      @NonNull EnumChatFormat var8
   );

   void unregisterTeam(@NonNull String var1);

   void updateTeam(
      @NonNull String var1,
      @NonNull TabComponent var2,
      @NonNull TabComponent var3,
      @NonNull Scoreboard.NameVisibility var4,
      @NonNull Scoreboard.CollisionRule var5,
      int var6,
      @NonNull EnumChatFormat var7
   );

   void updateTeam(@NonNull String var1, @NonNull TabComponent var2, @NonNull TabComponent var3, @NonNull EnumChatFormat var4);

   void updateTeam(@NonNull String var1, @NonNull Scoreboard.CollisionRule var2);

   void updateTeam(@NonNull String var1, @NonNull Scoreboard.NameVisibility var2);

   void renameTeam(@NonNull String var1, @NonNull String var2);

   void resend();

   void clear();

   enum CollisionRule {
      ALWAYS("always"),
      NEVER("never"),
      PUSH_OTHER_TEAMS("pushOtherTeams"),
      PUSH_OWN_TEAM("pushOwnTeam");

      private static final Map<String, Scoreboard.CollisionRule> BY_NAME = Arrays.stream(values())
         .collect(Collectors.toMap(collisionRule -> collisionRule.string, collisionRule -> (Scoreboard.CollisionRule)collisionRule));
      private final String string;

      @Override
      public String toString() {
         return this.string;
      }

      @NotNull
      public static Scoreboard.CollisionRule getByName(@NotNull String name) {
         return BY_NAME.getOrDefault(name, ALWAYS);
      }

      @Generated
      CollisionRule(final String string) {
         this.string = string;
      }
   }

   enum DisplaySlot {
      PLAYER_LIST,
      SIDEBAR,
      BELOW_NAME;
   }

   enum HealthDisplay {
      INTEGER,
      HEARTS;
   }

   enum NameVisibility {
      ALWAYS("always"),
      NEVER("never"),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam");

      private static final Map<String, Scoreboard.NameVisibility> BY_NAME = Arrays.stream(values())
         .collect(Collectors.toMap(visibility -> visibility.string, visibility -> (Scoreboard.NameVisibility)visibility));
      private final String string;

      @Override
      public String toString() {
         return this.string;
      }

      public static Scoreboard.NameVisibility getByName(String name) {
         return BY_NAME.getOrDefault(name, ALWAYS);
      }

      @Generated
      NameVisibility(final String string) {
         this.string = string;
      }
   }

   class ObjectiveAction {
      public static final int REGISTER = 0;
      public static final int UNREGISTER = 1;
      public static final int UPDATE = 2;
   }

   class ScoreAction {
      public static final int CHANGE = 0;
      public static final int REMOVE = 1;
   }

   class TeamAction {
      public static final int CREATE = 0;
      public static final int REMOVE = 1;
      public static final int UPDATE = 2;
      public static final int ADD_PLAYER = 3;
      public static final int REMOVE_PLAYER = 4;
   }
}
