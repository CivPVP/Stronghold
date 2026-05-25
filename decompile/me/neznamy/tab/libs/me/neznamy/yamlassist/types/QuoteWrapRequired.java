package me.neznamy.tab.libs.me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.me.neznamy.yamlassist.SyntaxError;
import me.neznamy.tab.libs.me.neznamy.yamlassist.YamlAssist;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public class QuoteWrapRequired extends SyntaxError {
   private final char[] invalidStartCharacters = new char[]{'\u0000', '%', '-', '.', '[', '{', ']', '}', ',', '?', ':', '*', '&', '!', '|', '>'};

   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();

      for (int lineNumber = 1; lineNumber <= fileLines.size(); lineNumber++) {
         String line = this.removeIndent(fileLines.get(lineNumber - 1));
         if (YamlAssist.getError(InvalidLine.class).isLineValid(line) && !line.startsWith("#") && !line.isEmpty()) {
            String value = this.getValue(line);
            if (!value.equals("{}") && !value.equals("[]")) {
               for (char invalid : this.invalidStartCharacters) {
                  if (value.startsWith(invalid + "")) {
                     suggestions.add("Wrap value in line " + lineNumber + " into quotes.");
                  }
               }
            }
         }
      }

      return suggestions;
   }
}
