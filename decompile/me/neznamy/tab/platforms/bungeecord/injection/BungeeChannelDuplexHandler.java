package me.neznamy.tab.platforms.bungeecord.injection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.md_5.bungee.protocol.packet.Login;
import org.jetbrains.annotations.NotNull;

public class BungeeChannelDuplexHandler extends NettyPipelineInjector.TabChannelDuplexHandler {
   public BungeeChannelDuplexHandler(TabPlayer player) {
      super(player);
   }

   @Override
   public void write(@NotNull ChannelHandlerContext context, @NotNull Object packet, @NotNull ChannelPromise channelPromise) {
      if (packet instanceof Login) {
         ((SafeScoreboard)this.player.getScoreboard()).setFrozen(true);
         CpuManager cpu = TAB.getInstance().getCpu();
         cpu.getProcessingThread().executeLater(new TimedCaughtTask(cpu, () -> {
            ((SafeScoreboard)this.player.getScoreboard()).setFrozen(false);
            this.player.getScoreboard().resend();
            if (this.player.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
               TAB.getInstance().getFeatureManager().onTabListClear(this.player);
               ((SafeBossBar)this.player.getBossBar()).unfreezeAndResend();
            }
         }, "Pipeline injection", "Login packet"), 200);
      }

      super.write(context, packet, channelPromise);
   }
}
