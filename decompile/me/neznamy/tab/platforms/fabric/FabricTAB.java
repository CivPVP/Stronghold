package me.neznamy.tab.platforms.fabric;

import com.mojang.brigadier.CommandDispatcher;
import me.neznamy.tab.shared.TAB;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_5268;
import org.jetbrains.annotations.NotNull;

public class FabricTAB implements DedicatedServerModInitializer {
   public static CommandDispatcher<class_2168> COMMAND_DISPATCHER;

   public void onInitializeServer() {
      CommandRegistrationCallback.EVENT
         .register((CommandRegistrationCallback)(dispatcher, commandBuildContext, commandSelection) -> COMMAND_DISPATCHER = dispatcher);
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> TAB.create(new FabricPlatform(server)));
      ServerLifecycleEvents.SERVER_STOPPING.register((ServerStopping)server -> TAB.getInstance().unload());
   }

   @NotNull
   public static String getLevelName(@NotNull class_1937 level) {
      String path = level.method_27983().method_29177().method_12832();
      String var10000 = ((class_5268)level.method_8401()).method_150();

      return var10000 + switch (path) {
         case "overworld" -> "";
         case "the_nether" -> "_nether";
         default -> "_" + path;
      };
   }
}
