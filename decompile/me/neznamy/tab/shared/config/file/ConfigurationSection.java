package me.neznamy.tab.shared.config.file;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigurationSection {
   @NotNull
   private final String file;
   @NotNull
   private final String section;
   @NotNull
   private final Map<Object, Object> map;

   public void checkForUnknownKey(@NonNull List<String> validProperties) {
      if (validProperties == null) {
         throw new NullPointerException("validProperties is marked non-null but is null");
      }

      for (Object mapKey : this.map.keySet()) {
         if (!validProperties.contains(mapKey.toString().toLowerCase(Locale.US))) {
            this.startupWarn(String.format("Configuration section \"%s\" has unknown key \"%s\". Valid keys: %s", this.section, mapKey, validProperties));
         }
      }
   }

   public void startupWarn(@NonNull String message) {
      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      TAB.getInstance().getConfigHelper().startup().startupWarn("[" + this.file + "] " + this.section + ": " + message);
   }

   public void hint(@NonNull String message) {
      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      TAB.getInstance().getConfigHelper().hint(this.file, message);
   }

   @Nullable
   public Boolean getBoolean(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getNullable(path, Boolean.class);
      }
   }

   public boolean getBoolean(@NonNull String path, boolean defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getRequired(path, defaultValue, Boolean.class);
      }
   }

   @Nullable
   public Integer getInt(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getNullable(path, Integer.class);
      }
   }

   public int getInt(@NonNull String path, int defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getRequired(path, defaultValue, Integer.class);
      }
   }

   @Nullable
   public Number getNumber(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getNullable(path, Number.class);
      }
   }

   @NotNull
   public Number getNumber(@NonNull String path, @NonNull Number defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      } else {
         return this.getRequired(path, defaultValue, Number.class);
      }
   }

   @Nullable
   public String getString(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.fixString(this.getNullable(path, String.class));
      }
   }

   @NotNull
   public String getString(@NonNull String path, @NonNull String defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      } else {
         return this.fixString(this.getRequired(path, defaultValue, String.class));
      }
   }

   @Nullable
   public List<String> getStringList(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      List<Object> list = this.getNullable(path, List.class);
      return list == null ? null : list.stream().map(o -> this.fixString(o.toString())).collect(Collectors.toList());
   }

   @NotNull
   public List<String> getStringList(@NonNull String path, @NonNull List<String> defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      }

      List<Object> list = this.getRequired(path, defaultValue, List.class);
      return list.stream().map(o -> this.fixString(o.toString())).collect(Collectors.toList());
   }

   @Nullable
   public <K, V> Map<K, V> getMap(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getNullable(path, Map.class);
      }
   }

   @NotNull
   public <K, V> Map<K, V> getMap(@NonNull String path, @NonNull Map<?, ?> defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      } else {
         return this.getRequired(path, (Map<K, V>)defaultValue, Map.class);
      }
   }

   @Nullable
   public Object getObject(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getNullable(path, Object.class);
      }
   }

   @NotNull
   public Object getObject(@NonNull String path, @NonNull Object defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      } else {
         return this.getRequired(path, defaultValue, Object.class);
      }
   }

   @Nullable
   private <T> T getNullable(@NonNull String path, @NonNull Class<T> clazz) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (clazz == null) {
         throw new NullPointerException("clazz is marked non-null but is null");
      } else {
         return this.evaluateNullable(this.get(path), path, clazz);
      }
   }

   @Nullable
   private <T> T evaluateNullable(@Nullable Object value, @NonNull String path, @NonNull Class<T> clazz) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (clazz == null) {
         throw new NullPointerException("clazz is marked non-null but is null");
      } else if (value != null && !clazz.isInstance(value)) {
         this.startupWarn(
            "Configuration section \""
               + this.section
               + "."
               + path
               + "\" is expected to be of type "
               + clazz.getSimpleName()
               + ", but was "
               + value.getClass().getSimpleName()
         );
         return null;
      } else {
         return (T)value;
      }
   }

   @NotNull
   private <T> T getRequired(@NonNull String path, @NonNull T defaultValue, @NonNull Class<T> clazz) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      } else if (clazz == null) {
         throw new NullPointerException("clazz is marked non-null but is null");
      } else {
         return this.evaluateRequired(this.get(path), path, defaultValue, clazz);
      }
   }

   @NotNull
   private <T> T evaluateRequired(@Nullable Object value, @NonNull String path, @NonNull T defaultValue, @NonNull Class<T> clazz) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else if (defaultValue == null) {
         throw new NullPointerException("defaultValue is marked non-null but is null");
      } else if (clazz == null) {
         throw new NullPointerException("clazz is marked non-null but is null");
      } else if (value == null) {
         this.startupWarn(
            "Missing configuration section \""
               + this.section
               + "."
               + path
               + "\" of type "
               + clazz.getSimpleName()
               + ", using default value "
               + defaultValue
               + "."
         );
         return defaultValue;
      } else if (!clazz.isInstance(value)) {
         this.startupWarn(
            "Configuration section \""
               + this.section
               + "."
               + path
               + "\" is expected to be of type "
               + clazz.getSimpleName()
               + ", but was "
               + value.getClass().getSimpleName()
               + " ("
               + value
               + "). Using default value \""
               + defaultValue
               + "\"."
         );
         return defaultValue;
      } else {
         return (T)value;
      }
   }

   @Nullable
   private Object get(@NonNull String key) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      for (Entry<Object, Object> entry : this.map.entrySet()) {
         if (key.equalsIgnoreCase(entry.getKey().toString())) {
            return entry.getValue();
         }
      }

      return null;
   }

   @NotNull
   public Collection<Object> getKeys() {
      return this.map.keySet();
   }

   @NotNull
   public ConfigurationSection getConfigurationSection(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      Map<Object, Object> map = this.getMap(path);
      if (map == null) {
         map = Collections.emptyMap();
      }

      return new ConfigurationSection(this.file, this.section + "." + path, map);
   }

   @Contract("null -> null; !null -> !null")
   private String fixString(@Nullable String string) {
      return string == null ? null : string.replace("\\n", "\n");
   }

   @Generated
   public ConfigurationSection(@NotNull String file, @NotNull String section, @NotNull Map<Object, Object> map) {
      if (file == null) {
         throw new NullPointerException("file is marked non-null but is null");
      }

      if (section == null) {
         throw new NullPointerException("section is marked non-null but is null");
      }

      if (map == null) {
         throw new NullPointerException("map is marked non-null but is null");
      }

      this.file = file;
      this.section = section;
      this.map = map;
   }
}
