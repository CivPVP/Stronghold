package me.neznamy.tab.shared.chat.rgb.format;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import me.neznamy.tab.shared.chat.TabTextColor;
import org.jetbrains.annotations.NotNull;

public class CommonFormatter implements RGBFormatter {
   private final Pattern pattern;
   private final String stringCheck;

   @NotNull
   @Override
   public String reformat(@NotNull String text, @NotNull Function<TabTextColor, String> rgbFunction) {
      if (!text.contains(this.stringCheck)) {
         return text;
      }

      String replaced = text;
      Matcher m = this.pattern.matcher(replaced);

      while (m.find()) {
         String group = m.group();
         String hexCode = group.substring(2, 8);
         replaced = replaced.replace(group, rgbFunction.apply(new TabTextColor(hexCode)));
      }

      return replaced;
   }

   @Generated
   public CommonFormatter(Pattern pattern, String stringCheck) {
      this.pattern = pattern;
      this.stringCheck = stringCheck;
   }
}
