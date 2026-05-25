package me.neznamy.tab.shared.platform.impl;

import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jetbrains.annotations.NotNull;

public class AdventureBossBar extends SafeBossBar<BossBar> {
   private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.bossbar.BossBar");
   private final TabPlayer player;

   @NotNull
   public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      return BossBar.bossBar(title.toAdventure(), progress, Color.valueOf(color.name()), Overlay.valueOf(style.name()));
   }

   @Override
   public void create(@NotNull SafeBossBar.BossBarInfo bar) {
      ((Audience)this.player.getPlayer()).showBossBar((BossBar)bar.getBossBar());
   }

   @Override
   public void updateTitle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).name(bar.getTitle().toAdventure());
   }

   @Override
   public void updateProgress(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).progress(bar.getProgress());
   }

   @Override
   public void updateStyle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).overlay(Overlay.valueOf(bar.getStyle().name()));
   }

   @Override
   public void updateColor(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).color(Color.valueOf(bar.getColor().name()));
   }

   @Override
   public void remove(@NotNull SafeBossBar.BossBarInfo bar) {
      ((Audience)this.player.getPlayer()).hideBossBar((BossBar)bar.getBossBar());
   }

   @Generated
   public AdventureBossBar(TabPlayer player) {
      this.player = player;
   }

   @Generated
   public static boolean isAvailable() {
      return available;
   }
}
