package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YamlPropertyConfigurationFile extends YamlConfigurationFile implements PropertyConfiguration {
   private final String PER_SERVER = "per-server";
   private final String PER_WORLD = "per-world";
   private final String category;
   private final Collection<String> worldGroups = new ArrayList<>(this.getMap("per-world").keySet());
   private final Collection<String> serverGroups = new ArrayList<>(this.getMap("per-server").keySet());

   public YamlPropertyConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws IOException {
      super(source, destination);
      if (destination == null) {
         throw new NullPointerException("destination is marked non-null but is null");
      }

      this.category = destination.getName().contains("groups") ? "group" : "user";

      for (Entry<Object, Object> entry : this.getValues().entrySet()) {
         if (entry.getKey().equals("per-server")) {
            for (String server : this.serverGroups) {
               for (String name : this.getMap("per-server." + server).keySet()) {
                  for (String property : this.getMap("per-server." + server + "." + name).keySet()) {
                     this.checkProperty(destination.getName(), this.category, name, property, server, null, true);
                  }
               }
            }
         } else if (!entry.getKey().equals("per-world")) {
            for (String property : this.getMap(entry.getKey().toString()).keySet()) {
               this.checkProperty(destination.getName(), this.category, entry.getKey().toString(), property, null, null, true);
            }
         } else {
            for (String world : this.worldGroups) {
               for (String name : this.getMap("per-world." + world).keySet()) {
                  for (String property : this.getMap("per-world." + world + "." + name).keySet()) {
                     this.checkProperty(destination.getName(), this.category, name, property, null, world, true);
                  }
               }
            }
         }
      }
   }

   @Override
   public void setProperty(@NonNull String name, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      }

      if (world != null) {
         this.set(String.format("%s.%s.%s.%s", "per-world", world.getName(), name, property), this.fromString(value));
      } else if (server != null) {
         this.set(String.format("%s.%s.%s.%s", "per-server", server.getName(), name, property), this.fromString(value));
      } else {
         this.set(String.format("%s.%s", name, property), this.fromString(value));
      }
   }

   @Override
   public String[] getProperty(@NonNull String name, @NonNull String property, @Nullable Server server, @Nullable World world) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      } else {
         String worldName = world == null ? null : world.getName();
         String serverName = server == null ? null : server.getName();
         Object value;
         if ((value = this.getObject(new String[]{"per-world", TAB.getInstance().getConfiguration().getGroup(this.worldGroups, worldName), name, property}))
            != null) {
            return new String[]{this.toString(value), this.category + "=" + name + ", world=" + worldName};
         } else if ((
               value = this.getObject(
                  new String[]{"per-world", TAB.getInstance().getConfiguration().getGroup(this.worldGroups, worldName), "_DEFAULT_", property}
               )
            )
            != null) {
            return new String[]{this.toString(value), this.category + "=" + "_DEFAULT_" + ", world=" + worldName};
         } else if ((
               value = this.getObject(
                  new String[]{"per-server", TAB.getInstance().getConfiguration().getServerGroup(this.serverGroups, server), name, property}
               )
            )
            != null) {
            return new String[]{this.toString(value), this.category + "=" + name + ", server=" + serverName};
         } else if ((
               value = this.getObject(
                  new String[]{"per-server", TAB.getInstance().getConfiguration().getServerGroup(this.serverGroups, server), "_DEFAULT_", property}
               )
            )
            != null) {
            return new String[]{this.toString(value), this.category + "=" + "_DEFAULT_" + ", server=" + serverName};
         } else if ((value = this.getObject(new String[]{name, property})) != null) {
            return new String[]{this.toString(value), this.category + "=" + name};
         } else {
            return (value = this.getObject(new String[]{"_DEFAULT_", property})) != null
               ? new String[]{this.toString(value), this.category + "=" + "_DEFAULT_"}
               : new String[0];
         }
      }
   }

   @Override
   public void remove(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.set(name, null);
      this.getMap("per-world").keySet().forEach(world -> this.set("per-world." + world + "." + name, null));
      this.getMap("per-server").keySet().forEach(server -> this.set("per-server." + server + "." + name, null));
   }

   @NotNull
   @Override
   public Map<String, Object> getGlobalSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.getMap(name);
      }
   }

   @NotNull
   @Override
   public Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.convertMap(this.getMap("per-world"), name);
      }
   }

   @NotNull
   @Override
   public Map<String, Map<String, Object>> getPerServerSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.convertMap(this.getMap("per-server"), name);
      }
   }

   @NotNull
   @Override
   public Set<String> getAllEntries() {
      Set<Object> set = new HashSet<>(this.values.keySet());
      set.remove("per-world");
      set.remove("per-server");
      Map<String, Map<String, Map<String, String>>> perWorld = this.getMap("per-world");
      perWorld.values().forEach(m -> set.addAll(m.keySet()));
      Map<String, Map<String, Map<String, String>>> perServer = this.getMap("per-server");
      perServer.values().forEach(m -> set.addAll(m.keySet()));
      return set.stream().map(Object::toString).collect(Collectors.toSet());
   }
}
