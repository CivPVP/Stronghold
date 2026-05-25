package me.neznamy.tab.platforms.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import me.neznamy.tab.shared.TAB;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

@Mod(value = "tab", dist = Dist.DEDICATED_SERVER)
public class NeoForgeTAB {
   public static CommandDispatcher<CommandSourceStack> COMMAND_DISPATCHER;

   public NeoForgeTAB() {
      IEventBus EVENT_BUS = NeoForge.EVENT_BUS;
      EVENT_BUS.addListener(event -> COMMAND_DISPATCHER = event.getDispatcher());
      EVENT_BUS.addListener(event -> TAB.create(new NeoForgePlatform(event.getServer())));
      EVENT_BUS.addListener(event -> TAB.getInstance().unload());
   }

   @NotNull
   public static String getLevelName(@NotNull Level level) {
      String path = level.dimension().identifier().getPath();
      String var10000 = ((ServerLevelData)level.getLevelData()).getLevelName();

      return var10000 + switch (path) {
         case "overworld" -> "";
         case "the_nether" -> "_nether";
         default -> "_" + path;
      };
   }
}
