package me.neznamy.tab.shared.config.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkinManager {
   private final List<String> invalidSkins = new ArrayList<>();
   private final Map<String, SkinSource> sources = new HashMap<>();

   public SkinManager(@NotNull ConfigurationFile skinCache) {
      this.sources.put("player", new PlayerSkin(skinCache));
      this.sources.put("mineskin", new MineSkin(skinCache));
      this.sources.put("texture", new Texture(skinCache));
      this.sources.put("signed_texture", new SignedTexture(skinCache));
   }

   @Nullable
   public TabList.Skin getSkin(@NotNull String skin) {
      if (this.invalidSkins.contains(skin)) {
         return null;
      }

      for (Entry<String, SkinSource> entry : this.sources.entrySet()) {
         if (skin.startsWith(entry.getKey() + ":")) {
            TabList.Skin value = entry.getValue().getSkin(skin.substring(entry.getKey().length() + 1));
            if (value == null) {
               this.invalidSkins.add(skin);
               return null;
            }

            return value;
         }
      }

      TAB.getInstance().getConfigHelper().startup().invalidSkinDefinition(skin);
      this.invalidSkins.add(skin);
      return null;
   }
}
