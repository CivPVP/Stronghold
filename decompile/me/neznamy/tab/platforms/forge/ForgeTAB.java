package me.neznamy.tab.platforms.forge;

import com.mojang.brigadier.CommandDispatcher;
import me.neznamy.tab.shared.TAB;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.DEDICATED_SERVER)
@Mod("tab")
public class ForgeTAB {
   public static CommandDispatcher<CommandSourceStack> COMMAND_DISPATCHER;

   public ForgeTAB() {
      RegisterCommandsEvent.BUS.addListener(event -> COMMAND_DISPATCHER = event.getDispatcher());
      ServerStartingEvent.BUS.addListener(event -> TAB.create(new ForgePlatform(event.getServer())));
      ServerStoppingEvent.BUS.addListener(event -> TAB.getInstance().unload());
   }

   @NotNull
   public static String getLevelName(@NotNull Level level) {
      String path = level.dimension().location().getPath();
      String var10000 = ((ServerLevelData)level.getLevelData()).getLevelName();

      return var10000 + switch (path) {
         case "overworld" -> "";
         case "the_nether" -> "_nether";
         default -> "_" + path;
      };
   }
}
