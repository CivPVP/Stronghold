package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAtoZ extends SortingType {
   public PlaceholderAtoZ(Sorting sorting, String sortingPlaceholder) {
      super(sorting, "PLACEHOLDER_A_TO_Z", sortingPlaceholder);
   }

   @Override
   public String getChars(@NotNull TabPlayer p) {
      String output = this.setPlaceholders(p);
      p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> " + this.sortingPlaceholder + " returned \"&e" + output + "&r\". &r";
      return this.sorting.getConfiguration().isCaseSensitiveSorting() ? output : output.toLowerCase();
   }
}
