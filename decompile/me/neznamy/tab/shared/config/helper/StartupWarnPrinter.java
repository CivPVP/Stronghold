package me.neznamy.tab.shared.config.helper;

import java.io.File;
import java.util.Set;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.features.sorting.types.SortingType;

public class StartupWarnPrinter {
   private int warnCount;

   public void invalidSkinDefinition(@NonNull String definition) {
      if (definition == null) {
         throw new NullPointerException("definition is marked non-null but is null");
      }

      this.startupWarn(
         "Invalid skin definition: \"" + definition + "\". Supported patterns are:",
         "#1 - \"player:<name>\" for skin of player with specified name",
         "#2 - \"mineskin:<id>\" for UUID of chosen skin from mineskin.org",
         "#3 - \"texture:<texture>\" for raw texture string",
         "#4 - \"signed_texture:<texture>;<signature>\" for raw texture string with signature"
      );
   }

   public void invalidSortingTypeElement(@NonNull String element, @NonNull Set<String> validTypes) {
      if (element == null) {
         throw new NullPointerException("element is marked non-null but is null");
      }

      if (validTypes == null) {
         throw new NullPointerException("validTypes is marked non-null but is null");
      }

      this.startupWarn("\"" + element + "\" is not a valid sorting type element. Valid options are: " + validTypes + ".");
   }

   public void invalidSortingPlaceholder(@NonNull String placeholder, @NonNull SortingType type) {
      if (placeholder == null) {
         throw new NullPointerException("placeholder is marked non-null but is null");
      }

      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      this.startupWarn("\"" + placeholder + "\" is not a valid placeholder for " + type.getClass().getSimpleName() + " sorting type");
   }

   public void invalidSortingLine(@NonNull String configuredLine, @NonNull String message) {
      if (configuredLine == null) {
         throw new NullPointerException("configuredLine is marked non-null but is null");
      }

      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      this.startupWarn("Sorting line \"" + configuredLine + "\" is invalid: " + message);
   }

   public void startupWarn(@NonNull String... messages) {
      if (messages == null) {
         throw new NullPointerException("messages is marked non-null but is null");
      }

      this.warnCount++;

      for (String message : messages) {
         TAB.getInstance().getPlatform().logWarn(new TabTextComponent(message, TabTextColor.RED));
      }
   }

   public void startupWarn(@NonNull File file, @NonNull String message) {
      if (file == null) {
         throw new NullPointerException("file is marked non-null but is null");
      }

      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      this.warnCount++;
      TAB.getInstance().getPlatform().logWarn(new TabTextComponent("[" + file.getName() + "] " + message, TabTextColor.RED));
   }

   public void printWarnCount() {
      if (this.warnCount != 0) {
         TAB.getInstance().getPlatform().logWarn(new TabTextComponent("Found a total of " + this.warnCount + " issues.", TabTextColor.RED));
         this.warnCount = 0;
      }
   }
}
