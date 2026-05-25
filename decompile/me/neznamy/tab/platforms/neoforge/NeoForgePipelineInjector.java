package me.neznamy.tab.platforms.neoforge;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class NeoForgePipelineInjector extends NettyPipelineInjector {
   public NeoForgePipelineInjector() {
      super("packet_handler");
   }

   @NotNull
   @Override
   protected Channel getChannel(@NotNull TabPlayer player) {
      return ((NeoForgeTabPlayer)player).getPlayer().connection.getConnection().channel();
   }
}
