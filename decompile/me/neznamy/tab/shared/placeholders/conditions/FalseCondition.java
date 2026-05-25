package me.neznamy.tab.shared.placeholders.conditions;

import java.util.Collections;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class FalseCondition extends Condition {
   public static final FalseCondition INSTANCE = new FalseCondition();

   private FalseCondition() {
      super("false", Collections.emptyList(), true, "true", "false");
   }

   @Override
   public boolean isMet(@NotNull TabPlayer player) {
      return false;
   }

   @NotNull
   @Override
   public Condition invert() {
      return TrueCondition.INSTANCE;
   }

   @NotNull
   @Override
   public String toShortFormat() {
      return "false";
   }
}
