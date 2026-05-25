package me.neznamy.tab.platforms.fabric;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.class_1934;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2703;
import net.minecraft.class_2772;
import net.minecraft.class_7828;
import net.minecraft.class_2703.class_2705;
import net.minecraft.class_2703.class_5893;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FabricTabList extends TrackedTabList<FabricTabPlayer> {
   private static final EnumSet<class_5893> addPlayer = EnumSet.allOf(class_5893.class);
   private static final EnumSet<class_5893> updateDisplayName = EnumSet.of(class_5893.field_29139);
   private static final EnumSet<class_5893> updateLatency = EnumSet.of(class_5893.field_29138);
   private static final EnumSet<class_5893> updateGameMode = EnumSet.of(class_5893.field_29137);
   private static final EnumSet<class_5893> updateListed = EnumSet.of(class_5893.field_40700);
   private static final EnumSet<class_5893> updateListOrder = EnumSet.of(class_5893.field_52324);
   private static final EnumSet<class_5893> updateHat = EnumSet.of(class_5893.field_54981);
   private static final Field entries = ReflectionUtils.getOnlyField(class_2703.class, List.class);

   public FabricTabList(@NotNull FabricTabPlayer player) {
      super(player);
   }

   @Override
   public void removeEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(new class_7828(Collections.singletonList(entry)));
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateDisplayName, entry, "", null, false, 0, 0, displayName, 0, false);
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateLatency, entry, "", null, false, latency, 0, null, 0, false);
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateGameMode, entry, "", null, false, 0, gameMode, null, 0, false);
   }

   @Override
   public void updateListed(@NonNull UUID entry, boolean listed) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateListed, entry, "", null, listed, 0, 0, null, 0, false);
   }

   @Override
   public void updateListOrder(@NonNull UUID entry, int listOrder) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateListOrder, entry, "", null, false, 0, 0, null, listOrder, false);
   }

   @Override
   public void updateHat(@NonNull UUID entry, boolean showHat) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(updateHat, entry, "", null, false, 0, 0, null, 0, showHat);
   }

   @Override
   public void addEntry0(@NonNull TabList.Entry entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.sendPacket(
         addPlayer,
         entry.getUniqueId(),
         entry.getName(),
         entry.getSkin(),
         entry.isListed(),
         entry.getLatency(),
         entry.getGameMode(),
         entry.getDisplayName(),
         entry.getListOrder(),
         entry.isShowHat()
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

      this.sendPacket(new class_2772(header.convert(), footer.convert()));
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
      Collection<Property> properties = this.player.getPlayer().method_7334().properties().get("textures");
      if (properties.isEmpty()) {
         return null;
      }

      Property property = properties.iterator().next();
      return new TabList.Skin(property.value(), property.signature());
   }

   @NotNull
   @Override
   public Object onPacketSend(@NonNull Object packet) {
      try {
         if (packet == null) {
            throw new NullPointerException("packet is marked non-null but is null");
         }

         if (packet instanceof class_2772 tablist) {
            if (this.header == null || this.footer == null) {
               return packet;
            }

            if (tablist.comp_2282() != this.header.convert() || tablist.comp_2283() != this.footer.convert()) {
               return new class_2772(this.header.convert(), this.footer.convert());
            }
         }

         if (packet instanceof class_2703 info) {
            EnumSet<class_5893> actions = info.method_46327();
            List<class_2705> updatedList = new ArrayList<>();
            boolean rewritePacket = false;

            for (class_2705 nmsData : info.method_46329()) {
               boolean rewriteEntry = false;
               class_2561 displayName = nmsData.comp_1111();
               int gameMode = nmsData.comp_1110().method_8379();
               int latency = nmsData.comp_1109();
               if (actions.contains(class_5893.field_29139)) {
                  TabComponent forcedDisplayName = this.getForcedDisplayNames().get(nmsData.comp_1106());
                  if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                     displayName = forcedDisplayName.convert();
                     rewritePacket = true;
                     rewriteEntry = true;
                  }
               }

               if (actions.contains(class_5893.field_29137) && this.getBlockedSpectators().contains(nmsData.comp_1106()) && gameMode == 3) {
                  gameMode = 0;
                  rewritePacket = true;
                  rewriteEntry = true;
               }

               if (actions.contains(class_5893.field_29138) && getForcedLatency() != null) {
                  latency = getForcedLatency();
                  rewritePacket = true;
                  rewriteEntry = true;
               }

               if (actions.contains(class_5893.field_29136)) {
                  TAB.getInstance().getFeatureManager().onEntryAdd(this.player, nmsData.comp_1106(), nmsData.comp_1107().name());
               }

               updatedList.add(
                  rewriteEntry
                     ? new class_2705(
                        nmsData.comp_1106(),
                        nmsData.comp_1107(),
                        nmsData.comp_1108(),
                        latency,
                        class_1934.method_8384(gameMode),
                        displayName,
                        nmsData.comp_3324(),
                        nmsData.comp_2889(),
                        nmsData.comp_1112()
                     )
                     : nmsData
               );
            }

            if (rewritePacket) {
               class_2703 newPacket = new class_2703(actions, Collections.emptyList());
               entries.set(newPacket, updatedList);
               return newPacket;
            }
         }

         return packet;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private void sendPacket(
      @NonNull EnumSet<class_5893> action,
      @NonNull UUID id,
      @NonNull String name,
      @Nullable TabList.Skin skin,
      boolean listed,
      int latency,
      int gameMode,
      @Nullable TabComponent displayName,
      int listOrder,
      boolean showHat
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

         class_2703 packet = new class_2703(action, Collections.emptyList());
         entries.set(
            packet,
            Collections.singletonList(
               new class_2705(
                  id,
                  action.contains(class_5893.field_29136) ? this.createProfile(id, name, skin) : null,
                  listed,
                  latency,
                  class_1934.method_8384(gameMode),
                  displayName == null ? null : displayName.convert(),
                  showHat,
                  listOrder,
                  null
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

      Builder<String, Property> builder = ImmutableMultimap.builder();
      if (skin != null) {
         builder.put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
      }

      return new GameProfile(id, name, new PropertyMap(builder.build()));
   }

   private void sendPacket(@NotNull class_2596<?> packet) {
      this.player.getPlayer().field_13987.method_14364(packet);
   }
}
