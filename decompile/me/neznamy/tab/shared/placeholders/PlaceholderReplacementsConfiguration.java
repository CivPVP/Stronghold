package me.neznamy.tab.shared.placeholders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PlaceholderReplacementsConfiguration {
   @NotNull
   private final Map<String, Map<Object, Object>> values;

   @NotNull
   public static PlaceholderReplacementsConfiguration fromSection(@NotNull ConfigurationSection section) {
      Map<String, Map<Object, Object>> values = new HashMap<>();

      for (Object key : section.getKeys()) {
         String identifier = key.toString();
         Map<Object, Object> map = section.getMap(identifier, Collections.emptyMap());
         if (identifier.startsWith("%") && identifier.endsWith("%")) {
            for (Entry<?, ?> pattern : map.entrySet()) {
               if (pattern.getKey().equals("else") && pattern.getValue().equals(identifier)) {
                  section.hint(
                     String.format(
                        "Placeholder %s has configured \"else -> %s\" replacement pattern, but this is already the default behavior and therefore this pattern can be removed.",
                        identifier,
                        identifier
                     )
                  );
               }
            }

            values.put(identifier, map);
         } else {
            section.startupWarn(
               "Placeholder output replacements have a section for \""
                  + identifier
                  + "\", which is not a valid placeholder pattern (placeholders must start and end with %)"
            );
         }
      }

      return new PlaceholderReplacementsConfiguration(values);
   }

   @NotNull
   @Generated
   public Map<String, Map<Object, Object>> getValues() {
      return this.values;
   }

   @Generated
   public PlaceholderReplacementsConfiguration(@NotNull Map<String, Map<Object, Object>> values) {
      if (values == null) {
         throw new NullPointerException("values is marked non-null but is null");
      }

      this.values = values;
   }
}
