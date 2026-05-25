package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class LayoutLatencyRefresher extends RefreshableFeature {
   public LayoutLatencyRefresher() {
      this.addUsedPlaceholder("%ping%");
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Layout";
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating latency";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
         if (all.layoutData.currentLayout != null) {
            PlayerSlot slot = all.layoutData.currentLayout.view.getSlot(p);
            if (slot != null) {
               all.getTabList().updateLatency(slot.getUniqueId(), p.getPing());
            }
         }
      }
   }
}
