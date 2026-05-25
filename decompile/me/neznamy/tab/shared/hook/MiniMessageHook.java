package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.hook.AdventureHook;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniMessageHook {
   @Nullable
   private static final MiniMessage mm = createMiniMessage();

   @Nullable
   private static MiniMessage createMiniMessage() {
      try {
         return MiniMessage.miniMessage();
      } catch (Throwable ignored) {
         return null;
      }
   }

   public static boolean isAvailable() {
      return mm != null && TAB.getInstance().getConfiguration().getConfig().getComponents().isMinimessageSupport();
   }

   @Nullable
   public static TabComponent parseText(@NotNull String text) {
      if (mm == null) {
         return null;
      }

      try {
         return AdventureHook.convert(mm.deserialize(text));
      } catch (Throwable t) {
         TAB.getInstance().getErrorManager().printError("Failed to convert \"" + text + "\" into a MiniMessage component", t);
         return null;
      }
   }
}
