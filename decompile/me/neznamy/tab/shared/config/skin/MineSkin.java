package me.neznamy.tab.shared.config.skin;

import java.io.IOException;
import me.neznamy.tab.libs.org.json.simple.JSONObject;
import me.neznamy.tab.libs.org.json.simple.parser.ParseException;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MineSkin extends SkinSource {
   protected MineSkin(@NotNull ConfigurationFile file) {
      super(file, "mineskin");
   }

   @Nullable
   @Override
   public TabList.Skin download(@NotNull String input) {
      try {
         long time = System.currentTimeMillis();
         JSONObject json = this.getResponse("https://api.mineskin.org/get/uuid/" + input);
         JSONObject data = (JSONObject)json.get("data");
         JSONObject texture = (JSONObject)data.get("texture");
         String value = (String)texture.get("value");
         String signature = (String)texture.get("signature");
         TAB.getInstance().debug("Downloaded MINESKIN skin " + input + " in " + (System.currentTimeMillis() - time) + "ms");
         return new TabList.Skin(value, signature);
      } catch (IOException | ParseException e) {
         TAB.getInstance().getErrorManager().mineSkinDownloadError(input, e);
         return null;
      }
   }
}
