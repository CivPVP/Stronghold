package me.neznamy.tab.platforms.bukkit.v1_9_R1;

import io.netty.channel.Channel;
import lombok.Generated;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

public class NMSImplementationProvider implements ImplementationProvider {
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
      return player -> ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.networkManager.channel;
   }

   @Override
   public int getPing(@NotNull BukkitTabPlayer player) {
      return ((CraftPlayer)player.getPlayer()).getHandle().ping;
   }

   @NotNull
   @Generated
   @Override
   public ComponentConverter<?> getComponentConverter() {
      return this.componentConverter;
   }
}
