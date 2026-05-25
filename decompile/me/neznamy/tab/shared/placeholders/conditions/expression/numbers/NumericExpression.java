package me.neznamy.tab.shared.placeholders.conditions.expression.numbers;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.expression.ComparatorExpression;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class NumericExpression extends ComparatorExpression {
   private boolean leftSideStatic;
   private float leftSideValue;
   private boolean rightSideStatic;
   private float rightSideValue;

   protected NumericExpression(@NotNull String leftSide, @NotNull String rightSide) {
      super(leftSide, rightSide);

      try {
         this.leftSideValue = Float.parseFloat(leftSide);
         this.leftSideStatic = true;
      } catch (NumberFormatException var5) {
      }

      try {
         this.rightSideValue = Float.parseFloat(rightSide);
         this.rightSideStatic = true;
      } catch (NumberFormatException var4) {
      }
   }

   public double getLeftSide(@NotNull TabPlayer p) {
      if (this.leftSideStatic) {
         return this.leftSideValue;
      }

      String value = this.parseLeftSide(p);
      if (value.contains(",")) {
         value = value.replace(",", "");
      }

      return this.parseDouble(this.leftSide, value, p);
   }

   public double getRightSide(@NotNull TabPlayer p) {
      if (this.rightSideStatic) {
         return this.rightSideValue;
      }

      String value = this.parseRightSide(p);
      if (value.contains(",")) {
         value = value.replace(",", "");
      }

      return this.parseDouble(this.rightSide, value, p);
   }

   private double parseDouble(@NotNull String placeholder, @NotNull String output, TabPlayer player) {
      try {
         return Double.parseDouble(output);
      } catch (NumberFormatException e) {
         TAB.getInstance().getConfigHelper().runtime().invalidNumberForCondition(placeholder, output, player);
         return 0.0;
      }
   }
}
