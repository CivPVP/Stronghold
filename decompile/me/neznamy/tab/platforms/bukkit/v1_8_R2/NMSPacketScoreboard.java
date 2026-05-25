package me.neznamy.tab.platforms.bukkit.v1_8_R2;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_8_R2.IScoreboardCriteria;
import net.minecraft.server.v1_8_R2.Packet;
import net.minecraft.server.v1_8_R2.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_8_R2.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_8_R2.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_8_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R2.Scoreboard;
import net.minecraft.server.v1_8_R2.ScoreboardBaseCriteria;
import net.minecraft.server.v1_8_R2.ScoreboardHealthCriteria;
import net.minecraft.server.v1_8_R2.ScoreboardObjective;
import net.minecraft.server.v1_8_R2.ScoreboardScore;
import net.minecraft.server.v1_8_R2.ScoreboardTeam;
import net.minecraft.server.v1_8_R2.ScoreboardTeamBase.EnumNameTagVisibility;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

public class NMSPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {
   private static final EnumNameTagVisibility[] visibilities = EnumNameTagVisibility.values();
   private static final Scoreboard dummyScoreboard = new Scoreboard();
   private static final IScoreboardCriteria INTEGER = new ScoreboardBaseCriteria("dummy");
   private static final IScoreboardCriteria HEARTS = new ScoreboardHealthCriteria("health");
   private static final Field TeamPacket_NAME = ReflectionUtils.getFields(PacketPlayOutScoreboardTeam.class, String.class).get(0);
   private static final Field TeamPacket_ACTION = ReflectionUtils.getInstanceFields(PacketPlayOutScoreboardTeam.class, int.class).get(1);
   private static final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardTeam.class, Collection.class);
   private static final Field Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(PacketPlayOutScoreboardObjective.class, String.class).get(0);
   private static final Field Objective_METHOD = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardObjective.class, int.class);
   private static final Field DisplayObjective_OBJECTIVE_NAME = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardDisplayObjective.class, String.class);
   private static final Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(PacketPlayOutScoreboardDisplayObjective.class, int.class);

   public NMSPacketScoreboard(@NotNull BukkitTabPlayer player) {
      super(player);
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      IScoreboardCriteria healthDisplay = objective.getHealthDisplay() == me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay.INTEGER ? INTEGER : HEARTS;
      ScoreboardObjective obj = new ScoreboardObjective(dummyScoreboard, objective.getName(), healthDisplay);
      obj.setDisplayName(this.maybeCut(objective.getTitle().toLegacyText(), 32));
      objective.setPlatformObjective(obj);
      this.sendPacket(new PacketPlayOutScoreboardObjective(obj, 0));
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(new PacketPlayOutScoreboardDisplayObjective(objective.getDisplaySlot().ordinal(), (ScoreboardObjective)objective.getPlatformObjective()));
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

      IScoreboardCriteria healthDisplay = objective.getHealthDisplay() == me.neznamy.tab.shared.platform.Scoreboard.HealthDisplay.INTEGER ? INTEGER : HEARTS;
      ScoreboardObjective obj = new ScoreboardObjective(dummyScoreboard, objective.getName(), healthDisplay);
      obj.setDisplayName(this.maybeCut(objective.getTitle().toLegacyText(), 32));
      this.sendPacket(new PacketPlayOutScoreboardObjective(obj, 2));
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      ScoreboardScore s = new ScoreboardScore(dummyScoreboard, (ScoreboardObjective)score.getObjective().getPlatformObjective(), score.getHolder());
      s.setScore(score.getValue());
      this.sendPacket(new PacketPlayOutScoreboardScore(s));
   }

   @Override
   public void removeScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.sendPacket(new PacketPlayOutScoreboardScore(score.getHolder()));
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
      t.getPlayerNameSet().addAll(team.getPlayers());
      this.sendPacket(new PacketPlayOutScoreboardTeam(t, 0));
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.sendPacket(new PacketPlayOutScoreboardTeam((ScoreboardTeam)team.getPlatformTeam(), 1));
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      this.sendPacket(new PacketPlayOutScoreboardTeam((ScoreboardTeam)team.getPlatformTeam(), 2));
   }

   private void updateTeamProperties(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      ScoreboardTeam t = (ScoreboardTeam)team.getPlatformTeam();
      t.setAllowFriendlyFire((team.getOptions() & 1) != 0);
      t.setCanSeeFriendlyInvisibles((team.getOptions() & 2) != 0);
      t.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
      t.setPrefix(this.maybeCut(team.getPrefix().toLegacyText(), 16));
      t.setSuffix(this.maybeCut(team.getSuffix().toLegacyText(), 16));
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         }

         if (packet instanceof PacketPlayOutScoreboardDisplayObjective) {
            TAB.getInstance()
               .getFeatureManager()
               .onDisplayObjective(this.player, DisplayObjective_POSITION.getInt(packet), (String)DisplayObjective_OBJECTIVE_NAME.get(packet));
         }

         if (packet instanceof PacketPlayOutScoreboardObjective) {
            TAB.getInstance().getFeatureManager().onObjective(this.player, Objective_METHOD.getInt(packet), (String)Objective_OBJECTIVE_NAME.get(packet));
         }

         if (packet instanceof PacketPlayOutScoreboardTeam) {
            int action = TeamPacket_ACTION.getInt(packet);
            if (action != 2) {
               Collection<String> players = (Collection<String>)TeamPacket_PLAYERS.get(packet);
               if (players == null) {
                  players = Collections.emptyList();
               }

               TeamPacket_PLAYERS.set(packet, this.onTeamPacket(action, (String)TeamPacket_NAME.get(packet), players));
            }
         }

         return packet;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private void sendPacket(@NotNull Packet<?> packet) {
      ((CraftPlayer)this.player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
   }

   @NotNull
   private String maybeCut(@NonNull String string, int length) {
      if (string == null) {
         throw new NullPointerException("string is marked non-null but is null");
      } else {
         return this.player.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()
            ? string
            : cutTo(string, length);
      }
   }
}
