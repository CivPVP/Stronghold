package me.neznamy.tab.shared.features.layout;

import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSlot {
   private static final StringToComponentCache cache = new StringToComponentCache("LayoutPlayerSlot", 100);
   private final int slot;
   private final LayoutView layout;
   private final UUID uniqueId;
   private TabPlayer player;
   private String text = "";

   public void setPlayer(@Nullable TabPlayer newPlayer) {
      if (this.player != newPlayer) {
         this.player = newPlayer;
         if (this.player != null) {
            this.text = "";
         }

         if (this.layout.getViewer().getVersion().getMinorVersion() >= 8 && !this.layout.getViewer().isBedrockPlayer()) {
            this.layout.getViewer().getTabList().removeEntry(this.uniqueId);
            this.layout.getViewer().getTabList().addEntry(this.getSlot(this.layout.getViewer()));
         }
      }
   }

   @NotNull
   public TabList.Entry getSlot(@NotNull TabPlayer viewer) {
      TabPlayer player = this.player;
      TabList.Entry data;
      if (player != null) {
         PlayerList playerList = this.layout.getManager().getPlayerList();
         data = new TabList.Entry(
            this.uniqueId,
            this.layout.getManager().getConfiguration().getDirection().getEntryName(viewer, this.slot, LayoutManagerImpl.isTeamsEnabled()),
            player.getTabList().getSkin(),
            true,
            this.layout.getManager().getPingSpoof() != null ? this.layout.getManager().getPingSpoof().getConfiguration().getValue() : player.getPing(),
            0,
            playerList != null && !player.tablistData.disabled.get() ? playerList.getTabFormat(player, viewer) : TabComponent.legacyText(player.getName()),
            Integer.MAX_VALUE - this.layout.getManager().getConfiguration().getDirection().translateSlot(this.slot),
            true
         );
      } else {
         data = new TabList.Entry(
            this.uniqueId,
            this.layout.getManager().getConfiguration().getDirection().getEntryName(viewer, this.slot, LayoutManagerImpl.isTeamsEnabled()),
            this.layout.getPattern().getDefaultSkin(this.slot),
            true,
            this.layout.getManager().getConfiguration().getEmptySlotPing(),
            0,
            TabComponent.legacyText(this.text),
            Integer.MAX_VALUE - this.layout.getManager().getConfiguration().getDirection().translateSlot(this.slot),
            true
         );
      }

      return data;
   }

   public void setText(@NotNull String text) {
      if (!this.text.equals(text) || this.player != null) {
         this.text = text;
         if (this.player != null) {
            this.setPlayer(null);
         } else {
            if (this.layout.getViewer().isBedrockPlayer()) {
               return;
            }

            this.layout.getViewer().getTabList().updateDisplayName(this.uniqueId, cache.get(text));
         }
      }
   }

   @Generated
   public PlayerSlot(int slot, LayoutView layout, UUID uniqueId) {
      this.slot = slot;
      this.layout = layout;
      this.uniqueId = uniqueId;
   }

   @Generated
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   public TabPlayer getPlayer() {
      return this.player;
   }
}
