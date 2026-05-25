package me.neznamy.tab.shared.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.files.Animations;
import me.neznamy.tab.shared.config.files.Config;
import me.neznamy.tab.shared.config.mysql.MySQL;
import me.neznamy.tab.shared.config.mysql.MySQLGroupConfiguration;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.config.skin.SkinManager;
import me.neznamy.tab.shared.data.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Configs {
   private final Config config = new Config();
   private final Animations animations = new Animations();
   private final MessageFile messages = new MessageFile();
   private final ConfigurationFile playerData = new YamlConfigurationFile(
      new ByteArrayInputStream(new byte[0]), new File(TAB.getInstance().getDataFolder(), "playerdata.yml")
   );
   private final ConfigurationFile skinCache = new YamlConfigurationFile(
      new ByteArrayInputStream(new byte[0]), new File(TAB.getInstance().getDataFolder(), "skincache.yml")
   );
   private final SkinManager skinManager = new SkinManager(this.skinCache);
   private PropertyConfiguration groups;
   private PropertyConfiguration users;
   private MySQL mysql;

   public Configs() throws IOException {
      File errorLog = TAB.getInstance().getErrorManager().getErrorLog();
      if (errorLog.length() > 16777216L) {
         TAB.getInstance()
            .getConfigHelper()
            .startup()
            .startupWarn(
               errorLog,
               "The file has reached its size limit (16MB). No new errors will be logged. Take a look at the existing logged errors, as they may have caused the plugin to not work properly in the past and if not fixed, will most likely cause problems in the future as well. If you are using latest version of the plugin, consider reporting them."
            );
      }

      if (this.config.getMysql() != null) {
         try {
            this.mysql = new MySQL(this.config.getMysql());
            this.mysql.openConnection();
            this.groups = new MySQLGroupConfiguration(this.mysql);
            this.users = new MySQLUserConfiguration(this.mysql);
            return;
         } catch (SQLException e) {
            TAB.getInstance().getErrorManager().mysqlConnectionFailed(e);
         }
      }

      this.groups = new YamlPropertyConfigurationFile(
         this.getClass().getClassLoader().getResourceAsStream("config/groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml")
      );
      this.users = new YamlPropertyConfigurationFile(
         this.getClass().getClassLoader().getResourceAsStream("config/users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml")
      );
   }

   @NotNull
   public String getGroup(@NonNull Collection<String> serverGroups, @Nullable String element) {
      if (serverGroups == null) {
         throw new NullPointerException("serverGroups is marked non-null but is null");
      }

      if (element == null) {
         return "null";
      }

      if (serverGroups.isEmpty()) {
         return element;
      }

      for (Object worldGroup : serverGroups) {
         for (String definedWorld : worldGroup.toString().split(";")) {
            if (definedWorld.endsWith("*")) {
               if (element.toLowerCase().startsWith(definedWorld.substring(0, definedWorld.length() - 1).toLowerCase())) {
                  return worldGroup.toString();
               }
            } else if (definedWorld.startsWith("*")) {
               if (element.toLowerCase().endsWith(definedWorld.substring(1).toLowerCase())) {
                  return worldGroup.toString();
               }
            } else if (element.equalsIgnoreCase(definedWorld)) {
               return worldGroup.toString();
            }
         }
      }

      return element;
   }

   @NotNull
   public String getServerGroup(@NonNull Collection<String> serverGroups, @Nullable Server server) {
      if (serverGroups == null) {
         throw new NullPointerException("serverGroups is marked non-null but is null");
      }

      String globalGroup = this.tryServerGroup(serverGroups, server);
      return globalGroup != null ? globalGroup : this.getGroup(serverGroups, server == null ? null : server.getName());
   }

   @Nullable
   private String tryServerGroup(@NonNull Collection<String> serverGroups, @Nullable Server server) {
      if (serverGroups == null) {
         throw new NullPointerException("serverGroups is marked non-null but is null");
      } else if (serverGroups.isEmpty() || server == null) {
         return null;
      } else if (serverGroups.contains(server.getName())) {
         return server.getName();
      } else {
         return server.getServerGroup() != null && serverGroups.contains(server.getServerGroup().getName()) ? server.getServerGroup().getName() : null;
      }
   }

   @Generated
   public Config getConfig() {
      return this.config;
   }

   @Generated
   public Animations getAnimations() {
      return this.animations;
   }

   @Generated
   public MessageFile getMessages() {
      return this.messages;
   }

   @Generated
   public ConfigurationFile getPlayerData() {
      return this.playerData;
   }

   @Generated
   public ConfigurationFile getSkinCache() {
      return this.skinCache;
   }

   @Generated
   public SkinManager getSkinManager() {
      return this.skinManager;
   }

   @Generated
   public PropertyConfiguration getGroups() {
      return this.groups;
   }

   @Generated
   public PropertyConfiguration getUsers() {
      return this.users;
   }

   @Generated
   public MySQL getMysql() {
      return this.mysql;
   }
}
