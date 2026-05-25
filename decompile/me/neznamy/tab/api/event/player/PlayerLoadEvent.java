package me.neznamy.tab.api.event.player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.TabEvent;
import org.jetbrains.annotations.NotNull;

public interface PlayerLoadEvent extends TabEvent {
   @NotNull
   TabPlayer getPlayer();

   boolean isJoin();
}
