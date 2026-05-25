package me.neznamy.tab.platforms.bungeecord;

import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import net.md_5.bungee.protocol.packet.BossBar;
import org.jetbrains.annotations.NotNull;

public class BungeeBossBar extends SafeBossBar<UUID> {
   @NotNull
   private final BungeeTabPlayer player;

   @NotNull
   public UUID constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      return UUID.randomUUID();
   }

   @Override
   public void create(@NotNull SafeBossBar.BossBarInfo bar) {
      BossBar packet = new BossBar((UUID)bar.getBossBar(), 0);
      packet.setHealth(bar.getProgress());
      packet.setTitle(this.player.getPlatform().transformComponent(bar.getTitle(), this.player.getVersion()));
      packet.setColor(bar.getColor().ordinal());
      packet.setDivision(bar.getStyle().ordinal());
      this.player.sendPacket(packet);
   }

   @Override
   public void updateTitle(@NotNull SafeBossBar.BossBarInfo bar) {
      BossBar packet = new BossBar((UUID)bar.getBossBar(), 3);
      packet.setTitle(this.player.getPlatform().transformComponent(bar.getTitle(), this.player.getVersion()));
      this.player.sendPacket(packet);
   }

   @Override
   public void updateProgress(@NotNull SafeBossBar.BossBarInfo bar) {
      BossBar packet = new BossBar((UUID)bar.getBossBar(), 2);
      packet.setHealth(bar.getProgress());
      this.player.sendPacket(packet);
   }

   @Override
   public void updateStyle(@NotNull SafeBossBar.BossBarInfo bar) {
      this.updateColor(bar);
   }

   @Override
   public void updateColor(@NotNull SafeBossBar.BossBarInfo bar) {
      BossBar packet = new BossBar((UUID)bar.getBossBar(), 4);
      packet.setDivision(bar.getStyle().ordinal());
      packet.setColor(bar.getColor().ordinal());
      this.player.sendPacket(packet);
   }

   @Override
   public void remove(@NotNull SafeBossBar.BossBarInfo bar) {
      this.player.sendPacket(new BossBar((UUID)bar.getBossBar(), 1));
   }

   @Generated
   public BungeeBossBar(@NotNull BungeeTabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
   }
}
