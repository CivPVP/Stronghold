package me.neznamy.tab.shared.features.playerlistobjective;

import java.util.concurrent.atomic.AtomicBoolean;
import me.neznamy.tab.shared.Property;

public class PlayerListObjectivePlayerData {
   public Property valueLegacy;
   public Property valueModern;
   public Property title;
   public final AtomicBoolean disabled = new AtomicBoolean();
}
