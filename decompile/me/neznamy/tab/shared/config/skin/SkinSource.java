package me.neznamy.tab.shared.config.skin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.org.json.simple.JSONObject;
import me.neznamy.tab.libs.org.json.simple.parser.JSONParser;
import me.neznamy.tab.libs.org.json.simple.parser.ParseException;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SkinSource {
   @NotNull
   private final ConfigurationFile file;
   @NotNull
   private final String path;
   @NotNull
   private final Map<String, List<String>> cache;
   @NotNull
   private final Map<String, TabList.Skin> skins = new HashMap<>();

   protected SkinSource(@NotNull ConfigurationFile file, @NotNull String path) {
      this.file = file;
      this.path = path;
      this.cache = file.getMap(path);

      for (Entry<String, List<String>> entry : this.cache.entrySet()) {
         this.skins.put(entry.getKey(), new TabList.Skin(entry.getValue().get(0), entry.getValue().get(1)));
      }
   }

   @Nullable
   public TabList.Skin getSkin(@NotNull String skin) {
      if (this.skins.containsKey(skin)) {
         return this.skins.get(skin);
      }

      TabList.Skin downloaded = this.download(skin);
      if (downloaded != null) {
         this.skins.put(skin, downloaded);
         this.cache.put(skin, Arrays.asList(downloaded.getValue(), downloaded.getSignature()));
         this.file.set(this.path, this.cache);
      }

      return downloaded;
   }

   @Nullable
   public abstract TabList.Skin download(@NotNull String var1);

   @NotNull
   protected JSONObject getResponse(@NotNull String url) throws IOException, ParseException {
      InputStreamReader reader = new InputStreamReader(new URL(url).openStream());

      JSONObject var3;
      try {
         var3 = (JSONObject)new JSONParser().parse(reader);
      } catch (Throwable var6) {
         try {
            reader.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      reader.close();
      return var3;
   }
}
