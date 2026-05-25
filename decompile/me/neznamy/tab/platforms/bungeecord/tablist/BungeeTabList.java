package me.neznamy.tab.platforms.bungeecord.tablist;

import java.util.Collection;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.data.Property;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.tab.ServerUnique;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BungeeTabList extends TrackedTabList<BungeeTabPlayer> {
   private final Collection<UUID> uuids;

   protected BungeeTabList(@NonNull BungeeTabPlayer player) {
      super(player);

      try {
         if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
         }

         this.uuids = (Collection<UUID>)ReflectionUtils.getField(ServerUnique.class, "uuids").get(((UserConnection)player.getPlayer()).getTabListHandler());
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @Override
   public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
      if (header == null) {
         throw new NullPointerException("header is marked non-null but is null");
      }

      if (footer == null) {
         throw new NullPointerException("footer is marked non-null but is null");
      }

      this.player.getPlayer().setTabHeader(this.toComponent(header), this.toComponent(footer));
   }

   @NotNull
   public Item item(@NonNull UUID id) {
      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      Item item = new Item();
      item.setUuid(id);
      return item;
   }

   @NotNull
   public Item entryToItem(@NonNull TabList.Entry entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      Item item = this.item(entry.getUniqueId());
      item.setUsername(entry.getName());
      item.setDisplayName(entry.getDisplayName() == null ? null : this.toComponent(entry.getDisplayName()));
      item.setGamemode(entry.getGameMode());
      item.setListed(entry.isListed());
      item.setPing(entry.getLatency());
      if (entry.getSkin() != null) {
         item.setProperties(new Property[]{new Property("textures", entry.getSkin().getValue(), entry.getSkin().getSignature())});
      } else {
         item.setProperties(new Property[0]);
      }

      item.setListOrder(entry.getListOrder());
      item.setShowHat(entry.isShowHat());
      return item;
   }

   public void addUuid(@NonNull UUID id) {
      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      this.uuids.add(id);
   }

   public void removeUuid(@NonNull UUID id) {
      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      this.uuids.remove(id);
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      if (packet == null) {
         throw new NullPointerException("packet is marked non-null but is null");
      }

      if (packet instanceof PlayerListHeaderFooter) {
         PlayerListHeaderFooter tablist = (PlayerListHeaderFooter)packet;
         if (this.header == null || this.footer == null) {
            return packet;
         }

         BaseComponent headerComponent = this.player.getPlatform().transformComponent(this.header, this.player.getVersion());
         BaseComponent footerComponent = this.player.getPlatform().transformComponent(this.footer, this.player.getVersion());
         if (tablist.getHeader() != headerComponent || tablist.getFooter() != footerComponent) {
            tablist.setHeader(headerComponent);
            tablist.setFooter(footerComponent);
         }
      }

      if (packet instanceof PlayerListItem) {
         PlayerListItem listItem = (PlayerListItem)packet;

         for (Item item : listItem.getItems()) {
            if (listItem.getAction() == Action.UPDATE_DISPLAY_NAME || listItem.getAction() == Action.ADD_PLAYER) {
               TabComponent forcedDisplayName = this.getForcedDisplayNames().get(item.getUuid());
               if (forcedDisplayName != null) {
                  item.setDisplayName(this.toComponent(forcedDisplayName));
               }
            }

            if ((listItem.getAction() == Action.UPDATE_GAMEMODE || listItem.getAction() == Action.ADD_PLAYER)
               && this.getBlockedSpectators().contains(item.getUuid())
               && item.getGamemode() == 3) {
               item.setGamemode(0);
            }

            if ((listItem.getAction() == Action.UPDATE_LATENCY || listItem.getAction() == Action.ADD_PLAYER) && getForcedLatency() != null) {
               item.setPing(getForcedLatency());
            }

            if (listItem.getAction() == Action.ADD_PLAYER) {
               TAB.getInstance().getFeatureManager().onEntryAdd(this.player, item.getUuid(), item.getUsername());
            }
         }
      } else if (packet instanceof PlayerListItemUpdate) {
         PlayerListItemUpdate update = (PlayerListItemUpdate)packet;

         for (Item item : update.getItems()) {
            if (update.getActions().contains(net.md_5.bungee.protocol.packet.PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME)) {
               TabComponent forcedDisplayName = this.getForcedDisplayNames().get(item.getUuid());
               if (forcedDisplayName != null) {
                  item.setDisplayName(this.toComponent(forcedDisplayName));
               }
            }

            if (update.getActions().contains(net.md_5.bungee.protocol.packet.PlayerListItemUpdate.Action.UPDATE_GAMEMODE)
               && this.getBlockedSpectators().contains(item.getUuid())
               && item.getGamemode() == 3) {
               item.setGamemode(0);
            }

            if (update.getActions().contains(net.md_5.bungee.protocol.packet.PlayerListItemUpdate.Action.UPDATE_LATENCY) && getForcedLatency() != null) {
               item.setPing(getForcedLatency());
            }

            if (update.getActions().contains(net.md_5.bungee.protocol.packet.PlayerListItemUpdate.Action.ADD_PLAYER)) {
               TAB.getInstance().getFeatureManager().onEntryAdd(this.player, item.getUuid(), item.getUsername());
            }
         }
      }

      return packet;
   }

   @Override
   public boolean containsEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      } else {
         return this.uuids.contains(entry);
      }
   }

   @Nullable
   @Override
   public TabList.Skin getSkin() {
      LoginResult loginResult = ((InitialHandler)this.player.getPlayer().getPendingConnection()).getLoginProfile();
      if (loginResult == null) {
         return null;
      }

      Property[] properties = loginResult.getProperties();
      if (properties == null) {
         return null;
      }

      for (Property property : properties) {
         if (property.getName().equals("textures")) {
            return new TabList.Skin(property.getValue(), property.getSignature());
         }
      }

      return null;
   }

   @NotNull
   protected BaseComponent toComponent(@NonNull TabComponent component) {
      if (component == null) {
         throw new NullPointerException("component is marked non-null but is null");
      } else {
         return this.player.getPlatform().transformComponent(component, this.player.getVersion());
      }
   }
}
