package me.neznamy.tab.api.bossbar;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BossBarManager {
   @NotNull
   BossBar createBossBar(@NonNull String var1, float var2, @NonNull BarColor var3, @NonNull BarStyle var4);

   @NotNull
   BossBar createBossBar(@NonNull String var1, @NonNull String var2, @NonNull String var3, @NonNull String var4);

   @Nullable
   BossBar getBossBar(@NonNull String var1);

   @NotNull
   Map<String, BossBar> getRegisteredBossBars();

   void removeBossBar(@NonNull String var1);

   void removeBossBar(@NonNull BossBar var1);

   void toggleBossBar(@NonNull TabPlayer var1, boolean var2);

   boolean hasBossBarVisible(@NonNull TabPlayer var1);

   void setBossBarVisible(@NonNull TabPlayer var1, boolean var2, boolean var3);

   void sendBossBarTemporarily(@NonNull TabPlayer var1, @NonNull String var2, int var3);

   void announceBossBar(@NonNull String var1, int var2);

   @NotNull
   List<BossBar> getAnnouncedBossBars();
}
