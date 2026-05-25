package me.neznamy.tab.shared.config.skin;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;

public class SignedTexture extends SkinSource {
   protected SignedTexture(@NotNull ConfigurationFile file) {
      super(file, "signed_textures");
   }

   @NotNull
   @Override
   public TabList.Skin download(@NotNull String textureBase64) {
      String[] parts = textureBase64.split(";");
      String base64 = parts[0];
      String signature = parts.length > 1 ? parts[1] : "";
      return new TabList.Skin(base64, signature);
   }
}
