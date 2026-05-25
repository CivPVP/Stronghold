package me.neznamy.tab.shared.chat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabTextColor {
   public static final Map<Character, TabTextColor> LEGACY_COLORS = new LinkedHashMap<>();
   public static final TabTextColor BLACK = new TabTextColor(EnumChatFormat.BLACK, 0);
   public static final TabTextColor DARK_BLUE = new TabTextColor(EnumChatFormat.DARK_BLUE, 170);
   public static final TabTextColor DARK_GREEN = new TabTextColor(EnumChatFormat.DARK_GREEN, 43520);
   public static final TabTextColor DARK_AQUA = new TabTextColor(EnumChatFormat.DARK_AQUA, 43690);
   public static final TabTextColor DARK_RED = new TabTextColor(EnumChatFormat.DARK_RED, 11141120);
   public static final TabTextColor DARK_PURPLE = new TabTextColor(EnumChatFormat.DARK_PURPLE, 11141290);
   public static final TabTextColor GOLD = new TabTextColor(EnumChatFormat.GOLD, 16755200);
   public static final TabTextColor GRAY = new TabTextColor(EnumChatFormat.GRAY, 11184810);
   public static final TabTextColor DARK_GRAY = new TabTextColor(EnumChatFormat.DARK_GRAY, 5592405);
   public static final TabTextColor BLUE = new TabTextColor(EnumChatFormat.BLUE, 5592575);
   public static final TabTextColor GREEN = new TabTextColor(EnumChatFormat.GREEN, 5635925);
   public static final TabTextColor AQUA = new TabTextColor(EnumChatFormat.AQUA, 5636095);
   public static final TabTextColor RED = new TabTextColor(EnumChatFormat.RED, 16733525);
   public static final TabTextColor LIGHT_PURPLE = new TabTextColor(EnumChatFormat.LIGHT_PURPLE, 16733695);
   public static final TabTextColor YELLOW = new TabTextColor(EnumChatFormat.YELLOW, 16777045);
   public static final TabTextColor WHITE = new TabTextColor(EnumChatFormat.WHITE, 16777215);
   public static final TabTextColor OBFUSCATED = new TabTextColor(EnumChatFormat.OBFUSCATED, 0);
   public static final TabTextColor BOLD = new TabTextColor(EnumChatFormat.BOLD, 0);
   public static final TabTextColor STRIKETHROUGH = new TabTextColor(EnumChatFormat.STRIKETHROUGH, 0);
   public static final TabTextColor UNDERLINE = new TabTextColor(EnumChatFormat.UNDERLINE, 0);
   public static final TabTextColor ITALIC = new TabTextColor(EnumChatFormat.ITALIC, 0);
   public static final TabTextColor RESET = new TabTextColor(EnumChatFormat.RESET, 0);
   private static final TabTextColor[] legacyColorArray = LEGACY_COLORS.values().toArray(new TabTextColor[0]);
   private int rgb = -1;
   @Nullable
   private EnumChatFormat legacyColor;
   @Nullable
   private String hexCode;

   public TabTextColor(@NotNull String hexCode) {
      this.hexCode = hexCode;
   }

   private TabTextColor(@NotNull EnumChatFormat legacyColor, int rgb) {
      this.rgb = rgb;
      this.legacyColor = legacyColor;
      this.hexCode = String.format("%06X", rgb);
      LEGACY_COLORS.put(legacyColor.getCharacter(), this);
   }

   public TabTextColor(int rgb) {
      this.rgb = rgb;
   }

   public TabTextColor(int red, int green, int blue) {
      this.rgb = (red << 16) + (green << 8) + blue;
   }

   private EnumChatFormat loadClosestColor() {
      double minMaxDist = 9999.0;
      EnumChatFormat closestColor = EnumChatFormat.WHITE;

      for (TabTextColor color : legacyColorArray) {
         int rDiff = Math.abs(color.getRed() - this.getRed());
         int gDiff = Math.abs(color.getGreen() - this.getGreen());
         int bDiff = Math.abs(color.getBlue() - this.getBlue());
         double maxDist = rDiff;
         if (gDiff > maxDist) {
            maxDist = gDiff;
         }

         if (bDiff > maxDist) {
            maxDist = bDiff;
         }

         if (maxDist < minMaxDist) {
            minMaxDist = maxDist;
            closestColor = color.legacyColor;
         }
      }

      return closestColor;
   }

   public int getRed() {
      return this.getRgb() >> 16 & 0xFF;
   }

   public int getGreen() {
      return this.getRgb() >> 8 & 0xFF;
   }

   public int getBlue() {
      return this.getRgb() & 0xFF;
   }

   public int getRgb() {
      if (this.rgb == -1) {
         this.rgb = Integer.parseInt(this.getHexCode(), 16);
      }

      return this.rgb;
   }

   @NotNull
   public EnumChatFormat getLegacyColor() {
      if (this.legacyColor == null) {
         this.legacyColor = this.loadClosestColor();
      }

      return this.legacyColor;
   }

   @NotNull
   public String getHexCode() {
      if (this.hexCode == null) {
         this.hexCode = String.format("%06X", this.rgb);
      }

      return this.hexCode;
   }

   @Nullable
   public static TabTextColor getLegacyByChar(char c) {
      return LEGACY_COLORS.get(c);
   }
}
