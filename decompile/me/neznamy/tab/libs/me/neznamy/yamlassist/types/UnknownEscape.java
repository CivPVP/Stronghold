package me.neznamy.tab.libs.me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.me.neznamy.yamlassist.SyntaxError;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public class UnknownEscape extends SyntaxError {
   private final char[] validEscapedCharacters = new char[]{'\\', 'b', 'f', 'n', 'r', 't', 'u', '"'};

   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();

      for (int i = 1; i <= fileLines.size(); i++) {
         String line = fileLines.get(i - 1);

         for (int j = 0; j < line.length(); j++) {
            if (line.charAt(j) == '\\' && !this.isValidEscapedCharacter(line.charAt(j + 1))) {
               suggestions.add("Remove the \\ from line " + i + " or add another one after it to make the character display properly.");
               j++;
            }
         }
      }

      return suggestions;
   }

   private boolean isValidEscapedCharacter(char c) {
      for (char valid : this.validEscapedCharacters) {
         if (c == valid) {
            return true;
         }
      }

      return false;
   }
}
