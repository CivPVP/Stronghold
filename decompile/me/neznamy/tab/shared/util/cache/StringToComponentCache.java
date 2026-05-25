package me.neznamy.tab.shared.util.cache;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.hook.MiniMessageHook;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public class StringToComponentCache extends Cache<String, TabComponent> {
   private static final TriFunction<TabTextColor, String, TabTextColor, String> kyoriGradientFormatter = (start, text, end) -> String.format(
      "<gradient:#%s:#%s>%s</gradient>", start.getHexCode(), end.getHexCode(), text
   );
   private static final Function<TabTextColor, String> kyoriRGBFormatter = color -> String.format("<color:#%s>", color.getHexCode());
   private static final Pattern tabToKyori = Pattern.compile("(?<!:)(#([0-9A-Fa-f]{6}))(?![:>])");

   public StringToComponentCache(String name, int cacheSize) {
      super(name, cacheSize);
   }

   @NotNull
   public TabComponent convert(@NotNull String text) {
      if (MiniMessageHook.isAvailable() && text.indexOf(60) != -1) {
         String mmFormatted = RGBUtils.getInstance().applyFormats(text, kyoriGradientFormatter, kyoriRGBFormatter);

         for (TabTextColor format : TabTextColor.LEGACY_COLORS.values()) {
            char legacyChar = format.getLegacyColor().getCharacter();
            String colorName = format == TabTextColor.UNDERLINE ? "underlined" : format.getLegacyColor().name().toLowerCase(Locale.US);

            for (char c : new char[]{legacyChar, Character.toUpperCase(legacyChar)}) {
               String sequence = "§" + c;
               if (mmFormatted.contains(sequence)) {
                  if (format.getLegacyColor().isColor()) {
                     mmFormatted = mmFormatted.replace(
                        sequence, "<bold:false><italic:false><underlined:false><strikethrough:false><obfuscated:false><" + colorName + ">"
                     );
                  } else {
                     mmFormatted = mmFormatted.replace(sequence, "<" + colorName + ">");
                  }
               }
            }
         }

         mmFormatted = tabToKyori(mmFormatted);
         TabComponent component = MiniMessageHook.parseText(mmFormatted);
         if (component != null) {
            return component;
         }
      }

      return !text.contains("#") && !text.contains("§x") && !text.contains("<") ? TabComponent.legacyText(text) : TabComponent.fromColoredText(text);
   }

   @NotNull
   private static String tabToKyori(@NotNull String text) {
      Matcher matcher = tabToKyori.matcher(text);
      StringBuffer result = new StringBuffer();

      while (matcher.find()) {
         matcher.appendReplacement(result, "<color:" + matcher.group(1) + ">");
      }

      matcher.appendTail(result);
      return result.toString();
   }
}
