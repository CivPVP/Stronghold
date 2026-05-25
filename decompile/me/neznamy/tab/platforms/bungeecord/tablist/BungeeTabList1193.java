package me.neznamy.tab.platforms.bungeecord.tablist;

import java.util.EnumSet;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate.Action;
import org.jetbrains.annotations.Nullable;

public class BungeeTabList1193 extends BungeeTabList {
   private static final EnumSet<Action> updateDisplayName = EnumSet.of(Action.UPDATE_DISPLAY_NAME);
   private static final EnumSet<Action> updateLatency = EnumSet.of(Action.UPDATE_LATENCY);
   private static final EnumSet<Action> updateGameMode = EnumSet.of(Action.UPDATE_GAMEMODE);
   private static final EnumSet<Action> updateListed = EnumSet.of(Action.UPDATE_LISTED);
   private static final EnumSet<Action> updateListOrder = EnumSet.of(Action.UPDATE_LIST_ORDER);
   private static final EnumSet<Action> updateHat = EnumSet.of(Action.UPDATE_HAT);
   private static final EnumSet<Action> addPlayer_legacy = EnumSet.of(
      Action.ADD_PLAYER, Action.UPDATE_GAMEMODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME
   );
   private static final EnumSet<Action> addPlayer_1_21_2 = EnumSet.of(
      Action.ADD_PLAYER, Action.UPDATE_GAMEMODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_LIST_ORDER
   );
   private static final EnumSet<Action> addPlayer_1_21_4 = EnumSet.of(
      Action.ADD_PLAYER,
      Action.UPDATE_GAMEMODE,
      Action.UPDATE_LISTED,
      Action.UPDATE_LATENCY,
      Action.UPDATE_DISPLAY_NAME,
      Action.UPDATE_LIST_ORDER,
      Action.UPDATE_HAT
   );

   public BungeeTabList1193(@NonNull BungeeTabPlayer player) {
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
      PlayerListItemRemove remove = new PlayerListItemRemove();
      remove.setUuids(new UUID[]{entry});
      this.player.sendPacket(remove);
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

      this.sendPacket(updateDisplayName, item);
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry);
      item.setPing(latency);
      this.sendPacket(updateLatency, item);
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry);
      item.setGamemode(gameMode);
      this.sendPacket(updateGameMode, item);
   }

   @Override
   public void updateListed(@NonNull UUID entry, boolean listed) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry);
      item.setListed(listed);
      this.sendPacket(updateListed, item);
   }

   @Override
   public void updateListOrder(@NonNull UUID entry, int listOrder) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      if (this.player.getVersionId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
         Item item = this.item(entry);
         item.setListOrder(listOrder);
         this.sendPacket(updateListOrder, item);
      }
   }

   @Override
   public void updateHat(@NonNull UUID entry, boolean showHat) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      if (this.player.getVersionId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
         Item item = this.item(entry);
         item.setShowHat(showHat);
         this.sendPacket(updateHat, item);
      }
   }

   @Override
   public void addEntry0(@NonNull TabList.Entry entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.addUuid(entry.getUniqueId());
      EnumSet<Action> actions;
      if (this.player.getVersionId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
         actions = addPlayer_1_21_4;
      } else if (this.player.getVersionId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
         actions = addPlayer_1_21_2;
      } else {
         actions = addPlayer_legacy;
      }

      this.sendPacket(actions, this.entryToItem(entry));
   }

   private void sendPacket(@NonNull EnumSet<Action> actions, @NonNull Item item) {
      if (actions == null) {
         throw new NullPointerException("actions is marked non-null but is null");
      }

      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      }

      PlayerListItemUpdate packet = new PlayerListItemUpdate();
      packet.setActions(actions);
      packet.setItems(new Item[]{item});
      this.player.sendPacket(packet);
   }
}
