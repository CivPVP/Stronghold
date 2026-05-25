package me.neznamy.tab.libs.me.neznamy.yamlassist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.BadIndentation;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.DoubleMapping;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.InvalidLine;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.InvalidList;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.MissingQuote;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.MissingSpaceBeforeValue;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.QuoteWrapRequired;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.TABIndent;
import me.neznamy.tab.libs.me.neznamy.yamlassist.types.UnknownEscape;
import me.neznamy.tab.libs.org.yaml.snakeyaml.Yaml;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;

public class YamlAssist {
   private static final Map<Class<? extends SyntaxError>, SyntaxError> registeredSyntaxErrors = new HashMap<>();

   public static List<String> getSuggestions(File file) {
      List<String> suggestions = new ArrayList<>();

      try {
         if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
         }

         if (!file.exists()) {
            throw new IllegalStateException("File does not exist");
         }

         FileInputStream input = new FileInputStream(file);

         try {
            new Yaml().load(input);
         } catch (YAMLException var6) {
            YAMLException exception = var6;

            for (SyntaxError possibleError : registeredSyntaxErrors.values()) {
               suggestions.addAll(possibleError.getSuggestions(exception, Files.readAllLines(file.toPath())));
            }
         }

         input.close();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return suggestions;
   }

   public static void registerSyntaxError(SyntaxError error) {
      registeredSyntaxErrors.put((Class<? extends SyntaxError>)error.getClass(), error);
   }

   public static <T> T getError(Class<T> clazz) {
      return (T)registeredSyntaxErrors.get(clazz);
   }

   static {
      registerSyntaxError(new DoubleMapping());
      registerSyntaxError(new InvalidList());
      registerSyntaxError(new InvalidLine());
      registerSyntaxError(new MissingQuote());
      registerSyntaxError(new MissingSpaceBeforeValue());
      registerSyntaxError(new QuoteWrapRequired());
      registerSyntaxError(new TABIndent());
      registerSyntaxError(new UnknownEscape());
      registerSyntaxError(new BadIndentation());
   }
}
