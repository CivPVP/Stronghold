package me.neznamy.tab.libs.org.yaml.snakeyaml.emitter;

import java.io.IOException;

interface EmitterState {
   void expect() throws IOException;
}
