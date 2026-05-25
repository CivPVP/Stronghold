package me.neznamy.tab.shared.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerListConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataManager {
   private final Map<String, Server> servers = new HashMap<>();
   private final Map<String, ServerGroup> serverGroups = new HashMap<>();
   private final ServerGroup defaultServerGroup = new ServerGroup("DEFAULT", new ArrayList<>());
   private final Map<String, World> worlds = new HashMap<>();
   @Nullable
   private GlobalPlayerListConfiguration globalPlayerListConfiguration;

   public void applyConfiguration(@NotNull GlobalPlayerListConfiguration configuration) {
      this.globalPlayerListConfiguration = configuration;

      for (String server : configuration.getSpyServers()) {
         this.servers.computeIfAbsent(server, Server::new).markSpyServer();
      }

      for (Entry<String, List<String>> entry : configuration.getSharedServers().entrySet()) {
         this.serverGroups.put(entry.getKey(), new ServerGroup(entry.getKey(), entry.getValue()));
      }

      for (Server server : this.servers.values()) {
         server.setServerGroup(this.computeServerGroup(server));
      }
   }

   @Nullable
   ServerGroup computeServerGroup(@NotNull Server server) {
      if (this.globalPlayerListConfiguration == null) {
         return null;
      }

      for (ServerGroup group : this.serverGroups.values()) {
         for (String serverDefinition : group.getPatterns()) {
            if (serverDefinition.endsWith("*")) {
               if (server.getName().startsWith(serverDefinition.substring(0, serverDefinition.length() - 1).toLowerCase())) {
                  return group;
               }
            } else if (serverDefinition.startsWith("*")) {
               if (server.getName().endsWith(serverDefinition.substring(1).toLowerCase())) {
                  return group;
               }
            } else if (server.getName().equals(serverDefinition)) {
               return group;
            }
         }
      }

      return this.globalPlayerListConfiguration.isIsolateUnlistedServers() ? new ServerGroup("", Collections.emptyList()) : this.defaultServerGroup;
   }

   @Generated
   public Map<String, Server> getServers() {
      return this.servers;
   }

   @Generated
   public Map<String, ServerGroup> getServerGroups() {
      return this.serverGroups;
   }

   @Generated
   public ServerGroup getDefaultServerGroup() {
      return this.defaultServerGroup;
   }

   @Generated
   public Map<String, World> getWorlds() {
      return this.worlds;
   }

   @Nullable
   @Generated
   public GlobalPlayerListConfiguration getGlobalPlayerListConfiguration() {
      return this.globalPlayerListConfiguration;
   }
}
