package me.neznamy.tab.shared.chat;

import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public enum EnumChatFormat {
   BLACK('0', true),
   DARK_BLUE('1', true),
   DARK_GREEN('2', true),
   DARK_AQUA('3', true),
   DARK_RED('4', true),
   DARK_PURPLE('5', true),
   GOLD('6', true),
   GRAY('7', true),
   DARK_GRAY('8', true),
   BLUE('9', true),
   GREEN('a', true),
   AQUA('b', true),
   RED('c', true),
   LIGHT_PURPLE('d', true),
   YELLOW('e', true),
   WHITE('f', true),
   OBFUSCATED('k', false),
   BOLD('l', false),
   STRIKETHROUGH('m', false),
   UNDERLINE('n', false),
   ITALIC('o', false),
   RESET('r', false);

   private final char character;
   private final boolean color;

   @NotNull
   public static String color(@NotNull String textToTranslate) {
      if (!textToTranslate.contains("&")) {
         return textToTranslate;
      }

      char[] b = textToTranslate.toCharArray();

      for (int i = 0; i < b.length - 1; i++) {
         if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx#".indexOf(b[i + 1]) > -1) {
            b[i] = 167;
            b[i + 1] = Character.toLowerCase(b[i + 1]);
         }
      }

      return new String(b);
   }

   @Generated
   public char getCharacter() {
      return this.character;
   }

   @Generated
   public boolean isColor() {
      return this.color;
   }

   @Generated
   EnumChatFormat(final char character, final boolean color) {
      this.character = character;
      this.color = color;
   }
}
