package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class ForgeEventListener implements EventListener<ServerPlayer> {
   public void register() {
      PlayerLoggedInEvent.BUS.addListener(event -> this.join((ServerPlayer)event.getEntity()));
      PlayerLoggedOutEvent.BUS.addListener(event -> this.quit(event.getEntity().getUUID()));
      PlayerRespawnEvent.BUS.addListener(event -> {
         ServerPlayer player = (ServerPlayer)event.getEntity();
         this.replacePlayer(player.getUUID(), player);
         this.worldChange(player.getUUID(), ForgeTAB.getLevelName(player.level()));
      });
      PlayerChangedDimensionEvent.BUS.addListener(event -> this.worldChange(event.getEntity().getUUID(), ForgeTAB.getLevelName(event.getEntity().level())));
   }

   @NotNull
   public TabPlayer createPlayer(@NotNull ServerPlayer player) {
      return new ForgeTabPlayer((ForgePlatform)TAB.getInstance().getPlatform(), player);
   }
}
