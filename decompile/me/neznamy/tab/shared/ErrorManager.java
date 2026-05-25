package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Generated;
import me.neznamy.tab.api.event.TabEvent;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorManager {
   private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");
   private final File errorLog;
   private final File antiOverrideLog;
   private final File placeholderErrorLog;

   public ErrorManager(@NotNull File dataFolder) {
      this.errorLog = new File(dataFolder, "errors.log");
      this.antiOverrideLog = new File(dataFolder, "anti-override.log");
      this.placeholderErrorLog = new File(dataFolder, "placeholder-errors.log");
   }

   public void printError(@Nullable String message, @Nullable Throwable t) {
      this.printError(message, t, false, this.errorLog);
   }

   public void printError(@Nullable String message, @Nullable Throwable t, boolean intoConsoleToo, @NotNull File file) {
      List<String> lines = t == null ? Collections.emptyList() : this.throwableToList(t, false);
      this.printError(message, lines, intoConsoleToo, file);
   }

   private List<String> throwableToList(@NotNull Throwable t, boolean nested) {
      List<String> list = new ArrayList<>();
      String causedText = nested ? "Caused by: " : "";
      list.add(causedText + t.getClass().getName() + ": " + t.getMessage());

      for (StackTraceElement ste : t.getStackTrace()) {
         list.add("\tat " + ste.toString());
      }

      if (t.getCause() != null) {
         list.addAll(this.throwableToList(t.getCause(), true));
      }

      return list;
   }

   public synchronized void printError(@Nullable String message, @NotNull List<String> error, boolean intoConsoleToo, @NotNull File file) {
      try {
         if (!file.exists()) {
            Files.createFile(file.toPath());
         }

         BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));

         try {
            if (message != null) {
               if (file.length() < 16777216L) {
                  buf.write(this.dateFormat.format(new Date()) + "[TAB v" + "5.4.0" + "] " + message + System.lineSeparator());
               }

               if (intoConsoleToo || TAB.getInstance().getConfiguration().getConfig().isDebugMode()) {
                  TAB.getInstance().getPlatform().logWarn(new TabTextComponent(message, TabTextColor.RED));
               }
            }

            for (String line : error) {
               if (file.length() < 16777216L) {
                  buf.write(this.dateFormat.format(new Date()) + line + System.lineSeparator());
               }

               if (intoConsoleToo || TAB.getInstance().getConfiguration().getConfig().isDebugMode()) {
                  TAB.getInstance().getPlatform().logWarn(new TabTextComponent(line, TabTextColor.RED));
               }
            }
         } catch (Throwable var10) {
            try {
               buf.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         buf.close();
      } catch (IOException ex) {
         List<String> lines = new ArrayList<>();
         lines.add("An error occurred when printing error message into file");
         lines.addAll(this.throwableToList(ex, false));
         lines.add("Original error: " + message);
         lines.addAll(error);

         for (String line : lines) {
            TAB.getInstance().getPlatform().logWarn(new TabTextComponent(line, TabTextColor.RED));
         }
      }
   }

   @NotNull
   private Throwable getRootCause(@NotNull Throwable throwable) {
      Throwable rootCause = throwable;

      while (rootCause.getCause() != null) {
         rootCause = rootCause.getCause();
      }

      return rootCause;
   }

   public void placeholderError(@Nullable String message, @Nullable Throwable t) {
      this.printError(message, t, false, this.placeholderErrorLog);
   }

   public void placeholderError(@Nullable String message, @NotNull List<String> t) {
      this.printError(message, t, false, this.placeholderErrorLog);
   }

   public void criticalError(@Nullable String message, @Nullable Throwable t) {
      this.printError(message, t, true, this.errorLog);
   }

   public void groupRetrieveException(@NotNull String pluginName, @NotNull TabPlayer player, Throwable t) {
      this.printError("Permission system " + pluginName + " threw an exception when getting group of " + player.getName(), t, false, this.errorLog);
   }

   public void nullGroupReturned(@NotNull String pluginName, @NotNull TabPlayer player) {
      this.printError("Permission system " + pluginName + " returned null group for player " + player.getName(), Collections.emptyList(), false, this.errorLog);
   }

   public void parseCommandError(@NotNull String placeholder, @NotNull TabPlayer target, @NotNull Throwable t) {
      this.printError("Placeholder " + placeholder + " threw an exception when parsing for player " + target.getName(), t, true, this.errorLog);
   }

   public void unknownProxyMessage(@NotNull String action) {
      this.printError(
         "ProxySupport received unknown action: \"" + action + "\". Does it come from a feature enabled on another proxy, but not here?",
         Collections.emptyList(),
         false,
         this.errorLog
      );
   }

   public void mineSkinDownloadError(@NotNull String id, @NotNull Throwable t) {
      this.printError("Failed to download skin \"" + id + "\" from MineSkin: " + t.getMessage(), Collections.emptyList(), true, this.errorLog);
   }

   public void playerSkinDownloadError(@NotNull String name, @NotNull Throwable t) {
      this.printError("Failed to download skin of player \"" + name + "\": " + t.getMessage(), Collections.emptyList(), true, this.errorLog);
   }

   public void textureSkinDownloadError(@NotNull String texture, @NotNull Throwable t) {
      this.printError("Failed to download skin from texture \"" + texture + "\": " + t.getMessage(), Collections.emptyList(), true, this.errorLog);
   }

   public void taskThrewError(@NotNull Throwable t) {
      this.printError("An error was thrown when executing task", t, false, this.errorLog);
   }

   public void mysqlConnectionFailed(@NotNull Throwable t) {
      Throwable root = this.getRootCause(t);
      this.printError("Failed to connect to MySQL: " + root.getClass().getName() + ": " + root.getMessage(), Collections.emptyList(), true, this.errorLog);
   }

   public void mysqlQueryFailed(@NotNull Throwable t) {
      Throwable root = this.getRootCause(t);
      this.printError(
         "Failed to execute MySQL query due to error: " + root.getClass().getName() + ": " + root.getMessage(), Collections.emptyList(), false, this.errorLog
      );
   }

   public void errorFiringEvent(@NotNull TabEvent event, @NotNull Collection<Throwable> exceptions) {
      this.printError("Some errors occurred whilst trying to fire event " + event, Collections.emptyList(), false, this.errorLog);
      int i = 0;

      for (Throwable exception : exceptions) {
         this.printError("#" + i++ + ": \n", exception, false, this.errorLog);
      }
   }

   public void redisBungeeMessageSendFail(@NotNull Exception e) {
      this.printError("Failed to deliver message through RedisBungee due to an error ", e, false, this.errorLog);
   }

   public void redisBungeeRegisterFail(@NotNull Exception e) {
      this.printError("Failed to register TAB channel in RedisBungee due to an error ", e, false, this.errorLog);
   }

   public void logAntiOverride(@NotNull String message) {
      this.printError(message, Collections.emptyList(), false, this.antiOverrideLog);
   }

   @Generated
   public File getErrorLog() {
      return this.errorLog;
   }

   @Generated
   public File getAntiOverrideLog() {
      return this.antiOverrideLog;
   }
}
