package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParentGroup {
   @NotNull
   private final LayoutView layout;
   @Nullable
   private final Condition condition;
   private final int[] slots;
   private final TabPlayer viewer;
   private final Map<Integer, PlayerSlot> playerSlots = new HashMap<>();
   private final Map<TabPlayer, PlayerSlot> players = new HashMap<>();

   public ParentGroup(@NotNull LayoutView layout, @NotNull LayoutConfiguration.LayoutDefinition.GroupPattern pattern, @NotNull TabPlayer viewer) {
      this.layout = layout;
      this.condition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(pattern.getCondition());
      this.slots = pattern.getSlots();
      this.viewer = viewer;

      for (int slot : this.slots) {
         this.playerSlots.put(slot, new PlayerSlot(slot, layout, layout.getManager().getUUID(slot)));
      }
   }

   public void tick(@NotNull List<TabPlayer> remainingPlayers) {
      this.players.clear();
      List<TabPlayer> meetingCondition = new ArrayList<>();
      remainingPlayers.removeIf(px -> {
         boolean met = this.condition == null || this.condition.isMet(px);
         if (met) {
            meetingCondition.add(px);
         }

         return met;
      });

      for (int index = 0; index < this.slots.length; index++) {
         int slot = this.slots[index];
         if (this.layout.getManager().getConfiguration().isRemainingPlayersTextEnabled()
            && index == this.slots.length - 1
            && this.playerSlots.size() < meetingCondition.size()) {
            this.playerSlots
               .get(slot)
               .setText(
                  String.format(this.layout.getManager().getConfiguration().getRemainingPlayersText(), meetingCondition.size() - this.playerSlots.size() + 1)
               );
            break;
         }

         if (meetingCondition.size() > index) {
            TabPlayer p = meetingCondition.get(index);
            this.playerSlots.get(slot).setPlayer(p);
            this.players.put(p, this.playerSlots.get(slot));
         } else {
            this.playerSlots.get(slot).setText("");
         }
      }
   }

   public void sendSlots() {
      for (PlayerSlot s : this.playerSlots.values()) {
         this.viewer.getTabList().addEntry(s.getSlot(this.viewer));
      }
   }

   @Generated
   public int[] getSlots() {
      return this.slots;
   }

   @Generated
   public Map<Integer, PlayerSlot> getPlayerSlots() {
      return this.playerSlots;
   }

   @Generated
   public Map<TabPlayer, PlayerSlot> getPlayers() {
      return this.players;
   }
}
