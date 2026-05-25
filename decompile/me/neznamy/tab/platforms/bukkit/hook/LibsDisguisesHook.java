package me.neznamy.tab.platforms.bukkit.hook;

import me.libraryaddict.disguise.DisguiseAPI;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;

public class LibsDisguisesHook {
   private static boolean installed = ReflectionUtils.classExists("me.libraryaddict.disguise.DisguiseAPI");

   public static boolean isDisguised(TabPlayer player) {
      try {
         return installed && DisguiseAPI.isDisguised(((BukkitTabPlayer)player).getPlayer());
      } catch (LinkageError e) {
         installed = false;
         return false;
      }
   }
}
