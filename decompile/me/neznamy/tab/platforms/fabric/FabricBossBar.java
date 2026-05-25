package me.neznamy.tab.platforms.fabric;

import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import net.minecraft.class_3213;
import net.minecraft.class_1259.class_1260;
import net.minecraft.class_1259.class_1261;
import org.jetbrains.annotations.NotNull;

public class FabricBossBar extends SafeBossBar<class_3213> {
   @NotNull
   private final FabricTabPlayer player;

   @NotNull
   public class_3213 constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      class_3213 bar = new class_3213(title.convert(), class_1260.valueOf(color.name()), class_1261.valueOf(style.name()));
      bar.method_5408(progress);
      return bar;
   }

   @Override
   public void create(@NotNull SafeBossBar.BossBarInfo bar) {
      ((class_3213)bar.getBossBar()).method_14088(this.player.getPlayer());
   }

   @Override
   public void updateTitle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((class_3213)bar.getBossBar()).method_5413(bar.getTitle().convert());
   }

   @Override
   public void updateProgress(@NotNull SafeBossBar.BossBarInfo bar) {
      ((class_3213)bar.getBossBar()).method_5408(bar.getProgress());
   }

   @Override
   public void updateStyle(@NotNull SafeBossBar.BossBarInfo bar) {
      ((class_3213)bar.getBossBar()).method_5409(class_1261.valueOf(bar.getStyle().name()));
   }

   @Override
   public void updateColor(@NotNull SafeBossBar.BossBarInfo bar) {
      ((class_3213)bar.getBossBar()).method_5416(class_1260.valueOf(bar.getColor().name()));
   }

   @Override
   public void remove(@NotNull SafeBossBar.BossBarInfo bar) {
      ((class_3213)bar.getBossBar()).method_14089(this.player.getPlayer());
   }

   @Generated
   public FabricBossBar(@NotNull FabricTabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
   }
}
