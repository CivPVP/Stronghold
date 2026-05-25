package me.neznamy.tab.shared.placeholders.conditions.expression.string;

import lombok.NonNull;
import me.neznamy.tab.shared.placeholders.conditions.expression.ComparatorExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class NotStartsWith extends ComparatorExpression {
   public NotStartsWith(@NotNull String[] sides) {
      super(sides[0], sides[1]);
   }

   @Override
   public boolean isMet(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return !this.parseLeftSide(p).startsWith(this.parseRightSide(p));
      }
   }

   @NotNull
   @Override
   public ConditionalExpression invert() {
      return new StartsWith(new String[]{this.leftSide, this.rightSide});
   }

   @NotNull
   @Override
   public String toShortFormat() {
      return this.leftSide + "!|-" + this.rightSide;
   }
}
