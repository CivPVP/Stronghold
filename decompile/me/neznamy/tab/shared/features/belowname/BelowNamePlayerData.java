package me.neznamy.tab.shared.features.belowname;

import java.util.concurrent.atomic.AtomicBoolean;
import me.neznamy.tab.shared.Property;

public class BelowNamePlayerData {
   public Property score;
   public Property numberFormat;
   public Property text;
   public Property defaultNumberFormat;
   public final AtomicBoolean disabled = new AtomicBoolean();
}
