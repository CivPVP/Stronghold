package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.platforms.fabric.hook.PermissionsAPIHook;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import net.minecraft.class_155;
import net.minecraft.class_3222;
import net.minecraft.class_3468;
import org.jetbrains.annotations.NotNull;

public class FabricTabPlayer extends BackendTabPlayer {
   public FabricTabPlayer(@NotNull FabricPlatform platform, @NotNull class_3222 player) {
      super(platform, player, player.method_5667(), player.method_7334().name(), FabricTAB.getLevelName(player.method_51469()), class_155.method_31372());
   }

   @Override
   public boolean hasPermission(@NotNull String permission) {
      return PermissionsAPIHook.hasPermission(this.getPlayer().method_64396(), permission);
   }

   @Override
   public int getPing() {
      return this.getPlayer().field_13987.method_52405();
   }

   @Override
   public void sendMessage(@NotNull TabComponent message) {
      this.getPlayer().method_64398(message.convert());
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
   public class_3222 getPlayer() {
      return (class_3222)this.player;
   }

   public FabricPlatform getPlatform() {
      return (FabricPlatform)this.platform;
   }

   @Override
   public boolean isVanished0() {
      return false;
   }

   @Override
   public int getDeaths() {
      return this.getPlayer().method_14248().method_15025(class_3468.field_15419.method_14956(class_3468.field_15421));
   }

   @Override
   public int getGamemode() {
      return this.getPlayer().field_13974.method_14257().method_8379();
   }

   @Override
   public double getHealth() {
      return this.getPlayer().method_6032();
   }

   @NotNull
   @Override
   public String getDisplayName() {
      return this.getPlayer().method_7334().name();
   }
}
