package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Generated;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LayoutConfiguration {
   @NotNull
   private final LayoutConfiguration.Direction direction;
   @NotNull
   private final String defaultSkin;
   private final boolean remainingPlayersTextEnabled;
   @NotNull
   private final String remainingPlayersText;
   private final int emptySlotPing;
   @NotNull
   private final Map<Integer, String> defaultSkinHashMap;
   @NotNull
   private final LinkedHashMap<String, LayoutConfiguration.LayoutDefinition> layouts;

   @NotNull
   public static LayoutConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(
         Arrays.asList(
            "enabled",
            "direction",
            "default-skin",
            "enable-remaining-players-text",
            "remaining-players-text",
            "empty-slot-ping-value",
            "default-skins",
            "layouts"
         )
      );
      String directionString = section.getString("direction", "COLUMNS");

      LayoutConfiguration.Direction direction;
      try {
         direction = LayoutConfiguration.Direction.valueOf(directionString);
      } catch (IllegalArgumentException e) {
         section.startupWarn(
            "\""
               + directionString
               + "\" is not a valid type of layout direction. Valid options are: "
               + Arrays.deepToString(LayoutConfiguration.Direction.values())
               + ". Using COLUMNS"
         );
         direction = LayoutConfiguration.Direction.COLUMNS;
      }

      ConfigurationSection defaultSkins = section.getConfigurationSection("default-skins");
      Map<Integer, String> defaultSkinHashMap = new HashMap<>();

      for (Object groupName : defaultSkins.getKeys()) {
         String asString = groupName.toString();
         ConfigurationSection groupSection = defaultSkins.getConfigurationSection(asString);
         String skin = groupSection.getString("skin");

         for (String line : section.getStringList("slots", Collections.emptyList())) {
            String[] arr = line.split("-");
            int from = Integer.parseInt(arr[0]);
            int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);

            for (int i = from; i <= to; i++) {
               defaultSkinHashMap.put(i, skin);
            }
         }
      }

      ConfigurationSection layoutsSection = section.getConfigurationSection("layouts");
      LinkedHashMap<String, LayoutConfiguration.LayoutDefinition> layouts = new LinkedHashMap<>();

      for (Object bar : layoutsSection.getKeys()) {
         String asString = bar.toString();
         layouts.put(asString, LayoutConfiguration.LayoutDefinition.fromSection(asString, layoutsSection.getConfigurationSection(asString)));
      }

      return new LayoutConfiguration(
         direction,
         section.getString("default-skin", "mineskin:1753261242"),
         section.getBoolean("enable-remaining-players-text", true),
         EnumChatFormat.color(section.getString("remaining-players-text", "... and %s more")),
         section.getInt("empty-slot-ping-value", 1000),
         defaultSkinHashMap,
         layouts
      );
   }

   @NotNull
   public String getDefaultSkin(int slot) {
      return this.defaultSkinHashMap.getOrDefault(slot, this.defaultSkin);
   }

   @NotNull
   @Generated
   public LayoutConfiguration.Direction getDirection() {
      return this.direction;
   }

   @NotNull
   @Generated
   public String getDefaultSkin() {
      return this.defaultSkin;
   }

   @Generated
   public boolean isRemainingPlayersTextEnabled() {
      return this.remainingPlayersTextEnabled;
   }

   @NotNull
   @Generated
   public String getRemainingPlayersText() {
      return this.remainingPlayersText;
   }

   @Generated
   public int getEmptySlotPing() {
      return this.emptySlotPing;
   }

   @NotNull
   @Generated
   public Map<Integer, String> getDefaultSkinHashMap() {
      return this.defaultSkinHashMap;
   }

   @NotNull
   @Generated
   public LinkedHashMap<String, LayoutConfiguration.LayoutDefinition> getLayouts() {
      return this.layouts;
   }

   @Generated
   public LayoutConfiguration(
      @NotNull LayoutConfiguration.Direction direction,
      @NotNull String defaultSkin,
      boolean remainingPlayersTextEnabled,
      @NotNull String remainingPlayersText,
      int emptySlotPing,
      @NotNull Map<Integer, String> defaultSkinHashMap,
      @NotNull LinkedHashMap<String, LayoutConfiguration.LayoutDefinition> layouts
   ) {
      if (direction == null) {
         throw new NullPointerException("direction is marked non-null but is null");
      }

      if (defaultSkin == null) {
         throw new NullPointerException("defaultSkin is marked non-null but is null");
      }

      if (remainingPlayersText == null) {
         throw new NullPointerException("remainingPlayersText is marked non-null but is null");
      }

      if (defaultSkinHashMap == null) {
         throw new NullPointerException("defaultSkinHashMap is marked non-null but is null");
      }

      if (layouts == null) {
         throw new NullPointerException("layouts is marked non-null but is null");
      }

      this.direction = direction;
      this.defaultSkin = defaultSkin;
      this.remainingPlayersTextEnabled = remainingPlayersTextEnabled;
      this.remainingPlayersText = remainingPlayersText;
      this.emptySlotPing = emptySlotPing;
      this.defaultSkinHashMap = defaultSkinHashMap;
      this.layouts = layouts;
   }

   public enum Direction {
      COLUMNS(slot -> slot),
      ROWS(slot -> (slot - 1) % 4 * 20 + (slot - (slot - 1) % 4) / 4 + 1);

      @NotNull
      private final Function<Integer, Integer> slotTranslator;

      public int translateSlot(int slot) {
         return this.slotTranslator.apply(slot);
      }

      public String getEntryName(@NotNull TabPlayer viewer, int slot, boolean teamsEnabled) {
         boolean legacySorting = viewer.getVersionId() < ProtocolVersion.V1_19_3.getNetworkId();
         boolean modernSorting = viewer.getVersionId() >= ProtocolVersion.V1_21_2.getNetworkId() && TAB.getInstance().getPlatform().supportsListOrder();
         if (legacySorting || modernSorting) {
            return "";
         } else {
            return teamsEnabled ? "|slot_" + (10 + this.slotTranslator.apply(slot)) : " slot_" + (10 + this.slotTranslator.apply(slot));
         }
      }

      @Generated
      Direction(@NotNull final Function<Integer, Integer> slotTranslator) {
         if (slotTranslator == null) {
            throw new NullPointerException("slotTranslator is marked non-null but is null");
         }

         this.slotTranslator = slotTranslator;
      }
   }

   public static class LayoutDefinition {
      @Nullable
      private final String condition;
      @Nullable
      private final String defaultSkin;
      @NotNull
      private final List<LayoutConfiguration.LayoutDefinition.FixedSlotDefinition> fixedSlots;
      @NotNull
      private final LinkedHashMap<String, LayoutConfiguration.LayoutDefinition.GroupPattern> groups;

      public static LayoutConfiguration.LayoutDefinition fromSection(@NotNull String name, @NotNull ConfigurationSection section) {
         section.checkForUnknownKey(Arrays.asList("condition", "default-skin", "fixed-slots", "groups"));
         List<LayoutConfiguration.LayoutDefinition.FixedSlotDefinition> fixedSlots = new ArrayList<>();

         for (String line : section.getStringList("fixed-slots", Collections.emptyList())) {
            LayoutConfiguration.LayoutDefinition.FixedSlotDefinition def = LayoutConfiguration.LayoutDefinition.FixedSlotDefinition.fromLine(
               line, name, section
            );
            if (def != null) {
               fixedSlots.add(def);
            }
         }

         ConfigurationSection groupsSection = section.getConfigurationSection("groups");
         LinkedHashMap<String, LayoutConfiguration.LayoutDefinition.GroupPattern> groups = new LinkedHashMap<>();
         String noConditionGroup = null;
         Map<Integer, String> takenSlots = new HashMap<>();

         for (Object groupName : groupsSection.getKeys()) {
            String asString = groupName.toString();
            LayoutConfiguration.LayoutDefinition.GroupPattern pattern = LayoutConfiguration.LayoutDefinition.GroupPattern.fromSection(
               groupsSection.getConfigurationSection(asString), name, asString
            );
            if (noConditionGroup != null) {
               section.startupWarn(
                  "Layout \""
                     + name
                     + "\"'s player group \""
                     + groupName
                     + "\" is unreachable, because it is defined after group \""
                     + noConditionGroup
                     + "\", which has no condition requirement."
               );
            } else if (pattern.condition == null) {
               noConditionGroup = asString;
            }

            for (int slot : pattern.slots) {
               if (takenSlots.containsKey(slot)) {
                  section.startupWarn(
                     "Layout \""
                        + name
                        + "\"'s player group \""
                        + pattern.name
                        + "\" defines slot "
                        + slot
                        + ", but this slot is already taken by group \""
                        + takenSlots.get(slot)
                        + "\", which will take priority."
                  );
               } else {
                  takenSlots.put(slot, pattern.name);
               }
            }

            groups.put(asString, pattern);
         }

         return new LayoutConfiguration.LayoutDefinition(section.getString("condition"), section.getString("default-skin"), fixedSlots, groups);
      }

      @Nullable
      @Generated
      public String getCondition() {
         return this.condition;
      }

      @Nullable
      @Generated
      public String getDefaultSkin() {
         return this.defaultSkin;
      }

      @NotNull
      @Generated
      public List<LayoutConfiguration.LayoutDefinition.FixedSlotDefinition> getFixedSlots() {
         return this.fixedSlots;
      }

      @NotNull
      @Generated
      public LinkedHashMap<String, LayoutConfiguration.LayoutDefinition.GroupPattern> getGroups() {
         return this.groups;
      }

      @Generated
      public LayoutDefinition(
         @Nullable String condition,
         @Nullable String defaultSkin,
         @NotNull List<LayoutConfiguration.LayoutDefinition.FixedSlotDefinition> fixedSlots,
         @NotNull LinkedHashMap<String, LayoutConfiguration.LayoutDefinition.GroupPattern> groups
      ) {
         if (fixedSlots == null) {
            throw new NullPointerException("fixedSlots is marked non-null but is null");
         }

         if (groups == null) {
            throw new NullPointerException("groups is marked non-null but is null");
         }

         this.condition = condition;
         this.defaultSkin = defaultSkin;
         this.fixedSlots = fixedSlots;
         this.groups = groups;
      }

      public static class FixedSlotDefinition {
         private final int slot;
         @NotNull
         private final String text;
         @Nullable
         private final String skin;
         @Nullable
         private final Integer ping;

         @Nullable
         private static LayoutConfiguration.LayoutDefinition.FixedSlotDefinition fromLine(
            @NotNull String line, @NotNull String layoutName, @NotNull ConfigurationSection section
         ) {
            String[] array = line.split("\\|");
            if (array.length < 2) {
               section.startupWarn(
                  "Layout "
                     + layoutName
                     + " has invalid fixed slot defined as \""
                     + line
                     + "\". Supported values are \"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, TEXT is displayed text and SKIN is skin used for the slot"
               );
               return null;
            }

            int slot;
            try {
               slot = Integer.parseInt(array[0]);
               if (slot < 1 || slot > 80) {
                  section.startupWarn("Layout " + layoutName + " has invalid fixed slot value \"" + slot + "\" defined. Slots must range between 1 - 80.");
                  return null;
               }
            } catch (NumberFormatException e) {
               section.startupWarn(
                  "Layout "
                     + layoutName
                     + " has invalid fixed slot defined as \""
                     + line
                     + "\". Supported values are \"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, TEXT is displayed text and SKIN is skin used for the slot"
               );
               return null;
            }

            String skin = array.length > 2 ? array[2] : null;
            Integer ping = null;
            if (array.length > 3) {
               try {
                  ping = (int)Math.round(Double.parseDouble(array[3]));
               } catch (NumberFormatException ignored) {
                  section.startupWarn("Layout " + layoutName + " has fixed slot with defined ping \"" + array[3] + "\", which is not a valid number");
               }
            }

            return new LayoutConfiguration.LayoutDefinition.FixedSlotDefinition(slot, array[1], skin, ping);
         }

         @Generated
         public int getSlot() {
            return this.slot;
         }

         @NotNull
         @Generated
         public String getText() {
            return this.text;
         }

         @Nullable
         @Generated
         public String getSkin() {
            return this.skin;
         }

         @Nullable
         @Generated
         public Integer getPing() {
            return this.ping;
         }

         @Generated
         public FixedSlotDefinition(int slot, @NotNull String text, @Nullable String skin, @Nullable Integer ping) {
            if (text == null) {
               throw new NullPointerException("text is marked non-null but is null");
            }

            this.slot = slot;
            this.text = text;
            this.skin = skin;
            this.ping = ping;
         }
      }

      public static class GroupPattern {
         @NotNull
         private final String name;
         @Nullable
         private final String condition;
         private final int[] slots;

         @NotNull
         private static LayoutConfiguration.LayoutDefinition.GroupPattern fromSection(
            @NotNull ConfigurationSection section, @NotNull String layout, @NotNull String groupName
         ) {
            section.checkForUnknownKey(Arrays.asList("condition", "slots"));
            List<Integer> positions = new ArrayList<>();

            for (String line : section.getStringList("slots", Collections.emptyList())) {
               String[] arr = line.split("-");
               int from = Integer.parseInt(arr[0]);
               int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);

               for (int i = from; i <= to; i++) {
                  if (i < 1 || i > 80) {
                     section.startupWarn(
                        "Layout "
                           + layout
                           + "'s player group \""
                           + groupName
                           + "\" has invalid slot value \""
                           + i
                           + "\" defined. Slots must range between 1 - 80."
                     );
                  } else if (positions.contains(i)) {
                     section.startupWarn("Layout " + layout + "'s player group \"" + groupName + "\" has duplicated slot \"" + i + "\".");
                  } else {
                     positions.add(i);
                  }
               }
            }

            String condition = section.getString("condition");
            return new LayoutConfiguration.LayoutDefinition.GroupPattern(groupName, condition, positions.stream().mapToInt(ix -> ix).toArray());
         }

         @NotNull
         @Generated
         public String getName() {
            return this.name;
         }

         @Nullable
         @Generated
         public String getCondition() {
            return this.condition;
         }

         @Generated
         public int[] getSlots() {
            return this.slots;
         }

         @Generated
         public GroupPattern(@NotNull String name, @Nullable String condition, int[] slots) {
            if (name == null) {
               throw new NullPointerException("name is marked non-null but is null");
            }

            this.name = name;
            this.condition = condition;
            this.slots = slots;
         }
      }
   }
}
