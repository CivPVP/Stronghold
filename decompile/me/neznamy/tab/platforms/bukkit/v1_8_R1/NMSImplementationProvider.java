package me.neznamy.tab.platforms.bukkit.v1_8_R1;

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
import net.minecraft.server.v1_8_R1.NetworkManager;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

public class NMSImplementationProvider implements ImplementationProvider {
   private static final Field channel = ReflectionUtils.getOnlyField(NetworkManager.class, Channel.class);
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
      return player -> (Channel)channel.get(((CraftPlayer)player.getPlayer()).getHandle().playerConnection.networkManager);
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
