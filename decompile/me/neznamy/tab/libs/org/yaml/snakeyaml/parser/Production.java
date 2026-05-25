package me.neznamy.tab.libs.org.yaml.snakeyaml.parser;

import me.neznamy.tab.libs.org.yaml.snakeyaml.events.Event;

interface Production {
   Event produce();
}
