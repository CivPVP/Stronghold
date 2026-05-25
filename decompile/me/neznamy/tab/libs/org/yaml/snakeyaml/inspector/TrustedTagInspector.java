package me.neznamy.tab.libs.org.yaml.snakeyaml.inspector;

import me.neznamy.tab.libs.org.yaml.snakeyaml.nodes.Tag;

public final class TrustedTagInspector implements TagInspector {
   @Override
   public boolean isGlobalTagAllowed(Tag tag) {
      return true;
   }
}
