package me.neznamy.tab.shared.config.mysql;

import java.util.Arrays;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MySQLConfiguration {
   @NonNull
   private final String host;
   private final int port;
   @NonNull
   private final String database;
   @NonNull
   private final String username;
   @NonNull
   private final String password;
   private final boolean useSSL;

   @NotNull
   public static MySQLConfiguration fromSection(@NonNull ConfigurationSection section) {
      if (section == null) {
         throw new NullPointerException("section is marked non-null but is null");
      }

      section.checkForUnknownKey(Arrays.asList("enabled", "host", "port", "database", "username", "password", "usessl"));
      return new MySQLConfiguration(
         section.getString("host", "127.0.0.1"),
         section.getInt("port", 3306),
         section.getString("database", "tab"),
         section.getString("username", "user"),
         section.getString("password", "password"),
         section.getBoolean("useSSL", true)
      );
   }

   @NonNull
   @Generated
   public String getHost() {
      return this.host;
   }

   @Generated
   public int getPort() {
      return this.port;
   }

   @NonNull
   @Generated
   public String getDatabase() {
      return this.database;
   }

   @NonNull
   @Generated
   public String getUsername() {
      return this.username;
   }

   @NonNull
   @Generated
   public String getPassword() {
      return this.password;
   }

   @Generated
   public boolean isUseSSL() {
      return this.useSSL;
   }

   @Generated
   public MySQLConfiguration(@NonNull String host, int port, @NonNull String database, @NonNull String username, @NonNull String password, boolean useSSL) {
      if (host == null) {
         throw new NullPointerException("host is marked non-null but is null");
      }

      if (database == null) {
         throw new NullPointerException("database is marked non-null but is null");
      }

      if (username == null) {
         throw new NullPointerException("username is marked non-null but is null");
      }

      if (password == null) {
         throw new NullPointerException("password is marked non-null but is null");
      }

      this.host = host;
      this.port = port;
      this.database = database;
      this.username = username;
      this.password = password;
      this.useSSL = useSSL;
   }
}
