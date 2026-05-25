package me.neznamy.tab.shared.config.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySQL {
   private Connection con;
   @NotNull
   private final MySQLConfiguration configuration;

   public void openConnection() throws SQLException {
      if (!this.isConnected()) {
         Properties properties = new Properties();
         properties.setProperty("user", this.configuration.getUsername());
         properties.setProperty("password", this.configuration.getPassword());
         properties.setProperty("useSSL", String.valueOf(this.configuration.isUseSSL()));
         properties.setProperty("characterEncoding", "UTF-8");
         this.con = DriverManager.getConnection(
            String.format("jdbc:mysql://%s:%d/%s", this.configuration.getHost(), this.configuration.getPort(), this.configuration.getDatabase()), properties
         );
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Successfully connected to MySQL", TabTextColor.GREEN));
      }
   }

   public void closeConnection() throws SQLException {
      if (this.isConnected()) {
         this.con.close();
      }
   }

   private boolean isConnected() throws SQLException {
      return this.con != null && !this.con.isClosed();
   }

   public void execute(@NonNull String query, @Nullable Object... vars) throws SQLException {
      if (query == null) {
         throw new NullPointerException("query is marked non-null but is null");
      }

      PreparedStatement ps = this.prepareStatement(query, vars);

      try {
         ps.execute();
      } catch (Throwable var7) {
         if (ps != null) {
            try {
               ps.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (ps != null) {
         ps.close();
      }
   }

   @NotNull
   private PreparedStatement prepareStatement(@NonNull String query, @Nullable Object... vars) throws SQLException {
      if (query == null) {
         throw new NullPointerException("query is marked non-null but is null");
      }

      if (!this.isConnected()) {
         this.openConnection();
      }

      PreparedStatement ps = this.con.prepareStatement(query);
      int i = 0;
      if (query.contains("?")) {
         for (Object obj : vars) {
            ps.setObject(++i, obj);
         }
      }

      return ps;
   }

   @NotNull
   public CachedRowSet getCRS(@NonNull String query, @NonNull Object... vars) throws SQLException {
      if (query == null) {
         throw new NullPointerException("query is marked non-null but is null");
      }

      if (vars == null) {
         throw new NullPointerException("vars is marked non-null but is null");
      }

      PreparedStatement ps = this.prepareStatement(query, vars);
      ResultSet rs = ps.executeQuery();

      try {
         CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
         crs.populate(rs);
         return crs;
      } finally {
         rs.close();
         ps.close();
      }
   }

   @Generated
   public MySQL(@NotNull MySQLConfiguration configuration) {
      if (configuration == null) {
         throw new NullPointerException("configuration is marked non-null but is null");
      }

      this.configuration = configuration;
   }
}
