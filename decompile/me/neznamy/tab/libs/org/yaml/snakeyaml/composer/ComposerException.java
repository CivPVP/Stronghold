package me.neznamy.tab.libs.org.yaml.snakeyaml.composer;

import me.neznamy.tab.libs.org.yaml.snakeyaml.error.Mark;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.MarkedYAMLException;

public class ComposerException extends MarkedYAMLException {
   private static final long serialVersionUID = 2146314636913113935L;

   protected ComposerException(String context, Mark contextMark, String problem, Mark problemMark) {
      super(context, contextMark, problem, problemMark);
   }
}
