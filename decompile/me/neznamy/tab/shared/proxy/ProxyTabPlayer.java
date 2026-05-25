package me.neznamy.tab.shared.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.api.integration.VanishIntegration;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.OutgoingMessage;
import me.neznamy.tab.shared.proxy.message.outgoing.PermissionRequest;
import me.neznamy.tab.shared.proxy.message.outgoing.PlayerJoin;
import me.neznamy.tab.shared.task.PluginMessageEncodeTask;
import org.jetbrains.annotations.NotNull;

public abstract class ProxyTabPlayer extends TabPlayer {
   public boolean vanished;
   private boolean disguised;
   private boolean invisibilityPotion;
   private long bridgeRequestTime;
   private boolean bridgeConnected;
   private int gamemode;
   private final Map<String, Boolean> permissions = new HashMap<>();

   protected ProxyTabPlayer(
      @NotNull ProxyPlatform platform, @NotNull Object player, @NotNull UUID uniqueId, @NotNull String name, @NotNull String server, int protocolVersion
   ) {
      super(platform, player, uniqueId, name, server, "N/A", protocolVersion, TAB.getInstance().getConfiguration().getConfig().isOnlineUuidInTabList());
      this.sendJoinPluginMessage();
   }

   public void sendJoinPluginMessage() {
      this.bridgeConnected = false;
      this.sendPluginMessage(
         new PlayerJoin(
            TAB.getInstance().getGroupManager().getPermissionPlugin().contains("Vault")
               && !TAB.getInstance().getConfiguration().getConfig().isGroupsByPermissions(),
            TAB.getInstance().getPlaceholderManager().getBridgePlaceholders(),
            TAB.getInstance().getConfiguration().getConfig().getReplacements().getValues()
         )
      );
      TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
      if (expansion instanceof ProxyTabExpansion) {
         ((ProxyTabExpansion)expansion).resendAllValues(this);
      }

      this.bridgeRequestTime = System.currentTimeMillis();
   }

   public void setHasPermission(@NotNull String permission, boolean value) {
      this.permissions.put(permission, value);
   }

   public void setGamemode(int gamemode) {
      if (this.gamemode != gamemode) {
         this.gamemode = gamemode;
         ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%gamemode%")).update(this);
         TAB.getInstance().getFeatureManager().onGameModeChange(this);
      }
   }

   public void setInvisibilityPotion(boolean invisibilityPotion) {
      if (this.invisibilityPotion != invisibilityPotion) {
         this.invisibilityPotion = invisibilityPotion;
         ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%invisible%")).update(this);
      }
   }

   public abstract boolean hasPermission0(String var1);

   public abstract void sendPluginMessage(byte[] var1);

   @Override
   public boolean hasInvisibilityPotion() {
      return this.invisibilityPotion;
   }

   @Override
   public boolean hasPermission(@NotNull String permission) {
      if (!TAB.getInstance().getConfiguration().getConfig().isBukkitPermissions()) {
         return this.hasPermission0(permission);
      }

      this.sendPluginMessage(new PermissionRequest(permission));
      return this.permissions != null && this.permissions.getOrDefault(permission, false);
   }

   public void sendPluginMessage(@NotNull OutgoingMessage message) {
      CpuManager.getPluginMessageEncodeThread().execute(new PluginMessageEncodeTask(this, message));
   }

   @Override
   public boolean isVanished() {
      if (!VanishIntegration.getHandlers().isEmpty()) {
         for (VanishIntegration integration : VanishIntegration.getHandlers()) {
            if (integration.isVanished(this)) {
               return true;
            }
         }

         return false;
      } else {
         return this.vanished;
      }
   }

   @Generated
   @Override
   public boolean isDisguised() {
      return this.disguised;
   }

   @Generated
   public boolean isInvisibilityPotion() {
      return this.invisibilityPotion;
   }

   @Generated
   public long getBridgeRequestTime() {
      return this.bridgeRequestTime;
   }

   @Generated
   public boolean isBridgeConnected() {
      return this.bridgeConnected;
   }

   @Generated
   @Override
   public int getGamemode() {
      return this.gamemode;
   }

   @Generated
   public Map<String, Boolean> getPermissions() {
      return this.permissions;
   }

   @Generated
   public void setVanished(boolean vanished) {
      this.vanished = vanished;
   }

   @Generated
   public void setDisguised(boolean disguised) {
      this.disguised = disguised;
   }

   @Generated
   public void setBridgeRequestTime(long bridgeRequestTime) {
      this.bridgeRequestTime = bridgeRequestTime;
   }

   @Generated
   public void setBridgeConnected(boolean bridgeConnected) {
      this.bridgeConnected = bridgeConnected;
   }
}
