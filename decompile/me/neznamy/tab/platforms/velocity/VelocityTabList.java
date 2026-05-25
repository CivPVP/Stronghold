package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VelocityTabList extends TrackedTabList<VelocityTabPlayer> {
   public VelocityTabList(@NotNull VelocityTabPlayer player) {
      super(player);
   }

   @Override
   public void removeEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().removeEntry(entry);
   }

   @Override
   public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setDisplayName(displayName == null ? null : displayName.toAdventure()));
   }

   @Override
   public void updateLatency(@NonNull UUID entry, int latency) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setLatency(latency));
   }

   @Override
   public void updateGameMode(@NonNull UUID entry, int gameMode) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setGameMode(gameMode));
   }

   @Override
   public void updateListed(@NonNull UUID entry, boolean listed) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setListed(listed));
   }

   @Override
   public void updateListOrder(@NonNull UUID entry, int listOrder) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setListOrder(listOrder));
   }

   @Override
   public void updateHat(@NonNull UUID entry, boolean showHat) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setShowHat(showHat));
   }

   @Override
   public void addEntry0(@NonNull TabList.Entry entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      GameProfile profile = new GameProfile(
         entry.getUniqueId(),
         entry.getName(),
         entry.getSkin() == null
            ? Collections.emptyList()
            : Collections.singletonList(new Property("textures", entry.getSkin().getValue(), Objects.requireNonNull(entry.getSkin().getSignature())))
      );
      TabListEntry e = TabListEntry.builder()
         .tabList(this.player.getPlayer().getTabList())
         .profile(profile)
         .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventure())
         .latency(entry.getLatency())
         .gameMode(entry.getGameMode())
         .listed(entry.isListed())
         .listOrder(entry.getListOrder())
         .showHat(entry.isShowHat())
         .build();
      this.removeEntry(entry.getUniqueId());
      this.player.getPlayer().getTabList().addEntry(e);
   }

   @Override
   public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
      if (header == null) {
         throw new NullPointerException("header is marked non-null but is null");
      }

      if (footer == null) {
         throw new NullPointerException("footer is marked non-null but is null");
      }

      this.player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventure(), footer.toAdventure());
   }

   @Override
   public boolean containsEntry(@NonNull UUID entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      } else {
         return this.player.getPlayer().getTabList().containsEntry(entry);
      }
   }

   @Nullable
   @Override
   public TabList.Skin getSkin() {
      List<Property> properties = this.player.getPlayer().getGameProfile().getProperties();
      if (properties.isEmpty()) {
         return null;
      }

      for (Property property : properties) {
         if (property.getName().equals("textures")) {
            return new TabList.Skin(property.getValue(), property.getSignature());
         }
      }

      return null;
   }

   @Override
   public void checkDisplayNames() {
      for (TabListEntry entry : this.player.getPlayer().getTabList().getEntries()) {
         TabComponent expectedComponent = this.getForcedDisplayNames().get(entry.getProfile().getId());
         if (expectedComponent != null && entry.getDisplayNameComponent().orElse(null) != expectedComponent.toAdventure()) {
            entry.setDisplayName(expectedComponent.toAdventure());
         }
      }
   }

   @Override
   public void checkGameModes() {
      for (TabListEntry entry : this.player.getPlayer().getTabList().getEntries()) {
         if (this.getBlockedSpectators().contains(entry.getProfile().getId()) && entry.getGameMode() == 3) {
            entry.setGameMode(0);
         }
      }
   }

   @Override
   public void checkHeaderFooter() {
   }
}
