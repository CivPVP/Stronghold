package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigurationFile {
   @NotNull
   protected Map<Object, Object> values;
   @NotNull
   protected final File file;

   protected ConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws IOException {
      if (destination == null) {
         throw new NullPointerException("destination is marked non-null but is null");
      }

      this.file = destination;
      if (this.file.getParentFile() != null && !this.file.getParentFile().exists()) {
         Files.createDirectories(this.file.getParentFile().toPath());
      }

      if (!this.file.exists()) {
         if (source == null) {
            throw new IllegalStateException("File does not exist and source is null");
         }

         Files.copy(source, this.file.toPath());
      }
   }

   public abstract void save();

   @Nullable
   public Object getObject(@NonNull String path, @Nullable Object defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      if (path.isEmpty()) {
         return this.values;
      }

      Object value = this.values;

      for (String section : path.contains(".") ? path.split("\\.") : new String[]{path}) {
         if (!(value instanceof Map)) {
            if (defaultValue != null) {
               this.set(path, defaultValue);
            }

            return defaultValue;
         }

         value = this.getIgnoreCase((Map<Object, Object>)value, section);
      }

      if (value == null && defaultValue != null) {
         TAB.getInstance().debug("Inserting missing config option \"" + path + "\" with value \"" + defaultValue + "\" into " + this.file.getName());
         this.set(path, defaultValue);
         return defaultValue;
      } else {
         return value;
      }
   }

   @Nullable
   public Object getObject(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getObject(path, null);
      }
   }

   @Nullable
   public Object getObject(@NonNull String[] path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      Object value = this.values;

      for (String section : path) {
         if (!(value instanceof Map)) {
            return null;
         }

         value = this.getIgnoreCase((Map<Object, Object>)value, section);
      }

      return value;
   }

   @Nullable
   private Object getIgnoreCase(@NonNull Map<Object, Object> map, @NonNull String key) {
      if (map == null) {
         throw new NullPointerException("map is marked non-null but is null");
      }

      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      try {
         for (Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(key)) {
               return entry.getValue();
            }
         }

         return map.get(key);
      } catch (ConcurrentModificationException e) {
         return this.getIgnoreCase(map, key);
      }
   }

   @Contract("_, !null -> !null")
   public String getString(@NonNull String path, @Nullable String defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      Object value = this.getObject(path, defaultValue);
      return value == null ? defaultValue : String.valueOf(value);
   }

   @Contract("_, !null -> !null")
   public List<String> getStringList(@NonNull String path, @Nullable List<String> defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      Object value = this.getObject(path, defaultValue);
      if (value == null) {
         return defaultValue;
      }

      if (!(value instanceof List)) {
         return Collections.emptyList();
      }

      List<String> fixedList = new ArrayList<>();

      for (Object key : (List)value) {
         fixedList.add(String.valueOf(key));
      }

      return fixedList;
   }

   @Contract("_, !null -> !null")
   public Integer getInt(@NonNull String path, @Nullable Integer defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      Object value = this.getObject(path, defaultValue);
      if (value == null) {
         return defaultValue;
      }

      try {
         return Integer.parseInt(value.toString());
      } catch (NumberFormatException e) {
         return defaultValue;
      }
   }

   public boolean getBoolean(@NonNull String path, boolean defaultValue) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      Object value = this.getObject(path, defaultValue);
      return value == null ? defaultValue : Boolean.parseBoolean(value.toString());
   }

   @NotNull
   public <K, V> Map<K, V> getMap(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      if (path.isEmpty()) {
         return (Map<K, V>)this.values;
      }

      Object value = this.getObject(path, null);
      return value instanceof Map ? (Map)value : new LinkedHashMap<>();
   }

   public boolean hasConfigOption(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return this.getObject(path) != null;
      }
   }

   public void set(@NonNull String path, @Nullable Object value) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      this.set(this.values, path, value);
      this.save();
   }

   @NotNull
   private Map<Object, Object> set(@NonNull Map<Object, Object> map, @NonNull String path, @Nullable Object value) {
      if (map == null) {
         throw new NullPointerException("map is marked non-null but is null");
      }

      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      }

      if (path.contains(".")) {
         String keyWord = this.getRealKey(map, path.split("\\.")[0]);
         Object subMap = map.get(keyWord);
         if (!(subMap instanceof Map)) {
            subMap = new LinkedHashMap();
         }

         map.put(keyWord, this.set((Map<Object, Object>)subMap, path.substring(keyWord.length() + 1), value));
      } else if (value == null) {
         map.remove(this.getRealKey(map, path));
      } else {
         map.put(path, value);
      }

      return map;
   }

   @NotNull
   private String getRealKey(@NonNull Map<?, ?> map, @NonNull String key) {
      if (map == null) {
         throw new NullPointerException("map is marked non-null but is null");
      }

      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      for (Object mapKey : map.keySet()) {
         if (mapKey.toString().equalsIgnoreCase(key)) {
            return mapKey.toString();
         }
      }

      return key;
   }

   public boolean setIfMissing(@NonNull String key, @NonNull Object value) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      } else if (value == null) {
         throw new NullPointerException("value is marked non-null but is null");
      } else if (!this.hasConfigOption(key)) {
         this.set(key, value);
         return true;
      } else {
         return false;
      }
   }

   public boolean removeOption(@NonNull String key) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      } else if (this.hasConfigOption(key)) {
         this.set(key, null);
         return true;
      } else {
         return false;
      }
   }

   public boolean rename(@NonNull String oldPath, @NonNull String newPath) {
      if (oldPath == null) {
         throw new NullPointerException("oldPath is marked non-null but is null");
      } else if (newPath == null) {
         throw new NullPointerException("newPath is marked non-null but is null");
      } else if (this.hasConfigOption(oldPath)) {
         this.set(newPath, this.getObject(oldPath));
         this.set(oldPath, null);
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   public ConfigurationSection getConfigurationSection(@NonNull String path) {
      if (path == null) {
         throw new NullPointerException("path is marked non-null but is null");
      } else {
         return new ConfigurationSection(this.file.getName(), path, this.getMap(path));
      }
   }

   @NotNull
   @Generated
   public Map<Object, Object> getValues() {
      return this.values;
   }

   @NotNull
   @Generated
   public File getFile() {
      return this.file;
   }

   @Generated
   public void setValues(@NotNull Map<Object, Object> values) {
      if (values == null) {
         throw new NullPointerException("values is marked non-null but is null");
      }

      this.values = values;
   }
}
