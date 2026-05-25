package me.neznamy.tab.platforms.forge;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class ForgePipelineInjector extends NettyPipelineInjector {
   public ForgePipelineInjector() {
      super("packet_handler");
   }

   @NotNull
   @Override
   protected Channel getChannel(@NotNull TabPlayer player) {
      return ((ForgeTabPlayer)player).getPlayer().connection.getConnection().channel();
   }
}
