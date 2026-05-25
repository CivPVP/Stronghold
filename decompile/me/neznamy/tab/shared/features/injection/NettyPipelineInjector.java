package me.neznamy.tab.shared.features.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.NoSuchElementException;
import java.util.function.Function;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NettyPipelineInjector extends PipelineInjector {
   @NotNull
   private final String injectPosition;
   private final Function<TabPlayer, ChannelDuplexHandler> channelFunction = NettyPipelineInjector.TabChannelDuplexHandler::new;

   @NotNull
   protected abstract Channel getChannel(@NotNull TabPlayer var1);

   @Override
   public void inject(@NotNull TabPlayer player) {
      Channel channel = this.getChannel(player);
      if (channel.pipeline().names().contains(this.injectPosition)) {
         this.uninject(player);

         try {
            channel.pipeline().addBefore(this.injectPosition, "TAB", (ChannelHandler)this.getChannelFunction().apply(player));
         } catch (NoSuchElementException | IllegalArgumentException var4) {
         }
      }
   }

   @Override
   public void uninject(@NotNull TabPlayer player) {
      Channel channel = this.getChannel(player);

      try {
         if (channel.pipeline().names().contains("TAB")) {
            channel.pipeline().remove("TAB");
         }
      } catch (NoSuchElementException var4) {
      }
   }

   @Generated
   public NettyPipelineInjector(@NotNull String injectPosition) {
      if (injectPosition == null) {
         throw new NullPointerException("injectPosition is marked non-null but is null");
      }

      this.injectPosition = injectPosition;
   }

   @Generated
   public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
      return this.channelFunction;
   }

   public static class TabChannelDuplexHandler extends ChannelDuplexHandler {
      protected final TabPlayer player;

      public void write(@NotNull ChannelHandlerContext context, @Nullable Object packet, @NotNull ChannelPromise channelPromise) {
         Object newPacket = packet;

         try {
            if (newPacket == null) {
               return;
            }

            if (this.player.getVersion().getMinorVersion() >= 8) {
               newPacket = ((TrackedTabList)this.player.getTabList()).onPacketSend(newPacket);
            }

            newPacket = ((SafeScoreboard)this.player.getScoreboard()).onPacketSend(newPacket);
         } catch (Throwable e) {
            TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
         }

         try {
            super.write(context, newPacket, channelPromise);
         } catch (Throwable e) {
            TAB.getInstance().getErrorManager().printError(String.format("Failed to forward packet %s to %s", newPacket, this.player.getName()), e);
         }
      }

      @Generated
      public TabChannelDuplexHandler(TabPlayer player) {
         this.player = player;
      }
   }
}
