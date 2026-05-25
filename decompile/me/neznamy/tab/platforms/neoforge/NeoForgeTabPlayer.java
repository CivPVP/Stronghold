package me.neznamy.tab.platforms.neoforge;

import me.neznamy.tab.platforms.neoforge.hook.LuckPermsAPIHook;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.NotNull;

public class NeoForgeTabPlayer extends BackendTabPlayer {
   public NeoForgeTabPlayer(@NotNull NeoForgePlatform platform, @NotNull ServerPlayer player) {
      super(platform, player, player.getUUID(), player.getGameProfile().name(), NeoForgeTAB.getLevelName(player.level()), SharedConstants.getProtocolVersion());
   }

   @Override
   public boolean hasPermission(@NotNull String permission) {
      return LuckPermsAPIHook.hasPermission(this.getPlayer().createCommandSourceStack(), permission);
   }

   @Override
   public int getPing() {
      return this.getPlayer().connection.latency();
   }

   @Override
   public void sendMessage(@NotNull TabComponent message) {
      this.getPlayer().sendSystemMessage(message.convert());
   }

   @Override
   public boolean hasInvisibilityPotion() {
      return false;
   }

   @Override
   public boolean isDisguised() {
      return false;
   }

   @NotNull
   public ServerPlayer getPlayer() {
      return (ServerPlayer)this.player;
   }

   public NeoForgePlatform getPlatform() {
      return (NeoForgePlatform)this.platform;
   }

   @Override
   public boolean isVanished0() {
      return false;
   }

   @Override
   public int getDeaths() {
      return this.getPlayer().getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
   }

   @Override
   public int getGamemode() {
      return this.getPlayer().gameMode.getGameModeForPlayer().getId();
   }

   @Override
   public double getHealth() {
      return this.getPlayer().getHealth();
   }

   @NotNull
   @Override
   public String getDisplayName() {
      return this.getPlayer().getGameProfile().name();
   }
}
