package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Generated;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class LayoutView {
   private final LayoutManagerImpl manager;
   private final LayoutPattern pattern;
   private final TabPlayer viewer;
   private final Condition displayCondition;
   private final List<Integer> emptySlots = IntStream.range(1, 81).boxed().collect(Collectors.toList());
   private final Collection<FixedSlot> fixedSlots;
   private final List<ParentGroup> groups = new ArrayList<>();

   public LayoutView(LayoutManagerImpl manager, LayoutPattern pattern, TabPlayer viewer) {
      this.manager = manager;
      this.viewer = viewer;
      this.pattern = pattern;
      this.fixedSlots = pattern.getFixedSlots().values();
      this.displayCondition = pattern.getCondition();

      for (FixedSlot slot : this.fixedSlots) {
         this.emptySlots.remove(Integer.valueOf(slot.getSlot()));
      }

      for (LayoutConfiguration.LayoutDefinition.GroupPattern group : pattern.getGroups()) {
         this.emptySlots.removeAll(Arrays.stream(group.getSlots()).boxed().collect(Collectors.toList()));
         this.groups.add(new ParentGroup(this, group, viewer));
      }
   }

   public void send() {
      if (this.viewer.getVersion().getMinorVersion() >= 8 && !this.viewer.isBedrockPlayer()) {
         for (ParentGroup group : this.groups) {
            group.sendSlots();
         }

         for (FixedSlot slot : this.fixedSlots) {
            this.viewer.getTabList().addEntry(slot.createEntry(this.viewer));
         }

         for (int slot : this.emptySlots) {
            this.viewer
               .getTabList()
               .addEntry(
                  new TabList.Entry(
                     this.manager.getUUID(slot),
                     this.manager.getConfiguration().getDirection().getEntryName(this.viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                     this.pattern.getDefaultSkin(slot),
                     true,
                     this.manager.getConfiguration().getEmptySlotPing(),
                     0,
                     TabComponent.empty(),
                     Integer.MAX_VALUE - this.manager.getConfiguration().getDirection().translateSlot(slot),
                     true
                  )
               );
         }

         this.tick();
      }
   }

   public void destroy() {
      if (this.viewer.getVersion().getMinorVersion() >= 8 && !this.viewer.isBedrockPlayer()) {
         for (UUID id : this.manager.getUuids().values()) {
            this.viewer.getTabList().removeEntry(id);
         }
      }
   }

   public void tick() {
      Stream<TabPlayer> str = this.manager.getSortedPlayers().keySet().stream().filter(this.viewer::canSee);
      List<TabPlayer> players = str.collect(Collectors.toList());

      for (ParentGroup group : this.groups) {
         group.tick(players);
      }
   }

   public PlayerSlot getSlot(@NotNull TabPlayer target) {
      for (ParentGroup group : this.groups) {
         if (group.getPlayers().containsKey(target)) {
            return group.getPlayers().get(target);
         }
      }

      return null;
   }

   @Generated
   public LayoutManagerImpl getManager() {
      return this.manager;
   }

   @Generated
   public LayoutPattern getPattern() {
      return this.pattern;
   }

   @Generated
   public TabPlayer getViewer() {
      return this.viewer;
   }

   @Generated
   public Condition getDisplayCondition() {
      return this.displayCondition;
   }

   @Generated
   public List<Integer> getEmptySlots() {
      return this.emptySlots;
   }

   @Generated
   public Collection<FixedSlot> getFixedSlots() {
      return this.fixedSlots;
   }

   @Generated
   public List<ParentGroup> getGroups() {
      return this.groups;
   }
}
