package me.neznamy.tab.shared.features.injection;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class PipelineInjector extends TabFeature implements JoinListener, Loadable, UnLoadable {
   @NotNull
   @Override
   public String getFeatureName() {
      return "Pipeline injection";
   }

   public abstract void inject(@NotNull TabPlayer var1);

   public abstract void uninject(@NotNull TabPlayer var1);

   @Override
   public void load() {
      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.inject(p);
      }
   }

   @Override
   public void unload() {
      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.uninject(p);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      this.inject(connectedPlayer);
   }
}
