package me.neznamy.tab.platforms.bukkit.v1_21_R1;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumNameTagVisibility;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumTeamPush;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import net.minecraft.world.scores.criteria.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

public class NMSPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {
   private static final EnumNameTagVisibility[] visibilities = EnumNameTagVisibility.values();
   private static final EnumTeamPush[] collisions = EnumTeamPush.values();
   private static final Scoreboard dummyScoreboard = new Scoreboard();
   private static final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardTeam.class, Collection.class);

   public NMSPacketScoreboard(@NotNull BukkitTabPlayer player) {
      super(player);
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      ScoreboardObjective obj = new ScoreboardObjective(
         dummyScoreboard,
         objective.getName(),
         IScoreboardCriteria.b,
         objective.getTitle().convert(),
         EnumScoreboardHealthDisplay.values()[objective.getHealthDisplay().ordinal()],
         false,
         objective.getNumberFormat() == null ? null : objective.getNumberFormat().toFixedFormat(FixedFormat::new)
      );
      objective.setPlatformObjective(obj);
      this.sendPacket(new PacketPlayOutScoreboardObjective(obj, 0));
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(
         new PacketPlayOutScoreboardDisplayObjective(
            net.minecraft.world.scores.DisplaySlot.values()[objective.getDisplaySlot().ordinal()], (ScoreboardObjective)objective.getPlatformObjective()
         )
      );
   }

   @Override
   public void unregisterObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(new PacketPlayOutScoreboardObjective((ScoreboardObjective)objective.getPlatformObjective(), 1));
   }

   @Override
   public void updateObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      ScoreboardObjective obj = (ScoreboardObjective)objective.getPlatformObjective();
      obj.a(objective.getTitle().convert());
      obj.a(EnumScoreboardHealthDisplay.valueOf(objective.getHealthDisplay().name()));
      this.sendPacket(new PacketPlayOutScoreboardObjective(obj, 2));
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.sendPacket(
         new PacketPlayOutScoreboardScore(
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
         return new ScoreboardTeam(dummyScoreboard, name);
      }
   }

   @Override
   public void registerTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      ScoreboardTeam t = (ScoreboardTeam)team.getPlatformTeam();
      t.g().addAll(team.getPlayers());
      this.sendPacket(PacketPlayOutScoreboardTeam.a(t, true));
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.sendPacket(PacketPlayOutScoreboardTeam.a((ScoreboardTeam)team.getPlatformTeam()));
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      this.sendPacket(PacketPlayOutScoreboardTeam.a((ScoreboardTeam)team.getPlatformTeam(), false));
   }

   private void updateTeamProperties(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      ScoreboardTeam t = (ScoreboardTeam)team.getPlatformTeam();
      t.a((team.getOptions() & 1) != 0);
      t.b((team.getOptions() & 2) != 0);
      t.a(visibilities[team.getVisibility().ordinal()]);
      t.a(collisions[team.getCollision().ordinal()]);
      t.b(team.getPrefix().convert());
      t.c(team.getSuffix().convert());
      t.a(EnumChatFormat.valueOf(team.getColor().name()));
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         }

         if (packet instanceof PacketPlayOutScoreboardDisplayObjective display) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(this.player, display.b().ordinal(), display.e());
         }

         if (packet instanceof PacketPlayOutScoreboardObjective objective) {
            TAB.getInstance().getFeatureManager().onObjective(this.player, objective.f(), objective.b());
         }

         if (packet instanceof PacketPlayOutScoreboardTeam team) {
            int action = this.getMethod(team);
            if (action != 2) {
               TeamPacket_PLAYERS.set(packet, this.onTeamPacket(action, team.f(), team.g() == null ? Collections.emptyList() : team.g()));
            }
         }

         return packet;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private int getMethod(@NonNull PacketPlayOutScoreboardTeam team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      } else if (team.e() == a.a) {
         return 0;
      } else if (team.e() == a.b) {
         return 1;
      } else if (team.b() == a.a) {
         return 3;
      } else {
         return team.b() == a.b ? 4 : 2;
      }
   }

   private void sendPacket(@NotNull Packet<?> packet) {
      ((CraftPlayer)this.player.getPlayer()).getHandle().c.b(packet);
   }
}
