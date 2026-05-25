package me.neznamy.tab.libs.org.yaml.snakeyaml.emitter;

import java.io.IOException;
import me.neznamy.tab.libs.org.yaml.snakeyaml.events.Event;

public interface Emitable {
   void emit(Event var1) throws IOException;
}
