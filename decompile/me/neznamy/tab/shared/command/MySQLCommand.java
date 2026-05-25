package me.neznamy.tab.shared.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.sql.rowset.CachedRowSet;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQL;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySQLCommand extends SubCommand {
   protected MySQLCommand() {
      super("mysql", null);
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      if (args.length == 0) {
         this.sendMessages(sender, this.getMessages().getMySQLHelpMenu());
      } else {
         if (args[0].equalsIgnoreCase("download")) {
            if (this.hasPermission(sender, "tab.mysql.download")) {
               this.download(sender);
            } else {
               this.sendMessage(sender, this.getMessages().getNoPermission());
            }
         } else if (args[0].equalsIgnoreCase("upload")) {
            if (this.hasPermission(sender, "tab.mysql.upload")) {
               this.upload(sender);
            } else {
               this.sendMessage(sender, this.getMessages().getNoPermission());
            }
         } else {
            for (String message : this.getMessages().getMySQLHelpMenu()) {
               this.sendMessage(sender, message);
            }
         }
      }
   }

   private void download(@Nullable TabPlayer sender) {
      MySQL mysql = TAB.getInstance().getConfiguration().getMysql();
      if (mysql == null) {
         this.sendMessage(sender, this.getMessages().getMySQLFailNotEnabled());
      } else {
         TAB.getInstance()
            .getCPUManager()
            .getMysqlThread()
            .execute(
               () -> {
                  try {
                     YamlPropertyConfigurationFile groupFile = new YamlPropertyConfigurationFile(
                        Configs.class.getClassLoader().getResourceAsStream("config/groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml")
                     );
                     YamlPropertyConfigurationFile userFile = new YamlPropertyConfigurationFile(
                        Configs.class.getClassLoader().getResourceAsStream("config/users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml")
                     );
                     CachedRowSet crs = mysql.getCRS("select * from tab_groups");

                     while (crs.next()) {
                        groupFile.setProperty(
                           crs.getString("group"),
                           crs.getString("property"),
                           Server.byName(crs.getString("server")),
                           World.byName(crs.getString("world")),
                           crs.getString("value")
                        );
                     }

                     crs = mysql.getCRS("select * from tab_users");

                     while (crs.next()) {
                        userFile.setProperty(
                           crs.getString("user"),
                           crs.getString("property"),
                           Server.byName(crs.getString("server")),
                           World.byName(crs.getString("world")),
                           crs.getString("value")
                        );
                     }

                     this.sendMessage(sender, this.getMessages().getMySQLDownloadSuccess());
                  } catch (YAMLException | IOException | SQLException e) {
                     this.sendMessage(sender, this.getMessages().getMySQLFailError());
                     TAB.getInstance().getErrorManager().criticalError("MySQL download failed", e);
                  }
               }
            );
      }
   }

   private void upload(@Nullable TabPlayer sender) {
      MySQL mysql = TAB.getInstance().getConfiguration().getMysql();
      if (mysql == null) {
         this.sendMessage(sender, this.getMessages().getMySQLFailNotEnabled());
      } else {
         TAB.getInstance()
            .getCPUManager()
            .getMysqlThread()
            .execute(
               () -> {
                  try {
                     YamlPropertyConfigurationFile groupFile = new YamlPropertyConfigurationFile(
                        Configs.class.getClassLoader().getResourceAsStream("config/groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml")
                     );
                     YamlPropertyConfigurationFile userFile = new YamlPropertyConfigurationFile(
                        Configs.class.getClassLoader().getResourceAsStream("config/users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml")
                     );
                     mysql.execute("DELETE FROM tab_groups");
                     mysql.execute("DELETE FROM tab_users");
                     this.upload(groupFile, TAB.getInstance().getConfiguration().getGroups());
                     this.upload(userFile, TAB.getInstance().getConfiguration().getUsers());
                     this.sendMessage(sender, this.getMessages().getMySQLUploadSuccess());
                  } catch (YAMLException | IOException | SQLException e) {
                     this.sendMessage(sender, this.getMessages().getMySQLFailError());
                     TAB.getInstance().getErrorManager().criticalError("MySQL upload failed", e);
                  }
               }
            );
      }
   }

   private void upload(@NotNull YamlPropertyConfigurationFile file, @NotNull PropertyConfiguration mysqlTable) {
      for (String name : file.getAllEntries()) {
         for (Entry<String, Object> property : file.getGlobalSettings(name).entrySet()) {
            mysqlTable.setProperty(name, property.getKey(), null, null, this.toString(property.getValue()));
         }

         for (Entry<String, Map<String, Object>> world : file.getPerWorldSettings(name).entrySet()) {
            if (world.getValue() != null) {
               for (Entry<String, Object> property : world.getValue().entrySet()) {
                  mysqlTable.setProperty(name, property.getKey(), null, World.byName(world.getKey()), this.toString(property.getValue()));
               }
            }
         }

         for (Entry<String, Map<String, Object>> server : file.getPerServerSettings(name).entrySet()) {
            if (server.getValue() != null) {
               for (Entry<String, Object> property : server.getValue().entrySet()) {
                  mysqlTable.setProperty(name, property.getKey(), Server.byName(server.getKey()), null, this.toString(property.getValue()));
               }
            }
         }
      }
   }

   private String toString(@NotNull Object obj) {
      return obj instanceof List ? ((List)obj).stream().map(Object::toString).collect(Collectors.joining("\n")) : obj.toString();
   }

   @NotNull
   @Override
   public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
      return this.getStartingArgument(Arrays.asList("download", "upload"), arguments[0]);
   }
}
