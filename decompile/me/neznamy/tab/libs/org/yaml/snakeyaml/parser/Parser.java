package me.neznamy.tab.libs.org.yaml.snakeyaml.parser;

import me.neznamy.tab.libs.org.yaml.snakeyaml.events.Event;

public interface Parser {
   boolean checkEvent(Event.ID var1);

   Event peekEvent();

   Event getEvent();
}
