package me.neznamy.tab.platforms.bungeecord.tablist;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BungeeTabList17 extends BungeeTabList {
   @NotNull
   private final Map<UUID, String> userNames = new HashMap<>();
   @NotNull
   private final Map<UUID, TabComponent> displayNames = new HashMap<>();

   public BungeeTabList17(@NonNull BungeeTabPlayer player) {
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

      if (this.displayNames.containsKey(entry)) {
         this.removeUuid(entry);
         this.update(Action.REMOVE_PLAYER, this.createItem(null, this.displayNames.get(entry), 0));
         this.userNames.remove(entry);
         this.displayNames.remove(entry);
      }
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      if (this.displayNames.containsKey(entry)) {
         this.update(Action.REMOVE_PLAYER, this.createItem(null, this.displayNames.get(entry), 0));
         this.addEntry0(new TabList.Entry(entry, this.userNames.get(entry), null, false, 0, 0, displayName, 0, false));
      }
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      if (this.displayNames.containsKey(entry)) {
         this.update(Action.UPDATE_LATENCY, this.createItem(null, this.displayNames.get(entry), latency));
      }
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }
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
      this.update(
         Action.ADD_PLAYER,
         this.createItem(entry.getName(), entry.getDisplayName() == null ? new TabTextComponent(entry.getName()) : entry.getDisplayName(), entry.getLatency())
      );
      this.userNames.put(entry.getUniqueId(), entry.getName());
      this.displayNames.put(entry.getUniqueId(), entry.getDisplayName());
   }

   @Override
   public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
      if (header == null) {
         throw new NullPointerException("header is marked non-null but is null");
      }

      if (footer == null) {
         throw new NullPointerException("footer is marked non-null but is null");
      }
   }

   @NotNull
   @Override
   public BaseComponent toComponent(@NonNull TabComponent component) {
      if (component == null) {
         throw new NullPointerException("component is marked non-null but is null");
      }

      String displayNameString = component.toLegacyText();
      if (displayNameString.length() > 16) {
         displayNameString = displayNameString.substring(0, 16);
      }

      return this.player.getPlatform().transformComponent(new TabTextComponent(displayNameString), this.player.getVersion());
   }

   private void update(@NonNull Action action, @NonNull Item item) {
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

   @NotNull
   private Item createItem(@Nullable String username, @Nullable TabComponent displayName, int latency) {
      Item item = new Item();
      item.setUsername(username);
      item.setPing(latency);
      if (displayName != null) {
         item.setDisplayName(this.toComponent(displayName));
      }

      return item;
   }
}
