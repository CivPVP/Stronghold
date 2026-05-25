package me.neznamy.tab.platforms.forge;

import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.jetbrains.annotations.NotNull;

public class ForgeBossBar extends SafeBossBar<ServerBossEvent> {
   @NotNull
   private final ForgeTabPlayer player;

   @NotNull
   public ServerBossEvent constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      ServerBossEvent bar = new ServerBossEvent(title.convert(), BossBarColor.valueOf(color.name()), BossBarOverlay.valueOf(style.name()));
      bar.setProgress(progress);
      return bar;
   }

   @Override
   public void create(@NotNull SafeBossBar.BossBarInfo bar) {
      ((ServerBossEvent)bar.getBossBar()).addPlayer(this.player.getPlayer());
   }

   @Override
   public void updateTitle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((ServerBossEvent)bar.getBossBar()).setName(bar.getTitle().convert());
   }

   @Override
   public void updateProgress(@NotNull SafeBossBar.BossBarInfo bar) {
      ((ServerBossEvent)bar.getBossBar()).setProgress(bar.getProgress());
   }

   @Override
   public void updateStyle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((ServerBossEvent)bar.getBossBar()).setOverlay(BossBarOverlay.valueOf(bar.getStyle().name()));
   }

   @Override
   public void updateColor(@NotNull SafeBossBar.BossBarInfo bar) {
      ((ServerBossEvent)bar.getBossBar()).setColor(BossBarColor.valueOf(bar.getColor().name()));
   }

   @Override
   public void remove(@NotNull SafeBossBar.BossBarInfo bar) {
      ((ServerBossEvent)bar.getBossBar()).removePlayer(this.player.getPlayer());
   }

   @Generated
   public ForgeBossBar(@NotNull ForgeTabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
   }
}
