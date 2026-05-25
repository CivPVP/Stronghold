package me.neznamy.tab.libs.me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.me.neznamy.yamlassist.SyntaxError;
import me.neznamy.tab.libs.me.neznamy.yamlassist.YamlAssist;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public class MissingQuote extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();

      for (int lineNumber = 1; lineNumber <= fileLines.size(); lineNumber++) {
         String text = fileLines.get(lineNumber - 1);
         if (YamlAssist.getError(InvalidLine.class).isLineValid(text) && !this.removeIndent(text).startsWith("#")) {
            String suggestion = this.checkElement(this.getValue(text), lineNumber);
            if (suggestion != null) {
               suggestions.add(suggestion);
            }
         }
      }

      return suggestions;
   }

   private String checkElement(String value, int lineNumber) {
      String result;
      if ((result = this.checkElement(value, lineNumber, "'")) != null) {
         return result;
      } else {
         return (result = this.checkElement(value, lineNumber, "\"")) != null ? result : null;
      }
   }

   private String checkElement(String value, int lineNumber, String c) {
      if (value.equals(c)) {
         return "Add " + c + " at the end of line " + lineNumber + " to finish empty value";
      }

      if (value.startsWith(c) && !value.endsWith(c)) {
         if (this.getCharCount(value, c.charAt(0)) == 2) {
            int i = value.length() - 2;

            while (i > 0 && value.charAt(i) != c.charAt(0)) {
               i--;
            }

            if (i > 0) {
               return "Remove extra text \"" + value.substring(i + 1) + "\" after ending " + c + " in line " + lineNumber;
            }
         }

         return "Add " + c + " at the end of line " + lineNumber;
      } else if (value.endsWith(c) && !value.startsWith(c)) {
         return "Add " + c + " at the beginning of value at line " + lineNumber;
      } else {
         return value.endsWith(c + c) && !value.equals(c + c) ? "Remove one " + c + " from the end of line " + lineNumber : null;
      }
   }

   private int getCharCount(String string, char c) {
      int count = 0;

      for (int i = 0; i < string.length(); i++) {
         if (string.charAt(i) == c) {
            count++;
         }
      }

      return count;
   }
}
