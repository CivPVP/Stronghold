package me.neznamy.tab.shared.features.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.shared.config.skin.SkinManager;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LayoutSkinManager {
   @NotNull
   private final SkinManager skinManager;
   @Nullable
   private final TabList.Skin defaultSkin;
   @NotNull
   private final Map<Integer, TabList.Skin> defaultSkinHashMap = new HashMap<>();

   public LayoutSkinManager(@NotNull SkinManager skinManager, @NotNull String defaultSkin, @NotNull Map<Integer, String> defaultSkinHashMap) {
      this.skinManager = skinManager;
      this.defaultSkin = this.getSkin(defaultSkin);

      for (Entry<Integer, String> entry : defaultSkinHashMap.entrySet()) {
         TabList.Skin skin = this.getSkin(entry.getValue());
         if (skin != null) {
            this.defaultSkinHashMap.put(entry.getKey(), skin);
         }
      }
   }

   @Nullable
   public TabList.Skin getDefaultSkin(int slot) {
      return this.defaultSkinHashMap.getOrDefault(slot, this.defaultSkin);
   }

   @Nullable
   public TabList.Skin getSkin(@NotNull String skin) {
      TabList.Skin skinObj = this.skinManager.getSkin(skin);
      return skinObj != null ? skinObj : this.defaultSkin;
   }
}
