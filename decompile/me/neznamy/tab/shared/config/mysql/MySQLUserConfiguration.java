package me.neznamy.tab.shared.config.mysql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.sql.rowset.CachedRowSet;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySQLUserConfiguration implements PropertyConfiguration {
   private final MySQL mysql;
   private final WeakHashMap<TabPlayer, Map<String, Object>> values = new WeakHashMap<>();
   private final Map<World, WeakHashMap<TabPlayer, Map<String, Object>>> perWorld = new HashMap<>();
   private final Map<Server, WeakHashMap<TabPlayer, Map<String, Object>>> perServer = new HashMap<>();

   public MySQLUserConfiguration(@NonNull MySQL mysql) throws SQLException {
      if (mysql == null) {
         throw new NullPointerException("mysql is marked non-null but is null");
      }

      this.mysql = mysql;
      mysql.execute(
         "create table if not exists tab_users (`user` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))"
      );
   }

   @Override
   public void setProperty(@NonNull String user, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
      try {
         if (user == null) {
            throw new NullPointerException("user is marked non-null but is null");
         }

         if (property == null) {
            throw new NullPointerException("property is marked non-null but is null");
         }

         TabPlayer p = this.getPlayer(user);
         String lowercaseUser = user.toLowerCase();
         if (this.getProperty(lowercaseUser, property, server, world) != null) {
            this.mysql
               .execute(
                  "delete from `tab_users` where `user` = ? and `property` = ? and world "
                     + this.querySymbol(world == null)
                     + " ? and server "
                     + this.querySymbol(server == null)
                     + " ?",
                  lowercaseUser,
                  property,
                  world == null ? null : world.getName(),
                  server == null ? null : server.getName()
               );
         }

         if (p != null) {
            this.setProperty0(p, property, server, world, value);
         }

         if (value != null) {
            this.mysql
               .execute(
                  "insert into `tab_users` (`user`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)",
                  lowercaseUser,
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

   private void setProperty0(@NonNull TabPlayer user, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
      if (user == null) {
         throw new NullPointerException("user is marked non-null but is null");
      }

      if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      }

      if (world != null) {
         this.perWorld.computeIfAbsent(world, w -> new WeakHashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
      } else if (server != null) {
         this.perServer.computeIfAbsent(server, s -> new WeakHashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
      } else {
         this.values.computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
      }
   }

   @Override
   public String[] getProperty(@NonNull String user, @NonNull String property, @Nullable Server server, @Nullable World world) {
      if (user == null) {
         throw new NullPointerException("user is marked non-null but is null");
      } else if (property == null) {
         throw new NullPointerException("property is marked non-null but is null");
      } else {
         TabPlayer p = this.getPlayer(user);
         Object value;
         if ((value = this.perWorld.getOrDefault(world, new WeakHashMap<>()).getOrDefault(p, new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("user=%s,world=%s", user, world.getName())};
         } else if ((value = this.perServer.getOrDefault(server, new WeakHashMap<>()).getOrDefault(p, new HashMap<>()).get(property)) != null) {
            return new String[]{this.toString(value), String.format("user=%s,server=%s", user, server.getName())};
         } else {
            return (value = this.values.getOrDefault(p, new HashMap<>()).get(property)) != null
               ? new String[]{this.toString(value), String.format("user=%s", user)}
               : new String[0];
         }
      }
   }

   @Override
   public void remove(@NonNull String player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      try {
         this.mysql.execute("delete from `tab_users` where `user` = ?", player);
      } catch (SQLException e) {
         TAB.getInstance().getErrorManager().mysqlQueryFailed(e);
      }

      TabPlayer user = this.getPlayer(player);
      if (user != null) {
         this.values.remove(user);

         for (WeakHashMap<TabPlayer, Map<String, Object>> worldValues : this.perWorld.values()) {
            worldValues.remove(user);
         }

         for (WeakHashMap<TabPlayer, Map<String, Object>> serverValues : this.perServer.values()) {
            serverValues.remove(user);
         }
      }
   }

   @NotNull
   @Override
   public Map<String, Object> getGlobalSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         throw new UnsupportedOperationException("Not supported for users");
      }
   }

   @NotNull
   @Override
   public Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         throw new UnsupportedOperationException("Not supported for users");
      }
   }

   @NotNull
   @Override
   public Map<String, Map<String, Object>> getPerServerSettings(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         throw new UnsupportedOperationException("Not supported for users");
      }
   }

   @NotNull
   @Override
   public Set<String> getAllEntries() {
      throw new UnsupportedOperationException("Not supported for users");
   }

   @Nullable
   private TabPlayer getPlayer(@NonNull String string) {
      if (string == null) {
         throw new NullPointerException("string is marked non-null but is null");
      }

      TabPlayer p = TAB.getInstance().getPlayer(string);
      if (p == null) {
         try {
            p = TAB.getInstance().getPlayer(UUID.fromString(string));
         } catch (IllegalArgumentException var4) {
         }
      }

      return p;
   }

   public void load(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      TAB.getInstance().getCPUManager().getMysqlThread().execute(() -> {
         try {
            CachedRowSet crs = this.mysql.getCRS("select * from `tab_users` where `user` = ?", player.getName().toLowerCase());

            while (crs.next()) {
               String user = crs.getString("user");
               String property = crs.getString("property");
               String value = crs.getString("value");
               String world = crs.getString("world");
               String server = crs.getString("server");
               TAB.getInstance().debug("Loaded user line: " + String.format("%s, %s, %s, %s, %s", user, property, value, world, server));
               this.checkProperty("MySQL", "player", user, property, server, world, false);
               this.setProperty0(player, property, Server.byName(server), World.byName(world), value);
            }

            CachedRowSet crs2 = this.mysql.getCRS("select * from `tab_users` where `user` = ?", player.getUniqueId().toString());

            while (crs2.next()) {
               String user = crs2.getString("user");
               String property = crs2.getString("property");
               String value = crs2.getString("value");
               String world = crs2.getString("world");
               String server = crs2.getString("server");
               TAB.getInstance().debug("Loaded user line: " + String.format("%s, %s, %s, %s, %s", user, property, value, world, server));
               this.checkProperty("MySQL", "player", user, property, server, world, false);
               this.setProperty0(player, property, Server.byName(server), World.byName(world), value);
            }

            TAB.getInstance().debug("Loaded MySQL data of " + player.getName());
            if (crs.size() > 0 || crs2.size() > 0) {
               TAB.getInstance().getFeatureManager().onGroupChange(player);
            }
         } catch (SQLException e) {
            TAB.getInstance().getErrorManager().mysqlQueryFailed(e);
         }
      });
   }
}
