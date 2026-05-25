package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderLowToHigh extends SortingType {
   public PlaceholderLowToHigh(Sorting sorting, String sortingPlaceholder) {
      super(sorting, "PLACEHOLDER_LOW_TO_HIGH", sortingPlaceholder);
   }

   @Override
   public String getChars(@NotNull TabPlayer p) {
      if (!this.valid) {
         return "";
      }

      String output = this.setPlaceholders(p);
      p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> " + this.sortingPlaceholder + " returned \"&e" + output + "&r\". &r";
      return this.compressNumber(1.073741823E9 + this.parseDouble(this.sortingPlaceholder, output, 0.0, p));
   }
}
