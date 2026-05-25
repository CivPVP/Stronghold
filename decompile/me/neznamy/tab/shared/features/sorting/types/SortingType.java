package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class SortingType {
   protected final Sorting sorting;
   private final String displayName;
   protected final int DEFAULT_NUMBER = 1073741823;
   protected String sortingPlaceholder;
   protected final boolean valid;

   protected SortingType(Sorting sorting, String displayName, String sortingPlaceholder) {
      this.sorting = sorting;
      this.displayName = displayName;
      if (sortingPlaceholder.startsWith("%") && sortingPlaceholder.endsWith("%")) {
         sorting.addUsedPlaceholder(sortingPlaceholder);
         this.sortingPlaceholder = sortingPlaceholder;
         this.valid = true;
      } else {
         TAB.getInstance().getConfigHelper().startup().invalidSortingPlaceholder(sortingPlaceholder, this);
         this.valid = false;
      }
   }

   protected String setPlaceholders(TabPlayer player) {
      if (this.sortingPlaceholder == null) {
         return "";
      }

      TabPlaceholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(this.sortingPlaceholder);
      return placeholder.set(placeholder.getIdentifier(), player);
   }

   protected LinkedHashMap<String, Integer> convertSortingElements(String[] elements) {
      LinkedHashMap<String, Integer> sortedGroups = new LinkedHashMap<>();
      int index = 1;

      for (String element : elements) {
         for (String element0 : element.split("\\|")) {
            sortedGroups.put(EnumChatFormat.color(element0.trim().toLowerCase()), index);
         }

         index++;
      }

      return sortedGroups;
   }

   public String compressNumber(double number) {
      int wholePart = (int)number;
      int base = 65534;
      char decimalChar = (char)((number - wholePart) * base);
      if (decimalChar >= '\\') {
         decimalChar++;
      }

      StringBuilder sb = new StringBuilder();

      while (wholePart > 0) {
         char digit = (char)(wholePart % base);
         if (digit >= '\\') {
            digit++;
         }

         sb.insert(0, digit);
         wholePart /= base;
      }

      while (sb.length() < 2) {
         sb.insert(0, '\u0000');
      }

      sb.append(decimalChar);
      return sb.toString();
   }

   public double parseDouble(@NotNull String placeholder, @NotNull String output, double defaultValue, TabPlayer player) {
      try {
         return Double.parseDouble(output.replace(",", "."));
      } catch (NumberFormatException e) {
         TAB.getInstance().getConfigHelper().runtime().invalidInputForNumericSorting(this, placeholder, output, player);
         return defaultValue;
      }
   }

   public abstract String getChars(@NotNull TabPlayer var1);

   @Generated
   public SortingType(Sorting sorting, String displayName, boolean valid) {
      this.sorting = sorting;
      this.displayName = displayName;
      this.valid = valid;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }
}
