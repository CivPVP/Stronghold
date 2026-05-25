package me.neznamy.tab.shared.features.layout;

import java.util.UUID;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

public class FixedSlot extends RefreshableFeature {
   @NotNull
   private static final StringToComponentCache cache = new StringToComponentCache("LayoutFixedSlot", 1000);
   @NonNull
   private final LayoutManagerImpl manager;
   private final int slot;
   @NonNull
   private final LayoutPattern pattern;
   @NonNull
   private final UUID id;
   @NonNull
   private final String text;
   @NonNull
   private final String skin;
   private final int ping;

   @NotNull
   @Override
   public String getFeatureName() {
      return this.manager.getFeatureName();
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating fixed slots";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      if (p.layoutData.currentLayout != null
         && p.layoutData.currentLayout.view.getPattern() == this.pattern
         && p.getVersion().getMinorVersion() >= 8
         && !p.isBedrockPlayer()) {
         if (p.layoutData.currentLayout.fixedSlotSkins.get(this).update()) {
            p.getTabList().removeEntry(this.id);
            p.getTabList().addEntry(this.createEntry(p));
         } else {
            p.getTabList().updateDisplayName(this.id, cache.get(p.layoutData.currentLayout.fixedSlotTexts.get(this).updateAndGet()));
         }
      }
   }

   @NotNull
   public TabList.Entry createEntry(@NotNull TabPlayer viewer) {
      viewer.layoutData.currentLayout.fixedSlotTexts.put(this, new Property(this, viewer, this.text));
      viewer.layoutData.currentLayout.fixedSlotSkins.put(this, new Property(this, viewer, this.skin));
      return new TabList.Entry(
         this.id,
         this.manager.getConfiguration().getDirection().getEntryName(viewer, this.slot, LayoutManagerImpl.isTeamsEnabled()),
         this.manager.getSkinManager().getSkin(viewer.layoutData.currentLayout.fixedSlotSkins.get(this).updateAndGet()),
         true,
         this.ping,
         0,
         cache.get(viewer.layoutData.currentLayout.fixedSlotTexts.get(this).updateAndGet()),
         Integer.MAX_VALUE - this.manager.getConfiguration().getDirection().translateSlot(this.slot),
         true
      );
   }

   @NotNull
   public static FixedSlot fromDefinition(
      @NotNull LayoutConfiguration.LayoutDefinition.FixedSlotDefinition def, @NotNull LayoutPattern pattern, @NotNull LayoutManagerImpl manager
   ) {
      String skin;
      if (def.getSkin() != null && !def.getSkin().isEmpty()) {
         skin = def.getSkin();
      } else if (pattern.getDefaultSkinDefinition() != null) {
         skin = pattern.getDefaultSkinDefinition();
      } else {
         skin = manager.getConfiguration().getDefaultSkin(def.getSlot());
      }

      FixedSlot f = new FixedSlot(
         manager,
         def.getSlot(),
         pattern,
         manager.getUUID(def.getSlot()),
         def.getText(),
         skin,
         def.getPing() == null ? manager.getConfiguration().getEmptySlotPing() : def.getPing()
      );
      if (!def.getText().isEmpty()) {
         TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(pattern.getName(), def.getSlot()), f);
      }

      return f;
   }

   @Generated
   public FixedSlot(
      @NonNull LayoutManagerImpl manager, int slot, @NonNull LayoutPattern pattern, @NonNull UUID id, @NonNull String text, @NonNull String skin, int ping
   ) {
      if (manager == null) {
         throw new NullPointerException("manager is marked non-null but is null");
      }

      if (pattern == null) {
         throw new NullPointerException("pattern is marked non-null but is null");
      }

      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (skin == null) {
         throw new NullPointerException("skin is marked non-null but is null");
      }

      this.manager = manager;
      this.slot = slot;
      this.pattern = pattern;
      this.id = id;
      this.text = text;
      this.skin = skin;
      this.ping = ping;
   }

   @Generated
   public int getSlot() {
      return this.slot;
   }
}
