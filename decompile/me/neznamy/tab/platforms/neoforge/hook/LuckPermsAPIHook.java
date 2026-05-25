package me.neznamy.tab.platforms.neoforge.hook;

import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permission.HasCommandLevel;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class LuckPermsAPIHook {
   private static final boolean luckPerms = ModList.get().isLoaded("luckperms");

   public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
      return source.permissions().hasPermission(new HasCommandLevel(PermissionLevel.OWNERS))
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
