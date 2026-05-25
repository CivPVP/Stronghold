package me.neznamy.tab.platforms.neoforge;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

public class NeoForgeEventListener implements EventListener<ServerPlayer> {
   public void register() {
      IEventBus eventBus = NeoForge.EVENT_BUS;
      eventBus.addListener(event -> this.join((ServerPlayer)event.getEntity()));
      eventBus.addListener(event -> this.quit(event.getEntity().getUUID()));
      eventBus.addListener(event -> {
         ServerPlayer player = (ServerPlayer)event.getEntity();
         this.replacePlayer(player.getUUID(), player);
         this.worldChange(player.getUUID(), NeoForgeTAB.getLevelName(player.level()));
      });
      eventBus.addListener(event -> this.worldChange(event.getEntity().getUUID(), NeoForgeTAB.getLevelName(event.getEntity().level())));
   }

   @NotNull
   public TabPlayer createPlayer(@NotNull ServerPlayer player) {
      return new NeoForgeTabPlayer((NeoForgePlatform)TAB.getInstance().getPlatform(), player);
   }
}
