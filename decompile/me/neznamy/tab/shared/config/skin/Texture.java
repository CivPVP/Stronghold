package me.neznamy.tab.shared.config.skin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import me.neznamy.tab.libs.org.json.simple.JSONObject;
import me.neznamy.tab.libs.org.json.simple.parser.JSONParser;
import me.neznamy.tab.libs.org.json.simple.parser.ParseException;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Texture extends SkinSource {
   protected Texture(@NotNull ConfigurationFile file) {
      super(file, "textures");
   }

   @Nullable
   @Override
   public TabList.Skin download(@NotNull String texture) {
      try {
         long time = System.currentTimeMillis();
         InputStreamReader reader = getInputStreamReader(texture);
         JSONObject json = (JSONObject)new JSONParser().parse(reader);
         JSONObject data = (JSONObject)json.get("data");
         JSONObject texture2 = (JSONObject)data.get("texture");
         String value = (String)texture2.get("value");
         String signature = (String)texture2.get("signature");
         TAB.getInstance().debug("Downloaded TEXTURE skin " + texture + " in " + (System.currentTimeMillis() - time) + "ms");
         return new TabList.Skin(value, signature);
      } catch (IOException | ParseException e) {
         TAB.getInstance().getErrorManager().textureSkinDownloadError(texture, e);
         return null;
      }
   }

   @NotNull
   private static InputStreamReader getInputStreamReader(@NotNull String texture) throws IOException {
      URL url = new URL("https://api.mineskin.org/generate/url/");
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setRequestProperty("User-Agent", "ExampleApp/v1.0");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      String jsonInputString = "{\"variant\":\"classic\",\"name\":\"string\",\"visibility\":0,\"url\":\"https://textures.minecraft.net/texture/"
         + texture
         + "\"}";
      OutputStream os = con.getOutputStream();

      try {
         byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
         os.write(input, 0, input.length);
      } catch (Throwable var8) {
         if (os != null) {
            try {
               os.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (os != null) {
         os.close();
      }

      return new InputStreamReader(con.getInputStream());
   }
}
