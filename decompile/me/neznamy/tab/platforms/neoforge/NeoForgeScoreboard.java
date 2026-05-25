package me.neznamy.tab.platforms.neoforge;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.Visibility;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.jetbrains.annotations.NotNull;

public class NeoForgeScoreboard extends SafeScoreboard<NeoForgeTabPlayer> {
   private static final ChatFormatting[] formats = ChatFormatting.values();
   private static final net.minecraft.world.scores.Team.CollisionRule[] collisions = net.minecraft.world.scores.Team.CollisionRule.values();
   private static final Visibility[] visibilities = Visibility.values();
   private static final Scoreboard dummyScoreboard = new Scoreboard();
   private static final Field players = ReflectionUtils.getOnlyField(ClientboundSetPlayerTeamPacket.class, Collection.class);

   public NeoForgeScoreboard(NeoForgeTabPlayer player) {
      super(player);
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      net.minecraft.world.scores.Objective obj = new net.minecraft.world.scores.Objective(
         dummyScoreboard,
         objective.getName(),
         ObjectiveCriteria.DUMMY,
         objective.getTitle().convert(),
         RenderType.values()[objective.getHealthDisplay().ordinal()],
         false,
         objective.getNumberFormat() == null ? null : objective.getNumberFormat().toFixedFormat(FixedFormat::new)
      );
      objective.setPlatformObjective(obj);
      this.sendPacket(new ClientboundSetObjectivePacket(obj, 0));
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(
         new ClientboundSetDisplayObjectivePacket(
            net.minecraft.world.scores.DisplaySlot.values()[objective.getDisplaySlot().ordinal()],
            (net.minecraft.world.scores.Objective)objective.getPlatformObjective()
         )
      );
   }

   @Override
   public void unregisterObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(new ClientboundSetObjectivePacket((net.minecraft.world.scores.Objective)objective.getPlatformObjective(), 1));
   }

   @Override
   public void updateObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      net.minecraft.world.scores.Objective obj = (net.minecraft.world.scores.Objective)objective.getPlatformObjective();
      obj.setDisplayName(objective.getTitle().convert());
      obj.setRenderType(RenderType.values()[objective.getHealthDisplay().ordinal()]);
      this.sendPacket(new ClientboundSetObjectivePacket(obj, 2));
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.sendPacket(
         new ClientboundSetScorePacket(
            score.getHolder(),
            score.getObjective().getName(),
            score.getValue(),
            Optional.ofNullable(score.getDisplayName() == null ? null : score.getDisplayName().convert()),
            Optional.ofNullable(score.getNumberFormat() == null ? null : score.getNumberFormat().toFixedFormat(FixedFormat::new))
         )
      );
   }

   @Override
   public void removeScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.sendPacket(new ClientboundResetScorePacket(score.getHolder(), score.getObjective().getName()));
   }

   @NotNull
   @Override
   public Object createTeam(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return new PlayerTeam(dummyScoreboard, name);
      }
   }

   @Override
   public void registerTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      PlayerTeam t = (PlayerTeam)team.getPlatformTeam();
      t.getPlayers().addAll(team.getPlayers());
      this.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(t, true));
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.sendPacket(ClientboundSetPlayerTeamPacket.createRemovePacket((PlayerTeam)team.getPlatformTeam()));
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      this.sendPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket((PlayerTeam)team.getPlatformTeam(), false));
   }

   private void updateTeamProperties(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      PlayerTeam t = (PlayerTeam)team.getPlatformTeam();
      t.setAllowFriendlyFire((team.getOptions() & 1) != 0);
      t.setSeeFriendlyInvisibles((team.getOptions() & 2) != 0);
      t.setColor(formats[team.getColor().ordinal()]);
      t.setCollisionRule(collisions[team.getCollision().ordinal()]);
      t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
      t.setPlayerPrefix(team.getPrefix().convert());
      t.setPlayerSuffix(team.getSuffix().convert());
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         }

         if (packet instanceof ClientboundSetDisplayObjectivePacket display) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(this.player, display.getSlot().ordinal(), display.getObjectiveName());
         }

         if (packet instanceof ClientboundSetObjectivePacket objective) {
            TAB.getInstance().getFeatureManager().onObjective(this.player, objective.getMethod(), objective.getObjectiveName());
         }

         if (packet instanceof ClientboundSetPlayerTeamPacket team) {
            int method = this.getMethod(team);
            if (method != 2) {
               players.set(team, this.onTeamPacket(method, team.getName(), team.getPlayers()));
            }
         }

         return packet;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private int getMethod(@NonNull ClientboundSetPlayerTeamPacket team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      } else if (team.getTeamAction() == Action.ADD) {
         return 0;
      } else if (team.getTeamAction() == Action.REMOVE) {
         return 1;
      } else if (team.getPlayerAction() == Action.ADD) {
         return 3;
      } else {
         return team.getPlayerAction() == Action.REMOVE ? 4 : 2;
      }
   }

   private void sendPacket(@NotNull Packet<?> packet) {
      this.player.getPlayer().connection.send(packet);
   }
}
