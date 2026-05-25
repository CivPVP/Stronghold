package me.neznamy.tab.shared.chat.rgb.gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public class CMIGradient extends CommonGradient {
   private final Pattern shortcutPattern = Pattern.compile("\\{#[0-9a-fA-F]{6}<>}");

   public CMIGradient() {
      super(Pattern.compile("\\{#[0-9a-fA-F]{6}>}[^{]*\\{#[0-9a-fA-F]{6}<}"), "{#", 2, 10, 8);
   }

   @NotNull
   @Override
   public String applyPattern(@NotNull String text, @NotNull TriFunction<TabTextColor, String, TabTextColor, String> gradientFunction) {
      String replaced = text;
      if (replaced.contains("<>}")) {
         Matcher m = this.shortcutPattern.matcher(replaced);

         while (m.find()) {
            String format = m.group();
            String code = format.substring(2, 8);
            replaced = replaced.replace(format, "{#" + code + "<}{#" + code + ">}");
         }
      }

      return super.applyPattern(replaced, gradientFunction);
   }
}
