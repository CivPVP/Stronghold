package me.neznamy.tab.api;

import java.util.UUID;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TabPlayer {
   @NotNull
   String getName();

   @NotNull
   UUID getUniqueId();

   @NotNull
   Object getPlayer();

   boolean isLoaded();

   @NotNull
   String getGroup();

   void setTemporaryGroup(@Nullable String var1);

   boolean hasTemporaryGroup();

   void setExpectedProfileName(@NonNull String var1);

   @NotNull
   String getExpectedProfileName();

   @NotNull
   String getServer();

   @NotNull
   String getWorld();

   boolean isBedrockPlayer();
}
