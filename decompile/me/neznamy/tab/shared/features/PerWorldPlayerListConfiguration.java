package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PerWorldPlayerListConfiguration {
   private final boolean allowBypassPermission;
   @NotNull
   private final List<String> ignoredWorlds;
   @NotNull
   private final Map<String, List<String>> sharedWorlds;

   @NotNull
   public static PerWorldPlayerListConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(Arrays.asList("enabled", "allow-bypass-permission", "ignore-effect-in-worlds", "shared-playerlist-world-groups"));
      ConfigurationSection sharedWorldsSection = section.getConfigurationSection("shared-playerlist-world-groups");
      Map<String, List<String>> sharedWorlds = new HashMap<>();
      Map<String, String> takenWorlds = new HashMap<>();

      for (Object worldGroup : sharedWorldsSection.getKeys()) {
         String group = worldGroup.toString();
         List<String> worlds = sharedWorldsSection.getStringList(group, Collections.emptyList());
         sharedWorlds.put(group, worlds);

         for (String server : worlds) {
            if (takenWorlds.containsKey(server)) {
               section.startupWarn(
                  String.format(
                     "World \"%s\" is defined in per world playerlist groups \"%s\" and \"%s\", but it can only be a part of one group.",
                     server,
                     takenWorlds.get(server),
                     group
                  )
               );
            } else {
               takenWorlds.put(server, group);
            }
         }
      }

      return new PerWorldPlayerListConfiguration(
         section.getBoolean("allow-bypass-permission", false),
         section.getStringList("ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build")),
         sharedWorlds
      );
   }

   @Generated
   public boolean isAllowBypassPermission() {
      return this.allowBypassPermission;
   }

   @NotNull
   @Generated
   public List<String> getIgnoredWorlds() {
      return this.ignoredWorlds;
   }

   @NotNull
   @Generated
   public Map<String, List<String>> getSharedWorlds() {
      return this.sharedWorlds;
   }

   @Generated
   public PerWorldPlayerListConfiguration(boolean allowBypassPermission, @NotNull List<String> ignoredWorlds, @NotNull Map<String, List<String>> sharedWorlds) {
      if (ignoredWorlds == null) {
         throw new NullPointerException("ignoredWorlds is marked non-null but is null");
      }

      if (sharedWorlds == null) {
         throw new NullPointerException("sharedWorlds is marked non-null but is null");
      }

      this.allowBypassPermission = allowBypassPermission;
      this.ignoredWorlds = ignoredWorlds;
      this.sharedWorlds = sharedWorlds;
   }
}
