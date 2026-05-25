package me.neznamy.tab.shared.config.mysql;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.sql.rowset.CachedRowSet;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySQLGroupConfiguration implements PropertyConfiguration {
   private final MySQL mysql;
   private final Map<String, Map<String, Object>> values = new HashMap<>();
   private final Map<String, Map<String, Map<String, Object>>> perWorld = new HashMap<>();
   private final Map<String, Map<String, Map<String, Object>>> perServer = new HashMap<>();

   public MySQLGroupConfiguration(@NonNull MySQL mysql) throws SQLException {
      if (mysql == null) {
         throw new NullPointerException("mysql is marked non-null but is null");
      }

      this.mysql = mysql;
      mysql.execute(
         "create table if not exists tab_groups (`group` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))"
      );
      CachedRowSet crs = mysql.getCRS("select * from tab_groups");
      if (crs.size() == 0) {
         TAB.getInstance()
            .getConfigHelper()
            .startup()
            .startupWarn(
               "[MySQL] Using MySQL to store groups and users, however, the database is empty. You can get started by uploading existing data in files using \"/"
                  + TAB.getInstance().getPlatform().getCommand()
                  + " mysql upload\". Further modifications can be done using property commands (/"
                  + TAB.getInstance().getPlatform().getCommand()
                  + " <group / player> <name> <property> <value...>)."
            );
      } else {
         while (crs.next()) {
            String group = crs.getString("group");
            if (!group.equals("_DEFAULT_")) {
               group = group.toLowerCase(Locale.US);
            }

            String property = crs.getString("property");
            String value = crs.getString("value");
            String world = crs.getString("world");
            String server = crs.getString("server");
            this.setProperty0(group, property, Server.byName(server), World.byName(world), value);
            this.checkProperty("MySQL", "group", group, property, server, world, true);
         }
      }
   }

   @Override
   public void setProperty(@NonNull String group, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
      try {
         if (group == null) {
            throw new NullPointerException("group is marked non-null but is null");
         }

         if (property == null) {
            throw new NullPointerException("property is marked non-null but is null");
         }

         String lowercaseGroup = group.equals("_DEFAULT_") ? group : group.toLowerCase(Locale.US);
         if (this.getProperty(lowercaseGroup, property, server, world) != null) {
            this.mysql
               .execute(
                  "delete from `tab_groups` where `group` = ? and `property` = ? and world "
                     + this.querySymbol(world == null)
                     + " ? and server "
                     + this.querySymbol(server == null)
                     + " ?",
                  lowercaseGroup,
                  property,
                  world == null ? null : world.getName(),
                  server == null ? null : server.getName()
               );
         }

         this.setProperty0(lowercaseGroup, property, server, world, value);
         if (value != null) {
            this.mysql
               .execute(
                  "insert into `tab_groups` (`group`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)",
                  lowercaseGroup,
                  property,
                  value,
                  world == null ? null : world.getName(),
                  server == null ? null : server.getName()
               );
         }
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   private String querySymbol(boolean isNull) {
      return isNull ? "is" : "=";
   }

   private void setProperty0(@NonNull String group, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
      if (group == null) {
         throw new NullPointerException("group is marked non-null but is null");
      }

      if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      }

      if (world != null) {
         this.perWorld.computeIfAbsent(world.getName(), w -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
      } else if (server != null) {
         this.perServer.computeIfAbsent(server.getName(), s -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
      } else {
         this.values.computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
      }
   }

   @Override
   public String[] getProperty(@NonNull String group, @NonNull String property, @Nullable Server server, @Nullable World world) {
      if (group == null) {
         throw new NullPointerException("group is marked non-null but is null");
      } else if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      } else {
         String lowercaseGroup = group.equals("_DEFAULT_") ? group : group.toLowerCase(Locale.US);
         String worldName = world == null ? null : world.getName();
         String serverName = server == null ? null : server.getName();
         Object value;
         if ((value = this.perWorld.getOrDefault(worldName, new HashMap<>()).getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("group=%s,world=%s", lowercaseGroup, worldName)};
         } else if ((value = this.perWorld.getOrDefault(worldName, new HashMap<>()).getOrDefault("_DEFAULT_", new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("group=%s,world=%s", "_DEFAULT_", worldName)};
         } else if ((value = this.perServer.getOrDefault(serverName, new HashMap<>()).getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("group=%s,server=%s", lowercaseGroup, serverName)};
         } else if ((value = this.perServer.getOrDefault(serverName, new HashMap<>()).getOrDefault("_DEFAULT_", new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("group=%s,server=%s", "_DEFAULT_", serverName)};
         } else if ((value = this.values.getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("group=%s", lowercaseGroup)};
         } else {
            return (value = this.values.getOrDefault("_DEFAULT_", new HashMap<>()).get(property)) != null
               ? new String[]{this.toString(value), String.format("group=%s", "_DEFAULT_")}
               : new String[0];
         }
      }
   }

   @Override
   public void remove(@NonNull String group) {
      if (group == null) {
         throw new NullPointerException("group is marked non-null but is null");
      }

      this.values.getOrDefault(group, new HashMap<>()).keySet().forEach(property -> this.setProperty(group, property, null, null, null));
      this.perWorld
         .forEach(
            (world, stringMapMap) -> stringMapMap.getOrDefault(group, new HashMap<>())
               .keySet()
               .forEach(property -> this.setProperty(group, property, null, World.byName(world), null))
         );
      this.perServer
         .forEach(
            (server, stringMapMap) -> stringMapMap.getOrDefault(group, new HashMap<>())
               .keySet()
               .forEach(property -> this.setProperty(group, property, Server.byName(server), null, null))
         );
   }

   @NotNull
   @Override
   public Map<String, Object> getGlobalSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.values.getOrDefault(name, Collections.emptyMap());
      }
   }

   @NotNull
   @Override
   public Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.convertMap(this.perWorld, name);
      }
   }

   @NotNull
   @Override
   public Map<String, Map<String, Object>> getPerServerSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         return this.convertMap(this.perServer, name);
      }
   }

   @NotNull
   @Override
   public Set<String> getAllEntries() {
      Set<String> set = new HashSet<>(this.values.keySet());
      this.perWorld.values().forEach(map -> set.addAll(map.keySet()));
      this.perServer.values().forEach(map -> set.addAll(map.keySet()));
      return set;
   }
}
