package me.neznamy.tab.shared.chat.rgb;

import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.Generated;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.rgb.format.BukkitFormat;
import me.neznamy.tab.shared.chat.rgb.format.CommonFormatter;
import me.neznamy.tab.shared.chat.rgb.format.RGBFormatter;
import me.neznamy.tab.shared.chat.rgb.gradient.CMIGradient;
import me.neznamy.tab.shared.chat.rgb.gradient.CommonGradient;
import me.neznamy.tab.shared.chat.rgb.gradient.GradientPattern;
import me.neznamy.tab.shared.chat.rgb.gradient.NexEngineGradient;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public class RGBUtils {
   private static final RGBUtils instance = new RGBUtils();
   private final RGBFormatter[] formats = new RGBFormatter[]{
      new BukkitFormat(),
      new CommonFormatter(Pattern.compile("\\{#[0-9a-fA-F]{6}}"), "{#"),
      new CommonFormatter(Pattern.compile("§#[0-9a-fA-F]{6}"), "§#"),
      new CommonFormatter(Pattern.compile("#<[0-9a-fA-F]{6}>"), "#<")
   };
   private final GradientPattern[] gradients = new GradientPattern[]{
      new CMIGradient(),
      new CommonGradient(Pattern.compile("<#[0-9a-fA-F]{6}>(?:(?!<#[0-9a-fA-F]{6}>).)*?</#[0-9a-fA-F]{6}>"), "<#", 2, 9, 7),
      new CommonGradient(Pattern.compile("<\\$#[0-9a-fA-F]{6}>.*?<\\$#[0-9a-fA-F]{6}>"), "<$", 3, 10, 7),
      new NexEngineGradient()
   };

   @NotNull
   public String applyFormats(
      @NotNull String text,
      @NotNull TriFunction<TabTextColor, String, TabTextColor, String> gradientFunction,
      @NotNull Function<TabTextColor, String> rgbFunction
   ) {
      String replaced = text;

      for (GradientPattern pattern : this.gradients) {
         replaced = pattern.applyPattern(replaced, gradientFunction);
      }

      for (RGBFormatter formatter : this.formats) {
         replaced = formatter.reformat(replaced, rgbFunction);
      }

      return replaced;
   }

   @Generated
   public static RGBUtils getInstance() {
      return instance;
   }
}
