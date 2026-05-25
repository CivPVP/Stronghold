package me.neznamy.tab.api.scoreboard;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public interface Line {
   @NotNull
   String getText();

   void setText(@NonNull String var1);
}
