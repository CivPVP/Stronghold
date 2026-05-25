package me.neznamy.tab.platforms.bungeecord.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import java.util.function.Function;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.files.Config;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.UserConnection;
import org.jetbrains.annotations.NotNull;

public class BungeePipelineInjector extends NettyPipelineInjector {
   private final boolean byteBufDeserialization;

   public BungeePipelineInjector() {
      super("inbound-boss");
      Config config = TAB.getInstance().getConfiguration().getConfig();
      this.byteBufDeserialization = config.getTeams() != null || config.getScoreboard() != null;
   }

   @NotNull
   @Override
   public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
      return this.byteBufDeserialization ? DeserializableBungeeChannelDuplexHandler::new : BungeeChannelDuplexHandler::new;
   }

   @NotNull
   @Override
   protected Channel getChannel(@NotNull TabPlayer player) {
      return ((UserConnection)((BungeeTabPlayer)player).getPlayer()).getCh().getHandle();
   }
}
