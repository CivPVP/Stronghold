package me.neznamy.tab.shared.platform.decorators;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TrackedTabList<P extends TabPlayer> implements TabList {
   private static Integer forcedLatency;
   protected final P player;
   private final Map<UUID, TabComponent> forcedDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());
   private final Set<UUID> blockedSpectators = Collections.synchronizedSet(new HashSet<>());
   @Nullable
   protected TabComponent header;
   @Nullable
   protected TabComponent footer;

   @Override
   public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.forcedDisplayNames.put(entry, displayName);
      if (this.player.getVersion().getMinorVersion() >= 8) {
         this.updateDisplayName0(entry, displayName);
      }
   }

   @Override
   public void addEntry(@NonNull TabList.Entry entry) {
      if (entry == null) {
         throw new NullPointerException("entry is marked non-null but is null");
      }

      this.forcedDisplayNames.put(entry.getUniqueId(), entry.getDisplayName());
      this.addEntry0(entry);
      if (this.player.getVersion().getMinorVersion() == 8) {
         this.updateDisplayName0(entry.getUniqueId(), entry.getDisplayName());
      }
   }

   @Override
   public void updateDisplayName(@NonNull TabPlayer player, @Nullable TabComponent displayName) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.forcedDisplayNames.put(player.getTablistId(), displayName);
      if (player.getVersion().getMinorVersion() >= 8) {
         if (this.containsEntry(player.getTablistId()) && this.player.canSee(player)) {
            this.updateDisplayName0(player.getTablistId(), displayName);
         }
      }
   }

   @Override
   public void updateLatency(@NonNull TabPlayer player, int latency) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (this.containsEntry(player.getTablistId()) && this.player.canSee(player)) {
         this.updateLatency(player.getTablistId(), latency);
      }
   }

   @Override
   public void updateGameMode(@NonNull TabPlayer player, int gameMode) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (this.containsEntry(player.getTablistId()) && this.player.canSee(player)) {
         this.updateGameMode(player.getTablistId(), gameMode);
      }
   }

   @Override
   public void setPlayerListHeaderFooter(@Nullable TabComponent header, @Nullable TabComponent footer) {
      this.header = header;
      this.footer = footer;
      this.setPlayerListHeaderFooter0(header == null ? TabComponent.empty() : header, footer == null ? TabComponent.empty() : footer);
   }

   public void resendHeaderFooter() {
      if (this.header != null && this.footer != null) {
         this.setPlayerListHeaderFooter0(this.header, this.footer);
      }
   }

   public void checkDisplayNames() {
   }

   public void checkGameModes() {
   }

   public void checkHeaderFooter() {
   }

   @NotNull
   public Object onPacketSend(@NonNull Object packet) {
      return packet;
   }

   @Override
   public void blockSpectator(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.blockedSpectators.add(player.getTablistId());
      this.updateGameMode(player, 0);
   }

   @Override
   public void unblockSpectator(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.blockedSpectators.remove(player.getTablistId());
      this.updateGameMode(player, player.getGamemode());
   }

   public abstract void updateDisplayName0(@NonNull UUID var1, @Nullable TabComponent var2);

   public abstract void addEntry0(@NonNull TabList.Entry var1);

   public abstract void setPlayerListHeaderFooter0(@NonNull TabComponent var1, @NonNull TabComponent var2);

   @Generated
   public TrackedTabList(P player) {
      this.player = player;
   }

   @Generated
   public P getPlayer() {
      return this.player;
   }

   @Generated
   public Map<UUID, TabComponent> getForcedDisplayNames() {
      return this.forcedDisplayNames;
   }

   @Generated
   public Set<UUID> getBlockedSpectators() {
      return this.blockedSpectators;
   }

   @Nullable
   @Generated
   public TabComponent getHeader() {
      return this.header;
   }

   @Nullable
   @Generated
   public TabComponent getFooter() {
      return this.footer;
   }

   @Generated
   public static Integer getForcedLatency() {
      return forcedLatency;
   }

   @Generated
   public static void setForcedLatency(Integer forcedLatency) {
      TrackedTabList.forcedLatency = forcedLatency;
   }
}
