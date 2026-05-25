package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LayoutPattern extends RefreshableFeature implements Layout {
   @NotNull
   private final LayoutManagerImpl manager;
   @NotNull
   private final String name;
   @Nullable
   private final Condition condition;
   @Nullable
   private final String defaultSkinDefinition;
   @Nullable
   private final TabList.Skin defaultSkin;
   private final Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
   private final List<LayoutConfiguration.LayoutDefinition.GroupPattern> groups = new ArrayList<>();

   public LayoutPattern(@NotNull LayoutManagerImpl manager, @NotNull String name, @NotNull LayoutConfiguration.LayoutDefinition def) {
      this.manager = manager;
      this.name = name;
      this.condition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(def.getCondition());
      if (this.condition != null) {
         manager.addUsedPlaceholder(TabConstants.Placeholder.condition(this.condition.getName()));
      }

      this.defaultSkinDefinition = def.getDefaultSkin();
      this.defaultSkin = def.getDefaultSkin() == null ? null : manager.getSkinManager().getSkin(def.getDefaultSkin());

      for (LayoutConfiguration.LayoutDefinition.FixedSlotDefinition fixed : def.getFixedSlots()) {
         this.addFixedSlot(fixed);
      }

      for (Entry<String, LayoutConfiguration.LayoutDefinition.GroupPattern> entry : def.getGroups().entrySet()) {
         this.addGroup(entry.getKey(), entry.getValue().getCondition(), entry.getValue().getSlots());
      }
   }

   public void addFixedSlot(@NotNull LayoutConfiguration.LayoutDefinition.FixedSlotDefinition def) {
      FixedSlot slot = FixedSlot.fromDefinition(def, this, this.manager);
      this.fixedSlots.put(slot.getSlot(), slot);
   }

   public void addGroup(@NotNull String name, @Nullable String condition, int[] slots) {
      this.groups
         .add(
            new LayoutConfiguration.LayoutDefinition.GroupPattern(
               name, condition, Arrays.stream(slots).filter(slot -> !this.fixedSlots.containsKey(slot)).toArray()
            )
         );
      if (condition != null) {
         this.addUsedPlaceholder(
            TabConstants.Placeholder.condition(TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(condition).getName())
         );
      }
   }

   public boolean isConditionMet(@NotNull TabPlayer p) {
      return this.condition == null || this.condition.isMet(p);
   }

   @Nullable
   public TabList.Skin getDefaultSkin(int slot) {
      return this.defaultSkin != null ? this.defaultSkin : this.manager.getSkinManager().getDefaultSkin(slot);
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating player groups";
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      this.manager.tickAllLayouts();
   }

   @Override
   public void addFixedSlot(int slot, @NonNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      this.ensureActive();
      this.addFixedSlot(
         slot,
         text,
         this.defaultSkinDefinition != null ? this.defaultSkinDefinition : this.manager.getConfiguration().getDefaultSkin(slot),
         this.manager.getConfiguration().getEmptySlotPing()
      );
   }

   @Override
   public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (skin == null) {
         throw new NullPointerException("skin is marked non-null but is null");
      }

      this.ensureActive();
      this.addFixedSlot(slot, text, skin, this.manager.getConfiguration().getEmptySlotPing());
   }

   @Override
   public void addFixedSlot(int slot, @NonNull String text, int ping) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      this.ensureActive();
      this.addFixedSlot(
         slot, text, this.defaultSkinDefinition != null ? this.defaultSkinDefinition : this.manager.getConfiguration().getDefaultSkin(slot), ping
      );
   }

   @Override
   public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (skin == null) {
         throw new NullPointerException("skin is marked non-null but is null");
      }

      this.ensureActive();
      if (slot >= 1 && slot <= 80) {
         this.fixedSlots.put(slot, new FixedSlot(this.manager, slot, this, this.manager.getUUID(slot), text, skin, ping));
      } else {
         throw new IllegalArgumentException("Slot must be between 1 - 80 (was " + slot + ")");
      }
   }

   @Override
   public void addGroup(@Nullable String condition, int[] slots) {
      this.ensureActive();
      this.addGroup(UUID.randomUUID().toString(), condition, slots);
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.manager.getFeatureName();
   }

   @NotNull
   @Generated
   public LayoutManagerImpl getManager() {
      return this.manager;
   }

   @NotNull
   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Nullable
   @Generated
   public Condition getCondition() {
      return this.condition;
   }

   @Nullable
   @Generated
   public String getDefaultSkinDefinition() {
      return this.defaultSkinDefinition;
   }

   @Nullable
   @Generated
   public TabList.Skin getDefaultSkin() {
      return this.defaultSkin;
   }

   @Generated
   public Map<Integer, FixedSlot> getFixedSlots() {
      return this.fixedSlots;
   }

   @Generated
   public List<LayoutConfiguration.LayoutDefinition.GroupPattern> getGroups() {
      return this.groups;
   }
}
