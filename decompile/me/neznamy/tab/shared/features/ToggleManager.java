package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class ToggleManager {
   @NotNull
   private final ConfigurationFile playerDataFile;
   @NotNull
   private final String sectionName;
   @NotNull
   private final Set<String> toggledPlayers;

   public ToggleManager(@NotNull ConfigurationFile playerDataFile, @NotNull String sectionName) {
      this.playerDataFile = playerDataFile;
      this.sectionName = sectionName;
      this.toggledPlayers = new HashSet<>(playerDataFile.getStringList(sectionName, Collections.emptyList()));
   }

   public void convert(@NotNull TabPlayer player) {
      if (this.toggledPlayers.remove(player.getName())) {
         this.toggledPlayers.add(player.getUniqueId().toString());
         this.save();
      }
   }

   public boolean contains(@NotNull TabPlayer player) {
      return this.toggledPlayers.contains(player.getUniqueId().toString());
   }

   public void add(@NotNull TabPlayer player) {
      if (this.toggledPlayers.add(player.getUniqueId().toString())) {
         this.save();
      }
   }

   public void remove(@NotNull TabPlayer player) {
      if (this.toggledPlayers.remove(player.getUniqueId().toString())) {
         this.save();
      }
   }

   private void save() {
      this.playerDataFile.set(this.sectionName, new ArrayList<>(this.toggledPlayers));
   }
}
