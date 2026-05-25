package me.neznamy.tab.shared.placeholders.conditions;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionManager {
   @NotNull
   private final Map<String, Condition> registeredConditions = new HashMap<>();

   public ConditionManager() {
      this.registerCondition(TrueCondition.INSTANCE);
      this.registerCondition(FalseCondition.INSTANCE);
   }

   public void registerCondition(@NonNull Condition condition) {
      if (condition == null) {
         throw new NullPointerException("condition is marked non-null but is null");
      }

      this.registeredConditions.put(condition.getName(), condition);
   }

   @Contract("null -> null")
   public Condition getByNameOrExpression(@Nullable String string) {
      if (string != null && !string.isEmpty()) {
         String anonVersion = "AnonymousCondition[" + string + "]";
         if (this.registeredConditions.containsKey(string)) {
            return this.registeredConditions.get(string);
         }

         if (this.registeredConditions.containsKey(anonVersion)) {
            return this.registeredConditions.get(anonVersion);
         }

         Condition c = new Condition(string);
         c.finishSetup();
         TAB.getInstance()
            .getPlaceholderManager()
            .registerInternalPlayerPlaceholder(TabConstants.Placeholder.condition(c.getName()), c.getRefresh(), p -> c.getText((TabPlayer)p));
         this.registerCondition(c);
         return c;
      } else {
         return null;
      }
   }

   public void finishSetups() {
      for (Condition c : this.registeredConditions.values()) {
         c.finishSetup();
      }
   }
}
