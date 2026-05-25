package me.neznamy.tab.shared.features.sorting.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class Permissions extends SortingType {
   private final LinkedHashMap<String, Integer> sortedGroups;

   public Permissions(Sorting sorting, String options) {
      super(sorting, "PERMISSIONS", true);
      this.sortedGroups = this.convertSortingElements(options.split(","));
      List<String> placeholders = new ArrayList<>();

      for (String permission : this.sortedGroups.keySet()) {
         String placeholder = "%permission:" + permission + "%";
         placeholders.add(placeholder);
         TAB.getInstance()
            .getPlaceholderManager()
            .registerInternalPlayerPlaceholder(
               placeholder,
               TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval(),
               p -> Boolean.toString(((TabPlayer)p).hasPermission(permission))
            );
      }

      sorting.addUsedPlaceholders(placeholders);
   }

   @Override
   public String getChars(@NotNull TabPlayer p) {
      int position = 0;

      for (String permission : this.sortedGroups.keySet()) {
         if (p.hasPermission(permission)) {
            position = this.sortedGroups.get(permission.toLowerCase());
            p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> Highest sorting permission: &e" + permission + " &a(#" + position + " in list). &r";
            if (p.hasPermission("tab.testpermission")) {
               p.sortingData.teamNameNote = p.sortingData.teamNameNote + "&cThis user appears to have all permissions. Are they OP? &r";
            }
            break;
         }
      }

      if (position == 0) {
         TAB.getInstance().getConfigHelper().runtime().noPermissionFromSortingList(this.sortedGroups.keySet(), p);
         position = this.sortedGroups.size() + 1;
         p.sortingData.teamNameNote = p.sortingData.teamNameNote + "\n-> &cPlayer does not have any of the defined permissions. &r";
      }

      return String.valueOf((char)(position + 47));
   }
}
