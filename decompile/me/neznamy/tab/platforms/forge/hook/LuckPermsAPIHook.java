package me.neznamy.tab.platforms.forge.hook;

import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class LuckPermsAPIHook {
   private static final boolean luckPerms = ModList.get().isLoaded("luckperms");

   public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
      return source.hasPermission(4)
         ? true
         : luckPerms
            && LuckPermsProvider.get()
               .getUserManager()
               .getUser(source.getPlayer().getUUID())
               .getCachedData()
               .getPermissionData()
               .checkPermission(permission)
               .asBoolean();
   }
}
