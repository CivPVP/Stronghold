package me.neznamy.tab.libs.org.yaml.snakeyaml.constructor;

import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;
import me.neznamy.tab.libs.org.yaml.snakeyaml.nodes.Node;

public abstract class AbstractConstruct implements Construct {
   @Override
   public void construct2ndStep(Node node, Object data) {
      if (node.isTwoStepsConstruction()) {
         throw new IllegalStateException("Not Implemented in " + this.getClass().getName());
      } else {
         throw new YAMLException("Unexpected recursive structure for Node: " + node);
      }
   }
}
