package me.neznamy.tab.shared.features.sorting;

import java.util.Collections;
import java.util.List;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class SortingConfiguration {
   private final boolean caseSensitiveSorting;
   private final List<String> sortingTypes;

   @NotNull
   public static SortingConfiguration fromSection(@NotNull ConfigurationSection section) {
      return new SortingConfiguration(section.getBoolean("case-sensitive-sorting", true), section.getStringList("sorting-types", Collections.emptyList()));
   }

   @Generated
   public boolean isCaseSensitiveSorting() {
      return this.caseSensitiveSorting;
   }

   @Generated
   public List<String> getSortingTypes() {
      return this.sortingTypes;
   }

   @Generated
   public SortingConfiguration(boolean caseSensitiveSorting, List<String> sortingTypes) {
      this.caseSensitiveSorting = caseSensitiveSorting;
      this.sortingTypes = sortingTypes;
   }
}
