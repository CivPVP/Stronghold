package me.neznamy.tab.shared.placeholders.conditions.expression;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.conditions.expression.numbers.GreaterThan;
import me.neznamy.tab.shared.placeholders.conditions.expression.numbers.GreaterThanOrEqual;
import me.neznamy.tab.shared.placeholders.conditions.expression.numbers.LessThan;
import me.neznamy.tab.shared.placeholders.conditions.expression.numbers.LessThanOrEqual;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.Contains;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.EndsWith;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.Equals;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.NotContains;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.NotEndsWith;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.NotEquals;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.NotStartsWith;
import me.neznamy.tab.shared.placeholders.conditions.expression.string.StartsWith;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConditionalExpression {
   @NotNull
   private static final Map<String, Function<String, ConditionalExpression>> conditionTypes = new LinkedHashMap<>();

   @NotNull
   private static String[] splitAndTrim(@NonNull String string, @NonNull String delimiter) {
      if (string == null) {
         throw new NullPointerException("string is marked non-null but is null");
      }

      if (delimiter == null) {
         throw new NullPointerException("delimiter is marked non-null but is null");
      }

      List<String> result = new ArrayList<>();
      StringBuilder current = new StringBuilder();
      boolean insidePercentBlock = false;
      int i = 0;
      int len = string.length();
      int delimiterLength = delimiter.length();

      while (i < len) {
         if (string.charAt(i) == '%') {
            insidePercentBlock = !insidePercentBlock;
            current.append('%');
            i++;
         } else if (!insidePercentBlock && i + delimiterLength <= len && string.regionMatches(i, delimiter, 0, delimiterLength)) {
            result.add(current.toString().trim());
            current.setLength(0);
            i += delimiterLength;
         } else {
            current.append(string.charAt(i));
            i++;
         }
      }

      result.add(current.toString().trim());
      return result.toArray(new String[0]);
   }

   @Nullable
   public static ConditionalExpression compile(@NonNull String pattern) {
      if (pattern == null) {
         throw new NullPointerException("pattern is marked non-null but is null");
      }

      String noPlaceholders = pattern;

      for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(pattern)) {
         noPlaceholders = noPlaceholders.replace(placeholder, "");
      }

      for (Entry<String, Function<String, ConditionalExpression>> entry : conditionTypes.entrySet()) {
         if (noPlaceholders.contains(entry.getKey())) {
            return entry.getValue().apply(pattern);
         }
      }

      return null;
   }

   public abstract boolean isMet(@NonNull TabPlayer var1);

   @NotNull
   public abstract ConditionalExpression invert();

   @NotNull
   public abstract String toShortFormat();

   @NotNull
   @Generated
   public static Map<String, Function<String, ConditionalExpression>> getConditionTypes() {
      return conditionTypes;
   }

   static {
      conditionTypes.put(">=", line -> new GreaterThanOrEqual(splitAndTrim(line, ">=")));
      conditionTypes.put(">", line -> new GreaterThan(splitAndTrim(line, ">")));
      conditionTypes.put("<=", line -> new LessThanOrEqual(splitAndTrim(line, "<=")));
      conditionTypes.put("!<-", line -> new NotContains(splitAndTrim(line, "!<-")));
      conditionTypes.put("!|-", line -> new NotStartsWith(splitAndTrim(line, "!|-")));
      conditionTypes.put("!-|", line -> new NotEndsWith(splitAndTrim(line, "!-|")));
      conditionTypes.put("<-", line -> new Contains(splitAndTrim(line, "<-")));
      conditionTypes.put("<", line -> new LessThan(splitAndTrim(line, "<")));
      conditionTypes.put("|-", line -> new StartsWith(splitAndTrim(line, "|-")));
      conditionTypes.put("-|", line -> new EndsWith(splitAndTrim(line, "-|")));
      conditionTypes.put("!=", line -> new NotEquals(splitAndTrim(line, "!=")));
      conditionTypes.put("=", line -> new Equals(splitAndTrim(line, "=")));
      conditionTypes.put("!permission:", line -> {
         String node = splitAndTrim(line, ":")[1];
         return new NotPermission(node);
      });
      conditionTypes.put("permission:", line -> {
         String node = splitAndTrim(line, ":")[1];
         return new Permission(node);
      });
   }
}
