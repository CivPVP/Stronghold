package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholder extends SortingType {
   private final LinkedHashMap<String, Integer> sortingMap;

   public Placeholder(Sorting sorting, Placeholder.PlaceholderSplitResult result) {
      super(sorting, "PLACEHOLDER", result.placeholder);
      this.sortingMap = this.convertSortingElements(result.values);
   }

   @Nullable
   public static Placeholder.PlaceholderSplitResult splitValue(@NotNull String options) {
      Pattern pattern = Pattern.compile("(%[^%]+%):(.+)");
      Matcher matcher = pattern.matcher(options);
      if (!matcher.matches()) {
         TAB.getInstance()
            .getConfigHelper()
            .startup()
            .invalidSortingLine("PLACEHOLDER:" + options, "Invalid format. Expected \"%placeholder%:value1,value2,...,valueN\".");
         return null;
      } else {
         String placeholder = matcher.group(1);
         String[] values = matcher.group(2).split(",", -1);
         return new Placeholder.PlaceholderSplitResult(placeholder, values);
      }
   }

   @Override
   public String getChars(@NotNull TabPlayer p) {
      if (!this.valid) {
         return "";
      }

      String output = EnumChatFormat.color(this.setPlaceholders(p));
      p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> " + this.sortingPlaceholder + " returned \"&e" + output + "&r\"";
      String cleanOutput = output.trim().toLowerCase(Locale.US);
      int position;
      if (!this.sortingMap.containsKey(cleanOutput)) {
         TAB.getInstance().getConfigHelper().runtime().valueNotInPredefinedValues(this.sortingPlaceholder, this.sortingMap.keySet(), cleanOutput, p);
         position = this.sortingMap.size() + 1;
         p.sortingData.teamNameNote = p.sortingData.teamNameNote + "&c (not in list)&r. ";
      } else {
         position = this.sortingMap.get(cleanOutput);
         p.sortingData.teamNameNote = p.sortingData.teamNameNote + "&r &a(#" + position + " in list). &r";
      }

      return String.valueOf((char)(position + 47));
   }

   public static class PlaceholderSplitResult {
      @NotNull
      private final String placeholder;
      @NotNull
      private final String[] values;

      @Generated
      public PlaceholderSplitResult(@NotNull String placeholder, @NotNull String[] values) {
         if (placeholder == null) {
            throw new NullPointerException("placeholder is marked non-null but is null");
         }

         if (values == null) {
            throw new NullPointerException("values is marked non-null but is null");
         }

         this.placeholder = placeholder;
         this.values = values;
      }
   }
}
