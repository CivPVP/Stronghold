package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class Groups extends SortingType {
   private final LinkedHashMap<String, Integer> sortedGroups;

   public Groups(Sorting sorting, String options) {
      super(sorting, "GROUPS", "%group%");
      this.sortedGroups = this.convertSortingElements(options.split(","));
   }

   @Override
   public String getChars(@NotNull TabPlayer p) {
      String group = p.getGroup().toLowerCase();
      int position;
      if (!this.sortedGroups.containsKey(group)) {
         TAB.getInstance().getConfigHelper().runtime().groupNotInSortingList(this.sortedGroups.keySet(), group, p);
         position = this.sortedGroups.size() + 1;
         p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> &cPrimary group (&e" + p.getGroup() + "&c) is not in sorting list. &r";
      } else {
         position = this.sortedGroups.get(group);
         p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> Primary group (&e" + p.getGroup() + "&r) is &a#" + position + "&r in sorting list.";
      }

      return String.valueOf((char)(position + 47));
   }
}
