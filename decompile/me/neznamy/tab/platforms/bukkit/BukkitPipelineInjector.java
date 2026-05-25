package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class BukkitPipelineInjector extends NettyPipelineInjector {
   public BukkitPipelineInjector() {
      super("packet_handler");
   }

   @NotNull
   @Override
   protected Channel getChannel(@NotNull TabPlayer player) {
      try {
         return ((BukkitPlatform)player.getPlatform()).getImplementationProvider().getChannelFunction().apply((BukkitTabPlayer)player);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }
}
