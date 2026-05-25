package me.neznamy.tab.shared.platform;

import java.util.UUID;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.task.PluginMessageDecodeTask;
import org.jetbrains.annotations.NotNull;

public interface EventListener<T> {
   default void join(@NotNull T player) {
      if (!TAB.getInstance().isPluginDisabled()) {
         TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onJoin(this.createPlayer(player)));
      }
   }

   default void quit(@NotNull UUID player) {
      if (!TAB.getInstance().isPluginDisabled()) {
         TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(player)));
      }
   }

   default void worldChange(@NotNull UUID player, @NotNull String world) {
      if (!TAB.getInstance().isPluginDisabled()) {
         TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onWorldChange(player, World.byName(world)));
      }
   }

   default void pluginMessage(@NotNull UUID player, byte[] message) {
      TAB.getInstance()
         .getCpu()
         .getPluginMessageDecodeThread()
         .execute(new TimedCaughtTask(TAB.getInstance().getCpu(), new PluginMessageDecodeTask(player, message), "Plugin message handling", "Decoding message"));
   }

   default void replacePlayer(UUID player, T newPlayer) {
      if (!TAB.getInstance().isPluginDisabled()) {
         TabPlayer p = TAB.getInstance().getPlayer(player);
         if (p != null) {
            p.setPlayer(newPlayer);
         }
      }
   }

   @NotNull
   TabPlayer createPlayer(@NotNull T var1);
}
