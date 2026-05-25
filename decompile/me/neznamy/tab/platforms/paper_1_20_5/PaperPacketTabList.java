package me.neznamy.tab.platforms.paper_1_20_5;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.world.level.GameType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperPacketTabList extends TrackedTabList<BukkitTabPlayer> {
   private static final EnumSet<Action> addPlayer = EnumSet.allOf(Action.class);
   private static final EnumSet<Action> updateDisplayName = EnumSet.of(Action.UPDATE_DISPLAY_NAME);
   private static final EnumSet<Action> updateLatency = EnumSet.of(Action.UPDATE_LATENCY);
   private static final EnumSet<Action> updateGameMode = EnumSet.of(Action.UPDATE_GAME_MODE);
   private static final EnumSet<Action> updateListed = EnumSet.of(Action.UPDATE_LISTED);

   public PaperPacketTabList(@NotNull BukkitTabPlayer player) {
      super(player);
   }

   @Override
   public void removeEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry)));
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateDisplayName, entry, "", null, false, 0, 0, displayName);
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateLatency, entry, "", null, false, latency, 0, null);
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateGameMode, entry, "", null, false, 0, gameMode, null);
   }

   @Override
   public void updateListed(@NonNull UUID entry, boolean listed) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateListed, entry, "", null, listed, 0, 0, null);
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

      this.sendPacket(
         addPlayer, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(), entry.getLatency(), entry.getGameMode(), entry.getDisplayName()
      );
   }

   @Override
   public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
      if (header == null) {
         throw new NullPointerException("header is marked non-null but is null");
      }

      if (footer == null) {
         throw new NullPointerException("footer is marked non-null but is null");
      }

      this.sendPacket(new ClientboundTabListPacket(header.convert(), footer.convert()));
   }

   @Override
   public boolean containsEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      } else {
         return true;
      }
   }

   @Nullable
   @Override
   public TabList.Skin getSkin() {
      Collection<Property> properties = ((CraftPlayer)this.player.getPlayer()).getProfile().getProperties().get("textures");
      if (properties.isEmpty()) {
         return null;
      }

      Property property = properties.iterator().next();
      return new TabList.Skin(property.value(), property.signature());
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      if (packet == null) {
         throw new NullPointerException("packet is marked non-null but is null");
      }

      if (packet instanceof ClientboundTabListPacket tablist) {
         if (this.header == null || this.footer == null) {
            return packet;
         }

         if (tablist.header() != this.header.convert() || tablist.footer() != this.footer.convert()) {
            return new ClientboundTabListPacket(this.header.convert(), this.footer.convert());
         }
      }

      if (packet instanceof ClientboundPlayerInfoUpdatePacket info) {
         EnumSet<Action> actions = info.actions();
         List<net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry> updatedList = new ArrayList<>();
         boolean rewritePacket = false;

         for (net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry nmsData : info.entries()) {
            boolean rewriteEntry = false;
            Component displayName = nmsData.displayName();
            int latency = nmsData.latency();
            int gameMode = nmsData.gameMode().getId();
            if (actions.contains(Action.UPDATE_DISPLAY_NAME)) {
               TabComponent forcedDisplayName = this.getForcedDisplayNames().get(nmsData.profileId());
               if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                  displayName = forcedDisplayName.convert();
                  rewritePacket = true;
                  rewriteEntry = true;
               }
            }

            if (actions.contains(Action.UPDATE_GAME_MODE) && this.getBlockedSpectators().contains(nmsData.profileId()) && gameMode == 3) {
               gameMode = 0;
               rewritePacket = true;
               rewriteEntry = true;
            }

            if (actions.contains(Action.UPDATE_LATENCY) && getForcedLatency() != null) {
               latency = getForcedLatency();
               rewritePacket = true;
               rewriteEntry = true;
            }

            if (actions.contains(Action.ADD_PLAYER)) {
               TAB.getInstance().getFeatureManager().onEntryAdd(this.player, nmsData.profileId(), nmsData.profile().getName());
            }

            updatedList.add(
               rewriteEntry
                  ? new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry(
                     nmsData.profileId(), nmsData.profile(), nmsData.listed(), latency, GameType.byId(gameMode), displayName, nmsData.chatSession()
                  )
                  : nmsData
            );
         }

         if (rewritePacket) {
            return new ClientboundPlayerInfoUpdatePacket(actions, updatedList);
         }
      }

      return packet;
   }

   private void sendPacket(
      @NonNull EnumSet<Action> action,
      @NonNull UUID id,
      @NonNull String name,
      @Nullable TabList.Skin skin,
      boolean listed,
      int latency,
      int gameMode,
      @Nullable TabComponent displayName
   ) {
      if (action == null) {
         throw new NullPointerException("action is marked non-null but is null");
      }

      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
         action,
         new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry(
            id,
            action.contains(Action.ADD_PLAYER) ? this.createProfile(id, name, skin) : null,
            listed,
            latency,
            GameType.byId(gameMode),
            displayName == null ? null : displayName.convert(),
            null
         )
      );
      this.sendPacket(packet);
   }

   @NotNull
   private GameProfile createProfile(@NonNull UUID id, @NonNull String name, @Nullable TabList.Skin skin) {
      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      GameProfile profile = new GameProfile(id, name);
      if (skin != null) {
         profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
      }

      return profile;
   }

   private void sendPacket(@NotNull Packet<?> packet) {
      ((CraftPlayer)this.player.getPlayer()).getHandle().connection.send(packet);
   }
}
