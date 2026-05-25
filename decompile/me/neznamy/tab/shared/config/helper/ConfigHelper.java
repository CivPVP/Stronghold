package me.neznamy.tab.shared.config.helper;

import java.io.File;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;

public class ConfigHelper {
   private final StartupWarnPrinter startupWarnPrinter = new StartupWarnPrinter();
   private final RuntimeErrorPrinter runtimeErrorPrinter = new RuntimeErrorPrinter();

   public StartupWarnPrinter startup() {
      return this.startupWarnPrinter;
   }

   public RuntimeErrorPrinter runtime() {
      return this.runtimeErrorPrinter;
   }

   public void hint(@NonNull File file, @NonNull String message) {
      if (file == null) {
         throw new NullPointerException("file is marked non-null but is null");
      }

      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      TAB.getInstance().getPlatform().logInfo(new TabTextComponent("[Hint] [" + file.getName() + "] " + message, TabTextColor.GOLD));
   }

   public void hint(@NonNull String file, @NonNull String message) {
      if (file == null) {
         throw new NullPointerException("file is marked non-null but is null");
      }

      if (message == null) {
         throw new NullPointerException("message is marked non-null but is null");
      }

      TAB.getInstance().getPlatform().logInfo(new TabTextComponent("[" + file + "] [Hint] " + message, TabTextColor.GOLD));
   }
}
