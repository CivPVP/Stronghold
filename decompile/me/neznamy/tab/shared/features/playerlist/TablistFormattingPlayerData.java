package me.neznamy.tab.shared.features.playerlist;

import java.util.concurrent.atomic.AtomicBoolean;
import me.neznamy.tab.shared.Property;

public class TablistFormattingPlayerData {
   public Property prefix;
   public Property name;
   public Property suffix;
   public final AtomicBoolean disabled = new AtomicBoolean();
}
