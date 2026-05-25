package me.neznamy.tab.shared.chat.hook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdventureShadowHook {
   @Nullable
   public static Integer getShadowColor(@NotNull Component component) {
      ShadowColor color = component.shadowColor();
      return color == null ? null : color.value();
   }

   public static void setShadowColor(@NotNull Builder style, @Nullable Integer shadowColor) {
      if (shadowColor != null) {
         style.shadowColor(ShadowColor.shadowColor(shadowColor));
      }
   }
}
