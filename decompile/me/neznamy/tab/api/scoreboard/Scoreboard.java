package me.neznamy.tab.api.scoreboard;

import java.util.List;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public interface Scoreboard {
   @NotNull
   String getName();

   @NotNull
   String getTitle();

   void setTitle(@NonNull String var1);

   @NotNull
   List<Line> getLines();

   void addLine(@NonNull String var1);

   void removeLine(int var1);

   void unregister();
}
