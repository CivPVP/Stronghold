package me.neznamy.tab.shared.features.bossbar;

import java.util.IdentityHashMap;
import java.util.Map;

public class BossBarPlayerData {
   public boolean visible;
   public final Map<BossBarLine, BossBarLinePlayerProperties> visibleBossBars = new IdentityHashMap<>();
}
