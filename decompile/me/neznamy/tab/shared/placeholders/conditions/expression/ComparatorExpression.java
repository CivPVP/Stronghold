package me.neznamy.tab.shared.placeholders.conditions.expression;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ComparatorExpression extends ConditionalExpression {
   @NotNull
   protected final String leftSide;
   @NotNull
   protected final String rightSide;
   @NotNull
   private final String[] leftSidePlaceholders;
   @NotNull
   private final String[] rightSidePlaceholders;

   protected ComparatorExpression(@NonNull String leftSide, @NonNull String rightSide) {
      if (leftSide == null) {
         throw new NullPointerException("leftSide is marked non-null but is null");
      }

      if (rightSide == null) {
         throw new NullPointerException("rightSide is marked non-null but is null");
      }

      this.leftSide = leftSide;
      this.rightSide = rightSide;
      this.leftSidePlaceholders = PlaceholderManagerImpl.detectPlaceholders(leftSide).toArray(new String[0]);
      this.rightSidePlaceholders = PlaceholderManagerImpl.detectPlaceholders(rightSide).toArray(new String[0]);
   }

   @NotNull
   public String parseLeftSide(@NotNull TabPlayer p) {
      return this.parseSide(p, this.leftSide, this.leftSidePlaceholders);
   }

   @NotNull
   public String parseRightSide(@NotNull TabPlayer p) {
      return this.parseSide(p, this.rightSide, this.rightSidePlaceholders);
   }

   @NotNull
   public String parseSide(@NotNull TabPlayer p, @NotNull String value, @NotNull String[] placeholders) {
      String result = value;

      for (String identifier : placeholders) {
         result = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).set(result, p);
      }

      return EnumChatFormat.color(result);
   }

   @NotNull
   @Generated
   public String getLeftSide() {
      return this.leftSide;
   }

   @NotNull
   @Generated
   public String getRightSide() {
      return this.rightSide;
   }

   @NotNull
   @Generated
   public String[] getLeftSidePlaceholders() {
      return this.leftSidePlaceholders;
   }

   @NotNull
   @Generated
   public String[] getRightSidePlaceholders() {
      return this.rightSidePlaceholders;
   }
}
