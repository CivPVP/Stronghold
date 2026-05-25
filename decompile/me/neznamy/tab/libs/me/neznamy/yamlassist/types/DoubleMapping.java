package me.neznamy.tab.libs.me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.me.neznamy.yamlassist.SyntaxError;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public class DoubleMapping extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();
      String[] arr = exception.getMessage().split(", line ");
      if (arr.length == 1) {
         return suggestions;
      }

      int line = Integer.parseInt(arr[1].split(",")[0]);
      String text = fileLines.get(line - 1).split("#")[0];
      if (exception.getMessage().contains("mapping values are not allowed here") && text.endsWith(":") && this.getIndentCount(text) % 2 == 0) {
         suggestions.add("Remove the last : from line " + line + ".");
      }

      return suggestions;
   }
}
