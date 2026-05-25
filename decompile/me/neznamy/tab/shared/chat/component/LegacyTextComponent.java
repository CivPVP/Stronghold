package me.neznamy.tab.shared.chat.component;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import org.jetbrains.annotations.NotNull;

public class LegacyTextComponent extends TabTextComponent {
   protected LegacyTextComponent(@NotNull String text) {
      super(text);
   }

   @NotNull
   @Override
   public String toLegacyText() {
      return this.text;
   }

   @NotNull
   @Override
   protected TabStyle fetchLastStyle() {
      TabStyle modifier = new TabStyle();
      char[] chars = this.text.toCharArray();

      for (int index = chars.length - 2; index >= 0; index--) {
         if (chars[index] == 167) {
            TabTextColor color = TabTextColor.getLegacyByChar(chars[index + 1]);
            if (color != null) {
               if (color == TabTextColor.BOLD) {
                  modifier.setBold(true);
               } else if (color == TabTextColor.ITALIC) {
                  modifier.setItalic(true);
               } else if (color == TabTextColor.UNDERLINE) {
                  modifier.setUnderlined(true);
               } else if (color == TabTextColor.STRIKETHROUGH) {
                  modifier.setStrikethrough(true);
               } else {
                  if (color != TabTextColor.OBFUSCATED) {
                     modifier.setColor(color);
                     break;
                  }

                  modifier.setObfuscated(true);
               }
            }
         }
      }

      return modifier;
   }
}
