package me.neznamy.tab.platforms.fabric;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class FabricPipelineInjector extends NettyPipelineInjector {
   public FabricPipelineInjector() {
      super("packet_handler");
   }

   @NotNull
   @Override
   protected Channel getChannel(@NotNull TabPlayer player) {
      return ((FabricTabPlayer)player).getPlayer().field_13987.field_45013.field_11651;
   }
}
