package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AfterPlayerChange;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.class_3222;
import org.jetbrains.annotations.NotNull;

public class FabricEventListener implements EventListener<class_3222> {
   public void register() {
      ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(connection, server) -> this.quit(connection.field_14140.method_5667()));
      ServerPlayConnectionEvents.JOIN.register((Join)(connection, sender, server) -> this.join(connection.field_14140));
      ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> {
         this.replacePlayer(newPlayer.method_5667(), newPlayer);
         this.worldChange(newPlayer.method_5667(), FabricTAB.getLevelName(newPlayer.method_51469()));
      });
      ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD
         .register((AfterPlayerChange)(player, origin, destination) -> this.worldChange(player.method_5667(), FabricTAB.getLevelName(destination)));
   }

   @NotNull
   public TabPlayer createPlayer(@NotNull class_3222 player) {
      return new FabricTabPlayer((FabricPlatform)TAB.getInstance().getPlatform(), player);
   }
}
