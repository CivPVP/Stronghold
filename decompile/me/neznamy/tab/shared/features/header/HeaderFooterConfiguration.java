package me.neznamy.tab.shared.features.header;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeaderFooterConfiguration {
   @NotNull
   private final LinkedHashMap<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> designs;

   @NotNull
   public static HeaderFooterConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "designs"));
      LinkedHashMap<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> designs = new LinkedHashMap<>();
      ConfigurationSection designsSection = section.getConfigurationSection("designs");

      for (Object key : designsSection.getKeys()) {
         designs.put(key.toString(), HeaderFooterConfiguration.HeaderFooterDesignDefinition.fromSection(designsSection.getConfigurationSection(key.toString())));
      }

      checkChain(section, designs);
      return new HeaderFooterConfiguration(designs);
   }

   private static void checkChain(@NotNull ConfigurationSection section, Map<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> scoreboards) {
      String noConditionDesign = null;

      for (Entry<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> entry : scoreboards.entrySet()) {
         if (noConditionDesign != null) {
            section.startupWarn(
               "Header/footer design \""
                  + noConditionDesign
                  + "\" has no display condition set, however, there is another design in the chain ("
                  + entry.getKey()
                  + "). Designs are checked from top to bottom until a design with meeting condition or no condition is found. Because of this, the design ("
                  + entry.getKey()
                  + ") after the no-condition design ("
                  + noConditionDesign
                  + ") will never be displayed."
            );
         } else if (entry.getValue().displayCondition == null) {
            noConditionDesign = entry.getKey();
         }
      }
   }

   @NotNull
   @Generated
   public LinkedHashMap<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> getDesigns() {
      return this.designs;
   }

   @Generated
   public HeaderFooterConfiguration(@NotNull LinkedHashMap<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> designs) {
      if (designs == null) {
         throw new NullPointerException("designs is marked non-null but is null");
      }

      this.designs = designs;
   }

   public static class HeaderFooterDesignDefinition {
      @Nullable
      private final String displayCondition;
      @NonNull
      private final List<String> header;
      @NonNull
      private final List<String> footer;

      public static HeaderFooterConfiguration.HeaderFooterDesignDefinition fromSection(@NotNull ConfigurationSection section) {
         section.checkForUnknownKey(Arrays.asList("header", "footer", "display-condition"));
         return new HeaderFooterConfiguration.HeaderFooterDesignDefinition(
            section.getString("display-condition"),
            section.getStringList("header", Collections.emptyList()),
            section.getStringList("footer", Collections.emptyList())
         );
      }

      @Nullable
      @Generated
      public String getDisplayCondition() {
         return this.displayCondition;
      }

      @NonNull
      @Generated
      public List<String> getHeader() {
         return this.header;
      }

      @NonNull
      @Generated
      public List<String> getFooter() {
         return this.footer;
      }

      @Generated
      public HeaderFooterDesignDefinition(@Nullable String displayCondition, @NonNull List<String> header, @NonNull List<String> footer) {
         if (header == null) {
            throw new NullPointerException("header is marked non-null but is null");
         }

         if (footer == null) {
            throw new NullPointerException("footer is marked non-null but is null");
         }

         this.displayCondition = displayCondition;
         this.header = header;
         this.footer = footer;
      }
   }
}
