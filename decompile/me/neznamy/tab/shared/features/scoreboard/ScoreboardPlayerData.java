package me.neznamy.tab.shared.features.scoreboard;

import java.util.IdentityHashMap;
import java.util.Map;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreboardPlayerData {
   public boolean joinDelayed;
   public boolean visible;
   @Nullable
   public ScoreboardImpl activeScoreboard;
   @Nullable
   public ScoreboardImpl forcedScoreboard;
   @Nullable
   public String otherPluginScoreboard;
   @Nullable
   public Property titleProperty;
   @NotNull
   public final Map<ScoreboardLine, Property> lineProperties = new IdentityHashMap<>();
   @NotNull
   public final Map<ScoreboardLine, Property> lineNameProperties = new IdentityHashMap<>();
   @NotNull
   public final Map<ScoreboardLine, Property> numberFormatProperties = new IdentityHashMap<>();
}
