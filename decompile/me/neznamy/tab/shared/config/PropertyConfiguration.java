package me.neznamy.tab.shared.config;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PropertyConfiguration {
   @NotNull
   List<String> VALID_PROPERTIES = Lists.newArrayList(new String[]{"tagprefix", "tagsuffix", "tabprefix", "customtabname", "tabsuffix"});

   void setProperty(@NonNull String var1, @NonNull String var2, @Nullable Server var3, @Nullable World var4, @Nullable String var5);

   @NotNull
   String[] getProperty(@NonNull String var1, @NonNull String var2, @Nullable Server var3, @Nullable World var4);

   void remove(@NonNull String var1);

   @NotNull
   Map<String, Object> getGlobalSettings(@NonNull String var1);

   @NotNull
   Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String var1);

   @NotNull
   Map<String, Map<String, Object>> getPerServerSettings(@NonNull String var1);

   @NotNull
   Set<String> getAllEntries();

   @NotNull
   default Map<String, Map<String, Object>> convertMap(@NonNull Map<String, Map<String, Map<String, Object>>> map, String groupOrUser) {
      if (map == null) {
         throw new NullPointerException("map is marked non-null but is null");
      }

      Map<String, Map<String, Object>> converted = new HashMap<>();

      for (Entry<String, Map<String, Map<String, Object>>> entry : map.entrySet()) {
         converted.put(entry.getKey(), entry.getValue().get(groupOrUser));
      }

      return converted;
   }

   @NotNull
   default String toString(@NonNull Object obj) {
      if (obj == null) {
         throw new NullPointerException("obj is marked non-null but is null");
      } else {
         return obj instanceof List ? ((List)obj).stream().map(Object::toString).collect(Collectors.joining("\n")) : obj.toString();
      }
   }

   @Nullable
   default Object fromString(@Nullable String string) {
      return string != null && string.contains("\n") ? Arrays.asList(string.split("\n")) : string;
   }

   default void checkProperty(
      @NonNull String source,
      @NonNull String type,
      @NonNull String name,
      @NonNull String property,
      @Nullable String server,
      @Nullable String world,
      boolean startupWarn
   ) {
      if (source == null) {
         throw new NullPointerException("source is marked non-null but is null");
      }

      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      }

      if (!VALID_PROPERTIES.contains(property.toLowerCase(Locale.US))) {
         StringBuilder msg = new StringBuilder(String.format("[%s] Unknown property \"%s\" defined for %s \"%s\"", source, property, type, name));
         if (world != null) {
            msg.append(" in world \"").append(world).append("\"");
         }

         if (server != null) {
            msg.append(" in server \"").append(server).append("\"");
         }

         msg.append(". Valid properties: ").append(VALID_PROPERTIES);
         if (startupWarn) {
            TAB.getInstance().getConfigHelper().startup().startupWarn(msg.toString());
         } else {
            TAB.getInstance().getConfigHelper().runtime().error(msg.toString());
         }
      }
   }
}
