package me.neznamy.tab.platforms.bungeecord.injection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Protocol.DirectionData;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeserializableBungeeChannelDuplexHandler extends BungeeChannelDuplexHandler {
   @Nullable
   private static Object directionData;
   @Nullable
   private static Method getId;
   @NotNull
   private final Class<? extends DefinedPacket>[] extraPacketClasses = new Class[]{Team.class, ScoreboardDisplay.class, ScoreboardObjective.class};
   @NotNull
   private final Supplier<DefinedPacket>[] extraPacketSuppliers = new Supplier[]{Team::new, ScoreboardDisplay::new, ScoreboardObjective::new};

   public DeserializableBungeeChannelDuplexHandler(@NotNull TabPlayer player) {
      super(player);
   }

   @Override
   public void write(@NotNull ChannelHandlerContext context, @NotNull Object packet, @NotNull ChannelPromise channelPromise) {
      long time = System.nanoTime();
      Object modifiedPacket = packet instanceof ByteBuf ? this.deserialize((ByteBuf)packet) : packet;
      TAB.getInstance().getCPUManager().addTime("Packet deserializing", "ByteBuf", System.nanoTime() - time);
      super.write(context, modifiedPacket, channelPromise);
   }

   @NotNull
   private Object deserialize(@NotNull ByteBuf buf) {
      int marker = buf.readerIndex();

      try {
         int packetId = buf.readByte();

         for (int i = 0; i < this.extraPacketClasses.length; i++) {
            ChannelWrapper ch = ((UserConnection)((BungeeTabPlayer)this.player).getPlayer()).getCh();
            if (ch.getEncodeProtocol()
                  .TO_CLIENT
                  .hasPacket(this.extraPacketClasses[i], ((ProxiedPlayer)this.player.getPlayer()).getPendingConnection().getVersion())
               && packetId == this.getPacketId(((BungeeTabPlayer)this.player).getPlayer().getPendingConnection().getVersion(), this.extraPacketClasses[i])) {
               DefinedPacket packet = this.extraPacketSuppliers[i].get();
               packet.read(buf, null, ((ProxiedPlayer)this.player.getPlayer()).getPendingConnection().getVersion());
               buf.release();
               return packet;
            }
         }
      } catch (Exception var7) {
      }

      buf.readerIndex(marker);
      return buf;
   }

   private int getPacketId(int protocolVersion, @NotNull Class<? extends DefinedPacket> clazz) {
      try {
         return getId == null ? -1 : (Integer)getId.invoke(directionData, clazz, protocolVersion);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   static {
      try {
         directionData = ReflectionUtils.setAccessible(Protocol.class.getDeclaredField("TO_CLIENT")).get(Protocol.GAME);
         getId = ReflectionUtils.setAccessible(DirectionData.class.getDeclaredMethod("getId", Class.class, int.class));
      } catch (ReflectiveOperationException exception) {
         TAB.getInstance().getErrorManager().criticalError("Failed to initialize bungee internal fields", exception);
      }
   }
}
