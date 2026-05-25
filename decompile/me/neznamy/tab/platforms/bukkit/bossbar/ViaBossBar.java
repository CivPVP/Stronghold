package me.neznamy.tab.platforms.bukkit.bossbar;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import org.jetbrains.annotations.NotNull;

public class ViaBossBar extends SafeBossBar<BossBar> {
   private static final BossStyle[] styles = BossStyle.values();
   @NotNull
   private final BukkitTabPlayer player;

   @NotNull
   public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      return Via.getAPI().legacyAPI().createLegacyBossBar(title.toLegacyText(), progress, BossColor.valueOf(color.name()), styles[style.ordinal()]);
   }

   @Override
   public void create(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).addPlayer(this.player.getPlayer().getUniqueId());
   }

   @Override
   public void updateTitle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setTitle(bar.getTitle().toLegacyText());
   }

   @Override
   public void updateProgress(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setHealth(bar.getProgress());
   }

   @Override
   public void updateStyle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setStyle(styles[bar.getStyle().ordinal()]);
   }

   @Override
   public void updateColor(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).setColor(BossColor.valueOf(bar.getColor().name()));
   }

   @Override
   public void remove(@NotNull SafeBossBar.BossBarInfo bar) {
      ((BossBar)bar.getBossBar()).removePlayer(this.player.getPlayer().getUniqueId());
   }

   @Generated
   public ViaBossBar(@NotNull BukkitTabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
   }
}
