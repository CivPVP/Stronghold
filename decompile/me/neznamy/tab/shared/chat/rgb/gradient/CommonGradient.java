package me.neznamy.tab.shared.chat.rgb.gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public class CommonGradient implements GradientPattern {
   @NotNull
   private final Pattern pattern;
   @NotNull
   private final String containCheck;
   private final int startColorStart;
   private final int messageStart;
   private final int endColorStartSub;

   @NotNull
   @Override
   public String applyPattern(@NotNull String text, @NotNull TriFunction<TabTextColor, String, TabTextColor, String> gradientFunction) {
      if (!text.contains(this.containCheck)) {
         return text;
      }

      String replaced = text;
      Matcher m = this.pattern.matcher(replaced);

      while (m.find()) {
         String format = m.group();
         TabTextColor start = new TabTextColor(format.substring(this.startColorStart, this.startColorStart + 6));
         String message = format.substring(this.messageStart, format.length() - 10);
         TabTextColor end = new TabTextColor(format.substring(format.length() - this.endColorStartSub, format.length() - this.endColorStartSub + 6));
         replaced = replaced.replace(format, gradientFunction.apply(start, message, end));
      }

      return replaced;
   }

   @Generated
   public CommonGradient(@NotNull Pattern pattern, @NotNull String containCheck, int startColorStart, int messageStart, int endColorStartSub) {
      if (pattern == null) {
         throw new NullPointerException("pattern is marked non-null but is null");
      }

      if (containCheck == null) {
         throw new NullPointerException("containCheck is marked non-null but is null");
      }

      this.pattern = pattern;
      this.containCheck = containCheck;
      this.startColorStart = startColorStart;
      this.messageStart = messageStart;
      this.endColorStartSub = endColorStartSub;
   }
}
