package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.data.NumberFormat;
import net.md_5.bungee.protocol.data.NumberFormat.Type;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.ScoreboardScoreReset;
import net.md_5.bungee.protocol.packet.Team.NameTagVisibility;
import net.md_5.bungee.protocol.util.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BungeeScoreboard extends SafeScoreboard<BungeeTabPlayer> {
   private final int TEAM_REWORK_VERSION = 13;

   public BungeeScoreboard(@NonNull BungeeTabPlayer player) {
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

      this.sendObjectivePacket(objective, (byte)0);
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.player.sendPacket(new ScoreboardDisplay(objective.getDisplaySlot().ordinal(), objective.getName()));
   }

   @Override
   public void unregisterObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendObjectivePacket(objective, (byte)1);
   }

   @Override
   public void updateObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendObjectivePacket(objective, (byte)2);
   }

   private void sendObjectivePacket(@NonNull SafeScoreboard.Objective objective, byte action) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.player
         .sendPacket(
            new ScoreboardObjective(
               objective.getName(),
               this.either(objective.getTitle(), 32),
               net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay.values()[objective.getHealthDisplay().ordinal()],
               action,
               this.numberFormat(objective.getNumberFormat())
            )
         );
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.player
         .sendPacket(
            new ScoreboardScore(
               score.getHolder(),
               (byte)0,
               score.getObjective().getName(),
               score.getValue(),
               score.getDisplayName() == null ? null : this.player.getPlatform().transformComponent(score.getDisplayName(), this.player.getVersion()),
               this.numberFormat(score.getNumberFormat())
            )
         );
   }

   @Override
   public void removeScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      if (this.player.getVersionId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
         this.player.sendPacket(new ScoreboardScoreReset(score.getHolder(), score.getObjective().getName()));
      } else {
         this.player.sendPacket(new ScoreboardScore(score.getHolder(), (byte)1, score.getObjective().getName(), 0, null, null));
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

      this.sendTeamPacket(team, (byte)0);
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.sendTeamPacket(team, (byte)1);
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.sendTeamPacket(team, (byte)2);
   }

   private void sendTeamPacket(@NonNull SafeScoreboard.Team team, byte action) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.player
         .sendPacket(
            new net.md_5.bungee.protocol.packet.Team(
               team.getName(),
               action,
               this.either(TabComponent.legacyText(team.getName()), 16),
               this.either(team.getPrefix(), 16),
               this.either(team.getSuffix(), 16),
               this.convertVisibility(team.getVisibility()),
               this.convertCollision(team.getCollision()),
               this.player.getVersion().getMinorVersion() >= 13 ? team.getColor().ordinal() : 0,
               (byte)team.getOptions(),
               team.getPlayers().toArray(new String[0])
            )
         );
   }

   @NotNull
   private Either<String, NameTagVisibility> convertVisibility(@NotNull Scoreboard.NameVisibility visibility) {
      return this.player.getVersionId() >= ProtocolVersion.V1_21_5.getNetworkId()
         ? Either.right(NameTagVisibility.valueOf(visibility.name()))
         : Either.left(visibility.toString());
   }

   @NotNull
   private Either<String, CollisionRule> convertCollision(@NotNull Scoreboard.CollisionRule collision) {
      return this.player.getVersionId() >= ProtocolVersion.V1_21_5.getNetworkId()
         ? Either.right(net.md_5.bungee.protocol.packet.Team.CollisionRule.valueOf(collision.name()))
         : Either.left(collision.toString());
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      if (packet == null) {
         throw new NullPointerException("packet is marked non-null but is null");
      }

      if (packet instanceof ScoreboardDisplay) {
         ScoreboardDisplay display = (ScoreboardDisplay)packet;
         TAB.getInstance().getFeatureManager().onDisplayObjective(this.player, display.getPosition(), display.getName());
      }

      if (packet instanceof ScoreboardObjective) {
         ScoreboardObjective objective = (ScoreboardObjective)packet;
         TAB.getInstance().getFeatureManager().onObjective(this.player, objective.getAction(), objective.getName());
      }

      if (packet instanceof net.md_5.bungee.protocol.packet.Team) {
         net.md_5.bungee.protocol.packet.Team team = (net.md_5.bungee.protocol.packet.Team)packet;
         if (team.getMode() != 2) {
            List<String> players = team.getPlayers() == null ? Collections.emptyList() : Lists.newArrayList(team.getPlayers());
            team.setPlayers(this.onTeamPacket(team.getMode(), team.getName(), players).toArray(new String[0]));
         }
      }

      return packet;
   }

   @NotNull
   private Either<String, BaseComponent> either(@NonNull TabComponent text, int legacyLimit) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      } else {
         return this.player.getVersion().getMinorVersion() >= 13
            ? Either.right(this.player.getPlatform().transformComponent(text, this.player.getVersion()))
            : Either.left(cutTo(text.toLegacyText(), legacyLimit));
      }
   }

   @Nullable
   private NumberFormat numberFormat(@Nullable TabComponent component) {
      return component == null
         ? null
         : component.toFixedFormat(
            baseComponentArray -> new NumberFormat(
               Type.FIXED, this.player.getPlatform().pickCorrectComponent((BaseComponent[])baseComponentArray, this.player.getVersion())
            )
         );
   }
}
