package me.neznamy.tab.shared.placeholders;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;

public class PlaceholderReplacementPattern {
   public static final PlaceholderReplacementPattern EMPTY = new PlaceholderReplacementPattern("", Collections.emptyMap());
   private final Map<String, String> replacements = new HashMap<>();
   private final Map<float[], String> numberIntervals = new HashMap<>();
   private final Set<String> nestedPlaceholders = new HashSet<>();
   private final boolean empty;

   private PlaceholderReplacementPattern(@NotNull String identifier, @NotNull Map<Object, Object> map) {
      this.empty = map.isEmpty();

      for (Entry<Object, Object> entry : map.entrySet()) {
         String key = String.valueOf(entry.getKey());
         String value = String.valueOf(entry.getValue()).replace(identifier, "%value%");
         this.replacements.put(EnumChatFormat.color(key), EnumChatFormat.color(value));
         this.replacements.put(key, value);
         this.nestedPlaceholders.addAll(PlaceholderManagerImpl.detectPlaceholders(value));
         this.nestedPlaceholders.remove("%value%");
         if ("true".equals(key)) {
            this.replacements.put("yes", value);
            this.replacements.put("Yes", value);
         } else if ("false".equals(key)) {
            this.replacements.put("no", value);
            this.replacements.put("No", value);
         } else if (key.contains("-")) {
            try {
               this.numberIntervals.put(new float[]{Float.parseFloat(key.split("-")[0]), Float.parseFloat(key.split("-")[1])}, value);
            } catch (NumberFormatException var9) {
            }

            try {
               this.numberIntervals.put(new float[]{Float.parseFloat(key.split("~")[0]), Float.parseFloat(key.split("~")[1])}, value);
            } catch (NumberFormatException var8) {
            }
         }
      }
   }

   @NotNull
   public String findReplacement(@NotNull String output) {
      if (this.empty) {
         return output;
      }

      String replacement = this.findReplacement0(output);
      if (replacement.contains("%value%")) {
         replacement = replacement.replace("%value%", output);
      }

      return replacement;
   }

   @NotNull
   private String findReplacement0(@NotNull String output) {
      if (this.replacements.containsKey(output)) {
         return this.replacements.get(output);
      }

      if (!this.numberIntervals.isEmpty()) {
         try {
            String cleanValue = output.contains(",") ? output.replace(",", "") : output;
            float value = Float.parseFloat(cleanValue);

            for (Entry<float[], String> entry : this.numberIntervals.entrySet()) {
               if (entry.getKey()[0] <= value && value <= entry.getKey()[1]) {
                  return entry.getValue();
               }
            }
         } catch (NumberFormatException var6) {
         }
      }

      return this.replacements.containsKey("else") ? this.replacements.get("else") : output;
   }

   public static PlaceholderReplacementPattern create(@NotNull String identifier, @NotNull Map<Object, Object> map) {
      return map.isEmpty() ? EMPTY : new PlaceholderReplacementPattern(identifier, map);
   }

   @Generated
   public Set<String> getNestedPlaceholders() {
      return this.nestedPlaceholders;
   }
}
