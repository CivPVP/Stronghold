package me.neznamy.tab.platforms.fabric.hook;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_12094;
import net.minecraft.class_2168;
import net.minecraft.class_12087.class_12089;
import org.jetbrains.annotations.NotNull;

public class PermissionsAPIHook {
   private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

   public static boolean hasPermission(@NotNull class_2168 source, @NotNull String permission) {
      return source.method_75037().hasPermission(new class_12089(class_12094.field_63200))
         ? true
         : fabricPermissionsApi && Permissions.check(source, permission);
   }
}
