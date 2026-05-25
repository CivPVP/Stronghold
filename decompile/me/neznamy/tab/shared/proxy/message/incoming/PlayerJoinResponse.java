package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinResponse implements IncomingMessage {
   private World world;
   private String group;
   private Map<String, Object> placeholders;
   private int gameMode;

   @Override
   public void read(@NotNull ByteArrayDataInput in) {
      this.world = World.byName(in.readUTF());
      if (TAB.getInstance().getGroupManager().getPermissionPlugin().contains("Vault")
         && !TAB.getInstance().getConfiguration().getConfig().isGroupsByPermissions()) {
         this.group = in.readUTF();
      }

      this.placeholders = new HashMap<>();
      int placeholderCount = in.readInt();

      for (int i = 0; i < placeholderCount; i++) {
         String identifier = in.readUTF();
         if (!identifier.startsWith("%rel_")) {
            this.placeholders.put(identifier, in.readUTF());
         } else {
            Map<String, String> map = new HashMap<>();
            int playerCount = in.readInt();

            for (int j = 0; j < playerCount; j++) {
               String otherPlayer = in.readUTF();
               String value = in.readUTF();
               map.put(otherPlayer, value);
            }

            this.placeholders.put(identifier, map);
         }
      }

      this.gameMode = in.readInt();
   }

   @Override
   public void process(@NotNull ProxyTabPlayer player) {
      TAB.getInstance()
         .debug("Bridge took " + (System.currentTimeMillis() - player.getBridgeRequestTime()) + "ms to respond to join message of " + player.getName());
      TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), this.world);
      if (this.group != null) {
         player.setGroup(this.group);
      }

      if (player.isVanished()) {
         player.setVanished(false);
         TAB.getInstance().getFeatureManager().onVanishStatusChange(player);
      }

      player.setDisguised(false);
      player.setInvisibilityPotion(false);
      Map<PlayerPlaceholderImpl, String> playerPlaceholderUpdates = new HashMap<>();

      for (Entry<String, Object> entry : this.placeholders.entrySet()) {
         String identifier = entry.getKey();
         if (TAB.getInstance().getPlaceholderManager().getBridgePlaceholders().containsKey(identifier)) {
            Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholderRaw(identifier);
            if (pl != null) {
               if (identifier.startsWith("%rel_")) {
                  RelationalPlaceholder rel = (RelationalPlaceholder)pl;
                  Map<String, String> map = (Map<String, String>)entry.getValue();

                  for (Entry<String, String> entry2 : map.entrySet()) {
                     TabPlayer other = TAB.getInstance().getPlayer(entry2.getKey());
                     if (other != null) {
                        rel.updateValue(player, other, entry2.getValue());
                     }
                  }
               } else if (pl instanceof PlayerPlaceholderImpl) {
                  playerPlaceholderUpdates.put((PlayerPlaceholderImpl)pl, (String)entry.getValue());
               } else {
                  ((ServerPlaceholder)pl).updateValue((String)entry.getValue());
               }
            }
         }
      }

      PlayerPlaceholderImpl.bulkUpdateValues(player, playerPlaceholderUpdates);
      player.setGamemode(this.gameMode);
      player.setBridgeConnected(true);
   }
}
