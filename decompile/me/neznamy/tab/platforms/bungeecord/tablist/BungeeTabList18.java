package me.neznamy.tab.platforms.bungeecord.tablist;

import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import org.jetbrains.annotations.Nullable;

public class BungeeTabList18 extends BungeeTabList {
   public BungeeTabList18(@NonNull BungeeTabPlayer player) {
      super(player);
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }
   }

   @Override
   public void removeEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.removeUuid(entry);
      this.sendPacket(Action.REMOVE_PLAYER, this.item(entry));
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry);
      if (displayName != null) {
         item.setDisplayName(this.toComponent(displayName));
      }

      this.sendPacket(Action.UPDATE_DISPLAY_NAME, item);
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry);
      item.setPing(latency);
      this.sendPacket(Action.UPDATE_LATENCY, item);
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry);
      item.setGamemode(gameMode);
      this.sendPacket(Action.UPDATE_GAMEMODE, item);
   }

   @Override
   public void updateListed(@NonNull UUID entry, boolean listed) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }
   }

   @Override
   public void updateListOrder(@NonNull UUID entry, int listOrder) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }
   }

   @Override
   public void updateHat(@NonNull UUID entry, boolean showHat) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }
   }

   @Override
   public void addEntry0(@NonNull TabList.Entry entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.addUuid(entry.getUniqueId());
      this.sendPacket(Action.ADD_PLAYER, this.entryToItem(entry));
   }

   private void sendPacket(@NonNull Action action, @NonNull Item item) {
      if (action == null) {
         throw new NullPointerException("action is marked non-null but is null");
      }

      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      }

      PlayerListItem packet = new PlayerListItem();
      packet.setAction(action);
      packet.setItems(new Item[]{item});
      this.player.sendPacket(packet);
   }
}
