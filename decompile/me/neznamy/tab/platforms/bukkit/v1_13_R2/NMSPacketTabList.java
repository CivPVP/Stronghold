package me.neznamy.tab.platforms.bukkit.v1_13_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumGamemode;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NMSPacketTabList extends TrackedTabList<BukkitTabPlayer> {
   private static final Class<?> PlayerInfoData = PacketPlayOutPlayerInfo.class.getDeclaredClasses()[0];
   private static final Constructor<?> newPlayerInfoData = ReflectionUtils.getOnlyConstructor(PlayerInfoData);
   private static final Field ACTION = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, EnumPlayerInfoAction.class);
   private static final Field PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, List.class);
   private static final Field PlayerInfoData_Profile = ReflectionUtils.getOnlyField(PlayerInfoData, GameProfile.class);
   private static final Field PlayerInfoData_Latency = ReflectionUtils.getFields(PlayerInfoData, int.class).get(0);
   private static final Field PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(PlayerInfoData, IChatBaseComponent.class);
   private static final Field PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(PlayerInfoData, EnumGamemode.class);

   public NMSPacketTabList(@NotNull BukkitTabPlayer player) {
      super(player);
   }

   @Override
   public void removeEntry(@NonNull UUID entry) {
      try {
         if (entry == null) {
            throw new NullPointerException("entry is marked non-null but is null");
         }

         this.sendPacket(EnumPlayerInfoAction.REMOVE_PLAYER, entry, "", null, 0, 0, null);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, entry, "", null, 0, 0, displayName);
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(EnumPlayerInfoAction.UPDATE_LATENCY, entry, "", null, latency, 0, null);
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(EnumPlayerInfoAction.UPDATE_GAME_MODE, entry, "", null, 0, gameMode, null);
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

      this.sendPacket(
         EnumPlayerInfoAction.ADD_PLAYER,
         entry.getUniqueId(),
         entry.getName(),
         entry.getSkin(),
         entry.getLatency(),
         entry.getGameMode(),
         entry.getDisplayName()
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

      this.sendPacket(this.newHeaderFooter(header, footer));
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

   @NonNull
   private PacketPlayOutPlayerListHeaderFooter newHeaderFooter(@NotNull TabComponent header, @NonNull TabComponent footer) {
      if (footer == null) {
         throw new NullPointerException("footer is marked non-null but is null");
      }

      PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
      packet.header = header.convert();
      packet.footer = footer.convert();
      return packet;
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

            if (tablist.header != this.header.convert() || tablist.footer != this.footer.convert()) {
               return this.newHeaderFooter(this.header, this.footer);
            }
         }

         if (!(packet instanceof PacketPlayOutPlayerInfo)) {
            return packet;
         }

         PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo)packet;
         EnumPlayerInfoAction action = (EnumPlayerInfoAction)ACTION.get(info);
         List<Object> updatedList = new ArrayList<>();
         boolean rewritePacket = false;

         for (Object nmsData : (List)PLAYERS.get(info)) {
            boolean rewriteEntry = false;
            GameProfile profile = (GameProfile)PlayerInfoData_Profile.get(nmsData);
            UUID id = profile.getId();
            IChatBaseComponent displayName = (IChatBaseComponent)PlayerInfoData_DisplayName.get(nmsData);
            int latency = PlayerInfoData_Latency.getInt(nmsData);
            int gameMode = ((EnumGamemode)PlayerInfoData_GameMode.get(nmsData)).getId();
            if (action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME || action == EnumPlayerInfoAction.ADD_PLAYER) {
               TabComponent forcedDisplayName = this.getForcedDisplayNames().get(id);
               if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                  displayName = forcedDisplayName.convert();
                  rewritePacket = true;
                  rewriteEntry = true;
               }
            }

            if ((action == EnumPlayerInfoAction.UPDATE_GAME_MODE || action == EnumPlayerInfoAction.ADD_PLAYER)
               && this.getBlockedSpectators().contains(id)
               && gameMode == 3) {
               gameMode = 0;
               rewritePacket = true;
               rewriteEntry = true;
            }

            if ((action == EnumPlayerInfoAction.UPDATE_LATENCY || action == EnumPlayerInfoAction.ADD_PLAYER) && getForcedLatency() != null) {
               latency = getForcedLatency();
               rewritePacket = true;
               rewriteEntry = true;
            }

            if (action == EnumPlayerInfoAction.ADD_PLAYER) {
               TAB.getInstance().getFeatureManager().onEntryAdd(this.player, id, profile.getName());
            }

            updatedList.add(
               rewriteEntry ? this.newPlayerInfoData((PacketPlayOutPlayerInfo)packet, profile, latency, EnumGamemode.getById(gameMode), displayName) : nmsData
            );
         }

         if (rewritePacket) {
            PacketPlayOutPlayerInfo newPacket = new PacketPlayOutPlayerInfo(action, Collections.emptyList());
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
      @NonNull EnumPlayerInfoAction action,
      @NonNull UUID id,
      @NonNull String name,
      @Nullable TabList.Skin skin,
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

         PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(action, new EntityPlayer[0]);
         PLAYERS.set(
            packet,
            Collections.singletonList(
               this.newPlayerInfoData(
                  packet, this.createProfile(id, name, skin), latency, EnumGamemode.getById(gameMode), displayName == null ? null : displayName.convert()
               )
            )
         );
         this.sendPacket(packet);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @NotNull
   private Object newPlayerInfoData(
      @NonNull PacketPlayOutPlayerInfo packet,
      @NonNull GameProfile profile,
      int latency,
      @NonNull EnumGamemode gameMode,
      @Nullable IChatBaseComponent displayName
   ) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         } else if (profile == null) {
            throw new NullPointerException("profile is marked non-null but is null");
         } else if (gameMode == null) {
            throw new NullPointerException("gameMode is marked non-null but is null");
         } else {
            return newPlayerInfoData.newInstance(packet, profile, latency, gameMode, displayName);
         }
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
      ((CraftPlayer)this.player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
   }
}
