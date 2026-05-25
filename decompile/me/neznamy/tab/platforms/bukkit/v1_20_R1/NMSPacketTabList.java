package me.neznamy.tab.platforms.bukkit.v1_20_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
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
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.a;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.b;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NMSPacketTabList extends TrackedTabList<BukkitTabPlayer> {
   private static final Field PLAYERS = ReflectionUtils.getOnlyField(ClientboundPlayerInfoUpdatePacket.class, List.class);
   private static final a ADD_PLAYER = a.valueOf("ADD_PLAYER");
   private static final a UPDATE_GAME_MODE = a.valueOf("UPDATE_GAME_MODE");
   private static final a UPDATE_LATENCY = a.valueOf("UPDATE_LATENCY");
   private static final a UPDATE_DISPLAY_NAME = a.valueOf("UPDATE_DISPLAY_NAME");
   private static final a UPDATE_LISTED = a.valueOf("UPDATE_LISTED");
   private static final EnumSet<a> ADD_PLAYER_SET = EnumSet.allOf(a.class);
   private static final EnumSet<a> UPDATE_GAME_MODE_SET = EnumSet.of(UPDATE_GAME_MODE);
   private static final EnumSet<a> UPDATE_DISPLAY_NAME_SET = EnumSet.of(UPDATE_DISPLAY_NAME);
   private static final EnumSet<a> UPDATE_LATENCY_SET = EnumSet.of(UPDATE_LATENCY);
   private static final EnumSet<a> UPDATE_LISTED_SET = EnumSet.of(UPDATE_LISTED);

   public NMSPacketTabList(@NotNull BukkitTabPlayer player) {
      super(player);
   }

   @Override
   public void removeEntry(@NonNull UUID entry) {
      try {
         if (entry == null) {
            throw new NullPointerException("entry is marked non-null but is null");
         }

         this.sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry)));
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(UPDATE_DISPLAY_NAME_SET, entry, "", null, false, 0, 0, displayName);
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(UPDATE_LATENCY_SET, entry, "", null, false, latency, 0, null);
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(UPDATE_GAME_MODE_SET, entry, "", null, false, 0, gameMode, null);
   }

   @Override
   public void updateListed(@NonNull UUID entry, boolean listed) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(UPDATE_LISTED_SET, entry, "", null, listed, 0, 0, null);
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
         ADD_PLAYER_SET,
         entry.getUniqueId(),
         entry.getName(),
         entry.getSkin(),
         entry.isListed(),
         entry.getLatency(),
         entry.getGameMode(),
         entry.getDisplayName()
      );
   }

   @Override
   public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
      try {
         if (header == null) {
            throw new NullPointerException("header is marked non-null but is null");
         }

         if (footer == null) {
            throw new NullPointerException("footer is marked non-null but is null");
         }

         this.sendPacket(new PacketPlayOutPlayerListHeaderFooter(header.convert(), footer.convert()));
      } catch (Throwable $ex) {
         throw $ex;
      }
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
      return new TabList.Skin(property.getValue(), property.getSignature());
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         }

         if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
            PacketPlayOutPlayerListHeaderFooter tablist = (PacketPlayOutPlayerListHeaderFooter)packet;
            if (this.header == null || this.footer == null) {
               return packet;
            }

            if (tablist.a != this.header.convert() || tablist.b != this.footer.convert()) {
               return new PacketPlayOutPlayerListHeaderFooter(this.header.convert(), this.footer.convert());
            }
         }

         if (!(packet instanceof ClientboundPlayerInfoUpdatePacket)) {
            return packet;
         }

         ClientboundPlayerInfoUpdatePacket info = (ClientboundPlayerInfoUpdatePacket)packet;
         EnumSet<a> actions = info.a();
         List<b> updatedList = new ArrayList<>();
         boolean rewritePacket = false;

         for (b nmsData : info.c()) {
            UUID profileId = nmsData.a();
            boolean rewriteEntry = false;
            IChatBaseComponent displayName = nmsData.f();
            int latency = nmsData.d();
            int gameMode = nmsData.e().a();
            if (actions.contains(UPDATE_DISPLAY_NAME)) {
               TabComponent forcedDisplayName = this.getForcedDisplayNames().get(profileId);
               if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                  displayName = forcedDisplayName.convert();
                  rewritePacket = true;
                  rewriteEntry = true;
               }
            }

            if (actions.contains(UPDATE_GAME_MODE) && this.getBlockedSpectators().contains(profileId) && gameMode == 3) {
               gameMode = 0;
               rewritePacket = true;
               rewriteEntry = true;
            }

            if (actions.contains(UPDATE_LATENCY) && getForcedLatency() != null) {
               latency = getForcedLatency();
               rewritePacket = true;
               rewriteEntry = true;
            }

            if (actions.contains(ADD_PLAYER)) {
               TAB.getInstance().getFeatureManager().onEntryAdd(this.player, profileId, nmsData.b().getName());
            }

            updatedList.add(rewriteEntry ? new b(profileId, nmsData.b(), nmsData.c(), latency, EnumGamemode.a(gameMode), displayName, nmsData.g()) : nmsData);
         }

         if (rewritePacket) {
            ClientboundPlayerInfoUpdatePacket newPacket = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
            PLAYERS.set(newPacket, updatedList);
            return newPacket;
         } else {
            return packet;
         }
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private void sendPacket(
      @NonNull EnumSet<a> action,
      @NonNull UUID id,
      @NonNull String name,
      @Nullable TabList.Skin skin,
      boolean listed,
      int latency,
      int gameMode,
      @Nullable TabComponent displayName
   ) {
      try {
         if (action == null) {
            throw new NullPointerException("action is marked non-null but is null");
         }

         if (id == null) {
            throw new NullPointerException("id is marked non-null but is null");
         }

         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         }

         ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(action, Collections.emptyList());
         PLAYERS.set(
            packet,
            Collections.singletonList(
               new b(
                  id, this.createProfile(id, name, skin), listed, latency, EnumGamemode.a(gameMode), displayName == null ? null : displayName.convert(), null
               )
            )
         );
         this.sendPacket(packet);
      } catch (Throwable $ex) {
         throw $ex;
      }
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
      ((CraftPlayer)this.player.getPlayer()).getHandle().c.a(packet);
   }
}
