package me.neznamy.tab.libs.org.yaml.snakeyaml.events;

import me.neznamy.tab.libs.org.yaml.snakeyaml.DumperOptions;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.Mark;

public final class MappingStartEvent extends CollectionStartEvent {
   public MappingStartEvent(String anchor, String tag, boolean implicit, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle) {
      super(anchor, tag, implicit, startMark, endMark, flowStyle);
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.MappingStart;
   }
}
