package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderZtoA extends SortingType {
   public PlaceholderZtoA(Sorting sorting, String sortingPlaceholder) {
      super(sorting, "PLACEHOLDER_Z_TO_A", sortingPlaceholder);
   }

   @Override
   public String getChars(@NotNull TabPlayer p) {
      char[] chars = this.setPlaceholders(p).toCharArray();
      p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> " + this.sortingPlaceholder + " returned \"&e" + new String(chars) + "&r\". &r";

      for (int i = 0; i < chars.length; i++) {
         char c = chars[i];
         if (c >= 'A' && c <= 'Z') {
            chars[i] = (char)(155 - c);
         }

         if (c >= 'a' && c <= 'z') {
            chars[i] = (char)(219 - c);
         }
      }

      String s = new String(chars);
      return this.sorting.getConfiguration().isCaseSensitiveSorting() ? s : s.toLowerCase();
   }
}
