package me.neznamy.tab.api.bossbar;

import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

public interface BossBar {
   @NotNull
   String getName();

   @NotNull
   UUID getUniqueId();

   void setTitle(@NonNull String var1);

   void setProgress(@NonNull String var1);

   void setProgress(float var1);

   void setColor(@NonNull String var1);

   void setColor(@NonNull BarColor var1);

   void setStyle(@NonNull String var1);

   void setStyle(@NonNull BarStyle var1);

   @NotNull
   String getTitle();

   @NotNull
   String getProgress();

   @NotNull
   String getColor();

   @NotNull
   String getStyle();

   void addPlayer(@NonNull TabPlayer var1);

   void removePlayer(@NonNull TabPlayer var1);

   @NotNull
   List<TabPlayer> getPlayers();

   boolean containsPlayer(@NonNull TabPlayer var1);

   boolean isAnnouncementBar();
}
