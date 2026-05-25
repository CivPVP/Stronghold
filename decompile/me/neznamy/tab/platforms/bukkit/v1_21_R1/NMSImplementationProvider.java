package me.neznamy.tab.platforms.bukkit.v1_21_R1;

import io.netty.channel.Channel;
import java.lang.reflect.Field;
import lombok.Generated;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

public class NMSImplementationProvider implements ImplementationProvider {
   private static final Field networkManager = ReflectionUtils.getOnlyField(ServerCommonPacketListenerImpl.class, NetworkManager.class);
   @NotNull
   private final ComponentConverter<?> componentConverter = new NMSComponentConverter();

   @NotNull
   @Override
   public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
      return new NMSPacketScoreboard(player);
   }

   @NotNull
   @Override
   public TabList newTabList(@NotNull BukkitTabPlayer player) {
      return new NMSPacketTabList(player);
   }

   @NotNull
   @Override
   public FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction() {
      return player -> ((NetworkManager)networkManager.get(((CraftPlayer)player.getPlayer()).getHandle().c)).n;
   }

   @Override
   public int getPing(@NotNull BukkitTabPlayer player) {
      return player.getPlayer().getPing();
   }

   @NotNull
   @Generated
   @Override
   public ComponentConverter<?> getComponentConverter() {
      return this.componentConverter;
   }
}
