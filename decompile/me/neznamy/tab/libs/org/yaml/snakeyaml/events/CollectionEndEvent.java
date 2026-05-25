package me.neznamy.tab.libs.org.yaml.snakeyaml.events;

import me.neznamy.tab.libs.org.yaml.snakeyaml.error.Mark;

public abstract class CollectionEndEvent extends Event {
   public CollectionEndEvent(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }
}
