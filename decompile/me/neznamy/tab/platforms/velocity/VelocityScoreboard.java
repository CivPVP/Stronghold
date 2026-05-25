package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.TextHolder;
import com.velocitypowered.api.scoreboard.NumberFormat;
import com.velocitypowered.api.scoreboard.ProxyObjective;
import com.velocitypowered.api.scoreboard.ProxyScoreboard;
import com.velocitypowered.api.scoreboard.ProxyTeam;
import com.velocitypowered.api.scoreboard.ScoreboardManager;
import com.velocitypowered.api.scoreboard.TeamColor;
import com.velocitypowered.api.scoreboard.ProxyObjective.Builder;
import java.util.function.Function;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

public class VelocityScoreboard extends SafeScoreboard<VelocityTabPlayer> {
   private static final Function<TabComponent, TextHolder> textHolderFunction = component -> TextHolder.of(component.toLegacyText(), component.toAdventure());
   private static final StringToComponentCache displayNames = new StringToComponentCache("Team display name", 5000);
   private static final TeamColor[] colors = TeamColor.values();
   private static final com.velocitypowered.api.scoreboard.NameVisibility[] visibilities = com.velocitypowered.api.scoreboard.NameVisibility.values();
   private static final com.velocitypowered.api.scoreboard.CollisionRule[] collisions = com.velocitypowered.api.scoreboard.CollisionRule.values();
   private final ProxyScoreboard scoreboard;

   public VelocityScoreboard(@NotNull VelocityTabPlayer player) {
      super(player);
      this.scoreboard = ScoreboardManager.getInstance().getProxyScoreboard(player.getPlayer());
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      try {
         Builder builder = this.scoreboard
            .objectiveBuilder(objective.getName())
            .healthDisplay(com.velocitypowered.api.scoreboard.HealthDisplay.valueOf(objective.getHealthDisplay().name()))
            .title(objective.getTitle().toTextHolder(textHolderFunction))
            .numberFormat(objective.getNumberFormat() == null ? null : NumberFormat.fixed(objective.getNumberFormat().toAdventure()));
         objective.setPlatformObjective(this.scoreboard.registerObjective(builder));
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().printError("Failed to register objective " + objective.getName() + " for player " + this.player.getName(), e);
      }
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      ((ProxyObjective)objective.getPlatformObjective())
         .setDisplaySlot(com.velocitypowered.api.scoreboard.DisplaySlot.valueOf(objective.getDisplaySlot().name()));
   }

   @Override
   public void unregisterObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      try {
         this.scoreboard.unregisterObjective(objective.getName());
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().printError("Failed to unregister objective " + objective.getName() + " for player " + this.player.getName(), e);
      }
   }

   @Override
   public void updateObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      try {
         ProxyObjective obj = (ProxyObjective)objective.getPlatformObjective();
         obj.setHealthDisplay(com.velocitypowered.api.scoreboard.HealthDisplay.valueOf(objective.getHealthDisplay().name()));
         obj.setTitle(objective.getTitle().toTextHolder(textHolderFunction));
         obj.setNumberFormat(objective.getNumberFormat() == null ? null : NumberFormat.fixed(objective.getNumberFormat().toAdventure()));
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().printError("Failed to update objective " + objective.getName() + " for player " + this.player.getName(), e);
      }
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      try {
         ((ProxyObjective)score.getObjective().getPlatformObjective())
            .setScore(
               score.getHolder(),
               b -> b.score(score.getValue())
                  .displayName(score.getDisplayName() == null ? null : score.getDisplayName().toAdventure())
                  .numberFormat(score.getNumberFormat() == null ? null : NumberFormat.fixed(score.getNumberFormat().toAdventure()))
            );
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().printError("Failed to set score " + score.getHolder() + " for player " + this.player.getName(), e);
      }
   }

   @Override
   public void removeScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      try {
         ((ProxyObjective)score.getObjective().getPlatformObjective()).removeScore(score.getHolder());
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().printError("Failed to remove score " + score.getHolder() + " for player " + this.player.getName(), e);
      }
   }

   @NotNull
   @Override
   public Object createTeam(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this;
      }
   }

   @Override
   public void registerTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      try {
         team.setPlatformTeam(
            this.scoreboard
               .registerTeam(
                  this.scoreboard
                     .teamBuilder(team.getName())
                     .displayName(displayNames.get(team.getName()).toTextHolder(textHolderFunction))
                     .prefix(team.getPrefix().toTextHolder(textHolderFunction))
                     .suffix(team.getSuffix().toTextHolder(textHolderFunction))
                     .nameVisibility(visibilities[team.getVisibility().ordinal()])
                     .collisionRule(collisions[team.getCollision().ordinal()])
                     .allowFriendlyFire((team.getOptions() & 1) > 0)
                     .canSeeFriendlyInvisibles((team.getOptions() & 2) > 0)
                     .color(colors[team.getColor().ordinal()])
                     .entries(team.getPlayers())
               )
         );
      } catch (Exception e) {
         TAB.getInstance()
            .getErrorManager()
            .printError(
               "Team "
                  + team.getName()
                  + " already existed with entry "
                  + this.scoreboard.getTeam(team.getName()).getEntries()
                  + " when registering for player "
                  + this.player.getName()
                  + " with new entry "
                  + team.getPlayers()
                  + ", unregistering",
               e
            );
         this.unregisterTeam(team);
         this.registerTeam(team);
      }
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      try {
         this.scoreboard.unregisterTeam(team.getName());
      } catch (Exception e) {
         TAB.getInstance().getErrorManager().printError("Team " + team.getName() + " did not exist when unregistering for player " + this.player.getName(), e);
      }
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      ((ProxyTeam)team.getPlatformTeam())
         .updateProperties(
            b -> b.prefix(team.getPrefix().toTextHolder(textHolderFunction))
               .suffix(team.getSuffix().toTextHolder(textHolderFunction))
               .nameVisibility(visibilities[team.getVisibility().ordinal()])
               .collisionRule(collisions[team.getCollision().ordinal()])
               .color(colors[team.getColor().ordinal()])
               .allowFriendlyFire((team.getOptions() & 1) > 0)
               .canSeeFriendlyInvisibles((team.getOptions() & 2) > 0)
         );
   }
}
