package me.neznamy.tab.platforms.fabric;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.class_124;
import net.minecraft.class_2596;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_2736;
import net.minecraft.class_274;
import net.minecraft.class_2751;
import net.minecraft.class_2757;
import net.minecraft.class_5900;
import net.minecraft.class_8646;
import net.minecraft.class_9006;
import net.minecraft.class_9021;
import net.minecraft.class_270.class_271;
import net.minecraft.class_270.class_272;
import net.minecraft.class_274.class_275;
import net.minecraft.class_5900.class_5901;
import org.jetbrains.annotations.NotNull;

public class FabricScoreboard extends SafeScoreboard<FabricTabPlayer> {
   private static final class_124[] formats = class_124.values();
   private static final class_271[] collisions = class_271.values();
   private static final class_272[] visibilities = class_272.values();
   private static final class_269 dummyScoreboard = new class_269();
   private static final Field players = ReflectionUtils.getOnlyField(class_5900.class, Collection.class);

   public FabricScoreboard(FabricTabPlayer player) {
      super(player);
   }

   @Override
   public void registerObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      class_266 obj = new class_266(
         dummyScoreboard,
         objective.getName(),
         class_274.field_1468,
         objective.getTitle().convert(),
         class_275.values()[objective.getHealthDisplay().ordinal()],
         false,
         objective.getNumberFormat() == null ? null : objective.getNumberFormat().toFixedFormat(class_9021::new)
      );
      objective.setPlatformObjective(obj);
      this.sendPacket(new class_2751(obj, 0));
   }

   @Override
   public void setDisplaySlot(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(new class_2736(class_8646.values()[objective.getDisplaySlot().ordinal()], (class_266)objective.getPlatformObjective()));
   }

   @Override
   public void unregisterObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      this.sendPacket(new class_2751((class_266)objective.getPlatformObjective(), 1));
   }

   @Override
   public void updateObjective(@NonNull SafeScoreboard.Objective objective) {
      if (objective == null) {
         throw new NullPointerException("objective is marked non-null but is null");
      }

      class_266 obj = (class_266)objective.getPlatformObjective();
      obj.method_1121(objective.getTitle().convert());
      obj.method_1115(class_275.values()[objective.getHealthDisplay().ordinal()]);
      this.sendPacket(new class_2751(obj, 2));
   }

   @Override
   public void setScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.sendPacket(
         new class_2757(
            score.getHolder(),
            score.getObjective().getName(),
            score.getValue(),
            Optional.ofNullable(score.getDisplayName() == null ? null : score.getDisplayName().convert()),
            Optional.ofNullable(score.getNumberFormat() == null ? null : score.getNumberFormat().toFixedFormat(class_9021::new))
         )
      );
   }

   @Override
   public void removeScore(@NonNull SafeScoreboard.Score score) {
      if (score == null) {
         throw new NullPointerException("score is marked non-null but is null");
      }

      this.sendPacket(new class_9006(score.getHolder(), score.getObjective().getName()));
   }

   @NotNull
   @Override
   public Object createTeam(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return new class_268(dummyScoreboard, name);
      }
   }

   @Override
   public void registerTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      class_268 t = (class_268)team.getPlatformTeam();
      t.method_1204().addAll(team.getPlayers());
      this.sendPacket(class_5900.method_34172(t, true));
   }

   @Override
   public void unregisterTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.sendPacket(class_5900.method_34170((class_268)team.getPlatformTeam()));
   }

   @Override
   public void updateTeam(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      this.updateTeamProperties(team);
      this.sendPacket(class_5900.method_34172((class_268)team.getPlatformTeam(), false));
   }

   private void updateTeamProperties(@NonNull SafeScoreboard.Team team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      }

      class_268 t = (class_268)team.getPlatformTeam();
      t.method_1135((team.getOptions() & 1) != 0);
      t.method_1143((team.getOptions() & 2) != 0);
      t.method_1141(formats[team.getColor().ordinal()]);
      t.method_1145(collisions[team.getCollision().ordinal()]);
      t.method_1149(visibilities[team.getVisibility().ordinal()]);
      t.method_1138(team.getPrefix().convert());
      t.method_1139(team.getSuffix().convert());
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         }

         if (packet instanceof class_2736 display) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(this.player, display.method_11806().ordinal(), display.method_11804());
         }

         if (packet instanceof class_2751 objective) {
            TAB.getInstance().getFeatureManager().onObjective(this.player, objective.method_11837(), objective.method_11835());
         }

         if (packet instanceof class_5900 team) {
            int method = this.getMethod(team);
            if (method != 2) {
               players.set(team, this.onTeamPacket(method, team.method_34177(), team.method_34178()));
            }
         }

         return packet;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private int getMethod(@NonNull class_5900 team) {
      if (team == null) {
         throw new NullPointerException("team is marked non-null but is null");
      } else if (team.method_34176() == class_5901.field_29155) {
         return 0;
      } else if (team.method_34176() == class_5901.field_29156) {
         return 1;
      } else if (team.method_34174() == class_5901.field_29155) {
         return 3;
      } else {
         return team.method_34174() == class_5901.field_29156 ? 4 : 2;
      }
   }

   private void sendPacket(@NotNull class_2596<?> packet) {
      this.player.getPlayer().field_13987.method_14364(packet);
   }
}
