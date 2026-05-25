package me.neznamy.tab.shared.features.types;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisableChecker extends RefreshableFeature {
   @NotNull
   private final TabFeature feature;
   @Nullable
   private final Condition disableCondition;
   @NotNull
   private final BiConsumer<TabPlayer, Boolean> action;
   @NotNull
   private final Function<TabPlayer, AtomicBoolean> field;

   public DisableChecker(
      @NotNull TabFeature feature,
      @Nullable Condition disableCondition,
      @NotNull BiConsumer<TabPlayer, Boolean> action,
      @NotNull Function<TabPlayer, AtomicBoolean> field
   ) {
      this.feature = feature;
      this.disableCondition = disableCondition;
      this.action = action;
      this.field = field;
      if (disableCondition != null) {
         this.addUsedPlaceholder(TabConstants.Placeholder.condition(disableCondition.getName()));
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Refreshing disable condition";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (this.disableCondition != null) {
         Runnable r = () -> {
            boolean disabledNow = this.disableCondition.isMet(refreshed);
            AtomicBoolean value = this.field.apply(refreshed);
            if (disabledNow != value.get()) {
               value.set(disabledNow);
               this.action.accept(refreshed, disabledNow);
            }
         };
         if (this.feature instanceof CustomThreaded) {
            ((CustomThreaded)this.feature)
               .getCustomThread()
               .execute(new TimedCaughtTask(TAB.getInstance().getCpu(), r, this.feature.getFeatureName(), "Refreshing disable condition"));
         } else {
            r.run();
         }
      }
   }

   public boolean isDisableConditionMet(TabPlayer p) {
      return this.disableCondition != null && this.disableCondition.isMet(p);
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.feature.getFeatureName();
   }
}
