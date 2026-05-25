package me.neznamy.tab.libs.me.neznamy.yamlassist;

import java.util.List;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public abstract class SyntaxError {
   public abstract List<String> getSuggestions(YAMLException var1, List<String> var2);

   protected String removeIndent(String line) {
      String replaced = line;

      while (replaced.startsWith(" ") || replaced.startsWith("\t")) {
         replaced = replaced.substring(1);
      }

      while (replaced.endsWith(" ") || replaced.endsWith("\t")) {
         replaced = replaced.substring(0, replaced.length() - 1);
      }

      return replaced;
   }

   protected int getIndentCount(String line) {
      if (line.split("#")[0].replace(" ", "").isEmpty()) {
         return 0;
      }

      int index = 0;
      int spaces = 0;

      while (line.length() > index && (line.charAt(index) == ' ' || line.charAt(index) == '\t')) {
         if (line.charAt(index) == ' ') {
            index++;
            spaces++;
         } else {
            index++;
            spaces += 4;
         }
      }

      return spaces;
   }

   protected String getValue(String line) {
      String value = this.removeIndent(line);
      if (value.startsWith("- ")) {
         value = value.substring(2);
      } else if (value.contains(": ")) {
         value = value.substring(value.split(": ")[0].length() + 2);
      } else {
         for (String c : new String[]{"'", "\""}) {
            if (value.startsWith(c)) {
               value = value.substring(1);
               int index = value.indexOf(c) + 2;
               if (value.length() >= index) {
                  value = value.substring(index);
                  if (!value.isEmpty()) {
                     value = value.substring(1);
                  }
               }
            }
         }
      }

      return this.removeEndLineComments(value);
   }

   protected String removeEndLineComments(String line) {
      StringBuilder sb = new StringBuilder();
      boolean insideQuotes = false;
      char quoteChar = 0;

      for (int i = 0; i < line.length(); i++) {
         char c = line.charAt(i);
         if (c == '"' || c == '\'') {
            if (i == 0) {
               insideQuotes = true;
               quoteChar = c;
            } else if (quoteChar == c) {
               insideQuotes = false;
            }
         }

         if (c == '#' && !insideQuotes && (quoteChar != 0 || sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ')) {
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
               sb.setLength(sb.length() - 1);
            }

            return sb.toString();
         }

         sb.append(c);
      }

      while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
         sb.setLength(sb.length() - 1);
      }

      return sb.toString();
   }
}
