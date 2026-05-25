package me.neznamy.tab.platforms.bukkit.bossbar;

import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.NotNull;

public class BukkitBossBar extends SafeBossBar<BossBar> {
   private static final boolean available = ReflectionUtils.classExists("org.bukkit.boss.BossBar");
   private static final BarStyle[] styles = available ? BarStyle.values() : null;
   @NotNull
   private final BukkitTabPlayer player;

   @NotNull
   public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      BossBar bar = Bukkit.createBossBar(
         this.player.getPlatform().toBukkitFormat(title), org.bukkit.boss.BarColor.valueOf(color.name()), styles[style.ordinal()], new BarFlag[0]
      );
      bar.setProgress(progress);
      return bar;
   }

   @Override
   public void create(SafeBossBar.@NotNull BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).addPlayer(this.player.getPlayer());
   }

   @Override
   public void updateTitle(SafeBossBar.@NotNull BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setTitle(this.player.getPlatform().toBukkitFormat(bar.getTitle()));
   }

   @Override
   public void updateProgress(SafeBossBar.@NotNull BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setProgress(bar.getProgress());
   }

   @Override
   public void updateStyle(SafeBossBar.@NotNull BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setStyle(styles[bar.getStyle().ordinal()]);
   }

   @Override
   public void updateColor(SafeBossBar.@NotNull BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setColor(org.bukkit.boss.BarColor.valueOf(bar.getColor().name()));
   }

   @Override
   public void remove(SafeBossBar.@NotNull BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).removePlayer(this.player.getPlayer());
   }

   @Generated
   public BukkitBossBar(@NotNull BukkitTabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
   }

   @Generated
   public static boolean isAvailable() {
      return available;
   }
}
