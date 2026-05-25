package me.neznamy.tab.shared.features.header;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class HeaderFooterDesign extends RefreshableFeature {
   private final HeaderFooter feature;
   private final String name;
   private final HeaderFooterConfiguration.HeaderFooterDesignDefinition definition;
   private final Condition displayCondition;

   public HeaderFooterDesign(@NonNull HeaderFooter feature, @NonNull String name, @NonNull HeaderFooterConfiguration.HeaderFooterDesignDefinition definition) {
      if (feature == null) {
         throw new NullPointerException("feature is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (definition == null) {
         throw new NullPointerException("definition is marked non-null but is null");
      }

      this.feature = feature;
      this.name = name;
      this.definition = definition;
      this.displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(definition.getDisplayCondition());
      if (this.displayCondition != null) {
         feature.addUsedPlaceholder(TabConstants.Placeholder.condition(this.displayCondition.getName()));
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating header/footer";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.headerFooterData.activeDesign == this) {
         this.feature.sendHeaderFooter(refreshed);
      }
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.feature.getFeatureName();
   }

   public boolean isConditionMet(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return this.displayCondition == null || this.displayCondition.isMet(p);
      }
   }

   @Generated
   public HeaderFooterDesign(HeaderFooter feature, String name, HeaderFooterConfiguration.HeaderFooterDesignDefinition definition, Condition displayCondition) {
      this.feature = feature;
      this.name = name;
      this.definition = definition;
      this.displayCondition = displayCondition;
   }

   @Generated
   public HeaderFooter getFeature() {
      return this.feature;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public HeaderFooterConfiguration.HeaderFooterDesignDefinition getDefinition() {
      return this.definition;
   }

   @Generated
   public Condition getDisplayCondition() {
      return this.displayCondition;
   }
}
