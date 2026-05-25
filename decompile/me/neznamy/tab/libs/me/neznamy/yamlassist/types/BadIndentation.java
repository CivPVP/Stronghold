package me.neznamy.tab.libs.me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.me.neznamy.yamlassist.SyntaxError;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public class BadIndentation extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      return this.checkForIndent(fileLines);
   }

   private List<String> checkForIndent(List<String> lines) {
      List<String> suggestions = new ArrayList<>();

      for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
         String line = lines.get(lineNumber);
         if (!this.isComment(line)) {
            int currentLineIndent = this.getIndentCount(line);
            String prevLine = lineNumber == 0 ? "" : lines.get(lineNumber - 1);
            int remove = 1;

            while (prevLine.isEmpty() || this.isComment(prevLine)) {
               int id = lineNumber - remove++;
               if (id == -1) {
                  prevLine = "";
                  break;
               }

               prevLine = lines.get(id);
            }

            prevLine = this.removeEndLineComments(prevLine);
            int prevLineIndent = this.getIndentCount(prevLine);
            if (this.removeSpaces(prevLine).endsWith(":")) {
               if (currentLineIndent - prevLineIndent > 2) {
                  suggestions.add("Remove " + (currentLineIndent - prevLineIndent - 2) + " space(s) from line " + (lineNumber + 1));
                  lineNumber++;
                  continue;
               }

               if (currentLineIndent - prevLineIndent == 1) {
                  suggestions.add("Add 1 space to line " + (lineNumber + 1));
                  lineNumber++;
                  continue;
               }

               if (prevLineIndent - currentLineIndent == 1) {
                  if (this.removeSpaces(line).startsWith("-")) {
                     suggestions.add("Add 1 or 3 spaces to line " + (lineNumber + 1));
                  } else {
                     suggestions.add("Remove 1 space from line " + (lineNumber + 1));
                  }

                  lineNumber++;
                  continue;
               }
            } else {
               if (currentLineIndent > prevLineIndent) {
                  suggestions.add("Remove " + (currentLineIndent - prevLineIndent) + " space(s) from line " + (lineNumber + 1));
                  lineNumber++;
                  continue;
               }

               if (currentLineIndent != prevLineIndent && this.removeIndent(prevLine).startsWith("-") && this.removeIndent(line).startsWith("-")) {
                  suggestions.add("Add " + (prevLineIndent - currentLineIndent) + " space(s) to line " + (lineNumber + 1));
                  lineNumber++;
                  continue;
               }
            }

            if (currentLineIndent % 2 == 1) {
               suggestions.add("Add or remove one space at line " + (lineNumber + 1));
               lineNumber++;
            }
         }
      }

      return suggestions;
   }

   private String removeSpaces(String line) {
      String fixed = line;

      while (fixed.startsWith(" ") || fixed.startsWith("\t")) {
         fixed = fixed.substring(1);
      }

      while (fixed.endsWith(" ") || fixed.endsWith("\t")) {
         fixed = fixed.substring(0, fixed.length() - 1);
      }

      return fixed;
   }

   private boolean isComment(String line) {
      String[] array = line.split("#");
      return array.length == 0 ? true : array[0].replace(" ", "").isEmpty();
   }
}
