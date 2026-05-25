package me.neznamy.tab.shared.platform.impl;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import org.jetbrains.annotations.NotNull;

public class DummyBossBar extends SafeBossBar<Object> {
   @NotNull
   @Override
   public Object constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      return new Object();
   }

   @Override
   public void create(@NotNull SafeBossBar.BossBarInfo bar) {
   }

   @Override
   public void updateTitle(@NotNull SafeBossBar.BossBarInfo bar) {
   }

   @Override
   public void updateProgress(@NotNull SafeBossBar.BossBarInfo bar) {
   }

   @Override
   public void updateStyle(@NotNull SafeBossBar.BossBarInfo bar) {
   }

   @Override
   public void updateColor(@NotNull SafeBossBar.BossBarInfo bar) {
   }

   @Override
   public void remove(@NotNull SafeBossBar.BossBarInfo bar) {
   }
}
