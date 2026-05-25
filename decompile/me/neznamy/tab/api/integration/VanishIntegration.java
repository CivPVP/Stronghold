package me.neznamy.tab.api.integration;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class VanishIntegration {
   private static final List<VanishIntegration> HANDLERS = new ArrayList<>();
   @NotNull
   private final String plugin;

   public abstract boolean canSee(@NotNull TabPlayer var1, @NotNull TabPlayer var2);

   public abstract boolean isVanished(@NotNull TabPlayer var1);

   public void register() {
      registerHandler(this);
   }

   public void unregister() {
      unregisterHandler(this);
   }

   public static void registerHandler(@NotNull VanishIntegration handler) {
      HANDLERS.add(handler);
   }

   public static void unregisterHandler(@NotNull VanishIntegration handler) {
      HANDLERS.remove(handler);
   }

   @NotNull
   public static List<VanishIntegration> getHandlers() {
      return HANDLERS;
   }

   @NotNull
   @Generated
   public String getPlugin() {
      return this.plugin;
   }

   @Generated
   public VanishIntegration(@NotNull String plugin) {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      }

      this.plugin = plugin;
   }
}
