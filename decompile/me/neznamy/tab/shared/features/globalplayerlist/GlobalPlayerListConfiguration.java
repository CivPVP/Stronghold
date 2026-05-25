package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class GlobalPlayerListConfiguration {
   private final boolean othersAsSpectators;
   private final boolean vanishedAsSpectators;
   private final boolean isolateUnlistedServers;
   private final boolean updateLatency;
   @NotNull
   private final List<String> spyServers;
   @NotNull
   private final Map<String, List<String>> sharedServers;

   @NotNull
   public static GlobalPlayerListConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(
         Arrays.asList(
            "enabled",
            "display-others-as-spectators",
            "display-vanished-players-as-spectators",
            "isolate-unlisted-servers",
            "update-latency",
            "spy-servers",
            "server-groups"
         )
      );
      ConfigurationSection serverGroupSection = section.getConfigurationSection("server-groups");
      Map<String, List<String>> sharedServers = new HashMap<>();
      Map<String, String> takenServers = new HashMap<>();

      for (Object serverGroup : serverGroupSection.getKeys()) {
         String group = serverGroup.toString();
         List<String> servers = serverGroupSection.getStringList(group, Collections.emptyList());
         sharedServers.put(group, servers);

         for (String server : servers) {
            if (takenServers.containsKey(server)) {
               section.startupWarn(
                  String.format(
                     "Server \"%s\" is defined in global playerlist groups \"%s\" and \"%s\", but it can only be a part of one group.",
                     server,
                     takenServers.get(server),
                     group
                  )
               );
            } else {
               takenServers.put(server, group);
            }
         }
      }

      return new GlobalPlayerListConfiguration(
         section.getBoolean("display-others-as-spectators", false),
         section.getBoolean("display-vanished-players-as-spectators", true),
         section.getBoolean("isolate-unlisted-servers", false),
         section.getBoolean("update-latency", false),
         section.getStringList("spy-servers", Collections.singletonList("spyserver1")),
         sharedServers
      );
   }

   @Generated
   public boolean isOthersAsSpectators() {
      return this.othersAsSpectators;
   }

   @Generated
   public boolean isVanishedAsSpectators() {
      return this.vanishedAsSpectators;
   }

   @Generated
   public boolean isIsolateUnlistedServers() {
      return this.isolateUnlistedServers;
   }

   @Generated
   public boolean isUpdateLatency() {
      return this.updateLatency;
   }

   @NotNull
   @Generated
   public List<String> getSpyServers() {
      return this.spyServers;
   }

   @NotNull
   @Generated
   public Map<String, List<String>> getSharedServers() {
      return this.sharedServers;
   }

   @Generated
   public GlobalPlayerListConfiguration(
      boolean othersAsSpectators,
      boolean vanishedAsSpectators,
      boolean isolateUnlistedServers,
      boolean updateLatency,
      @NotNull List<String> spyServers,
      @NotNull Map<String, List<String>> sharedServers
   ) {
      if (spyServers == null) {
         throw new NullPointerException("spyServers is marked non-null but is null");
      }

      if (sharedServers == null) {
         throw new NullPointerException("sharedServers is marked non-null but is null");
      }

      this.othersAsSpectators = othersAsSpectators;
      this.vanishedAsSpectators = vanishedAsSpectators;
      this.isolateUnlistedServers = isolateUnlistedServers;
      this.updateLatency = updateLatency;
      this.spyServers = spyServers;
      this.sharedServers = sharedServers;
   }
}
