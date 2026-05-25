package me.neznamy.tab.platforms.bukkit.v1_7_R2;

import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.minecraft.server.v1_7_R2.IScoreboardCriteria;
import net.minecraft.server.v1_7_R2.Packet;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_7_R2.Scoreboard;
import net.minecraft.server.v1_7_R2.ScoreboardObjective;
import net.minecraft.server.v1_7_R2.ScoreboardScore;
import net.minecraft.server.v1_7_R2.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

public class NMSPacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {
   private static final Scoreboard dummyScoreboard = new Scoreboard();

   public NMSPacketScoreboard(@NotNull BukkitTabPlayer player) {
      super(player);
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      ScoreboardObjective obj = new ScoreboardObjective(dummyScoreboard, objective.getName(), IScoreboardCriteria.b);
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

      ScoreboardObjective obj = (ScoreboardObjective)objective.getPlatformObjective();
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
      this.sendPacket(new PacketPlayOutScoreboardScore(s, 0));
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
      t.setPrefix(this.maybeCut(team.getPrefix().toLegacyText(), 16));
      t.setSuffix(this.maybeCut(team.getSuffix().toLegacyText(), 16));
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      return packet;
   }

   private void sendPacket(@NotNull Packet packet) {
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
