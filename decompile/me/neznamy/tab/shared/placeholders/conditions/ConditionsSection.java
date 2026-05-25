package me.neznamy.tab.shared.placeholders.conditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import org.jetbrains.annotations.NotNull;

public class ConditionsSection {
   @NotNull
   private final Map<String, ConditionsSection.ConditionDefinition> conditions;

   @NotNull
   public static ConditionsSection fromSection(@NotNull ConfigurationSection section) {
      Map<String, ConditionsSection.ConditionDefinition> conditions = new HashMap<>();

      for (Object condition : section.getKeys()) {
         conditions.put(
            condition.toString(),
            ConditionsSection.ConditionDefinition.fromSection(section.getConfigurationSection(condition.toString()), condition.toString())
         );
      }

      return new ConditionsSection(conditions);
   }

   @NotNull
   @Generated
   public Map<String, ConditionsSection.ConditionDefinition> getConditions() {
      return this.conditions;
   }

   @Generated
   public ConditionsSection(@NotNull Map<String, ConditionsSection.ConditionDefinition> conditions) {
      if (conditions == null) {
         throw new NullPointerException("conditions is marked non-null but is null");
      }

      this.conditions = conditions;
   }

   public static class ConditionDefinition {
      @NotNull
      private final String name;
      @NotNull
      private final List<ConditionalExpression> conditions;
      private final boolean type;
      @NotNull
      private final String yes;
      @NotNull
      private final String no;

      @NotNull
      public static ConditionsSection.ConditionDefinition fromSection(@NotNull ConfigurationSection section, @NotNull String name) {
         section.checkForUnknownKey(Arrays.asList("conditions", "type", "true", "false"));
         List<String> list = section.getStringList("conditions");
         if (list == null) {
            section.startupWarn("Condition \"" + name + "\" is missing \"conditions\" section.");
            list = Collections.emptyList();
         }

         String type = section.getString("type");
         Object yes = section.getObject("true");
         if (yes == null) {
            yes = "true";
         }

         Object no = section.getObject("false");
         if (no == null) {
            no = "false";
         }

         if (list.size() >= 2 && type == null) {
            section.startupWarn(String.format("Condition \"%s\" has multiple conditions defined, but is missing \"type\" attribute. Using AND.", name));
         }

         List<ConditionalExpression> expressions = list.stream().map(expressionString -> {
            ConditionalExpression expression = ConditionalExpression.compile(expressionString.trim());
            if (expression == null) {
               TAB.getInstance().getConfigHelper().startup().startupWarn("Line \"" + expressionString + "\" is not a valid conditional expression.");
            }

            return expression;
         }).filter(Objects::nonNull).collect(Collectors.toList());
         return new ConditionsSection.ConditionDefinition(name, expressions, !"OR".equals(type), yes.toString(), no.toString());
      }

      @NotNull
      @Generated
      public String getName() {
         return this.name;
      }

      @NotNull
      @Generated
      public List<ConditionalExpression> getConditions() {
         return this.conditions;
      }

      @Generated
      public boolean isType() {
         return this.type;
      }

      @NotNull
      @Generated
      public String getYes() {
         return this.yes;
      }

      @NotNull
      @Generated
      public String getNo() {
         return this.no;
      }

      @Generated
      public ConditionDefinition(@NotNull String name, @NotNull List<ConditionalExpression> conditions, boolean type, @NotNull String yes, @NotNull String no) {
         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         }

         if (conditions == null) {
            throw new NullPointerException("conditions is marked non-null but is null");
         }

         if (yes == null) {
            throw new NullPointerException("yes is marked non-null but is null");
         }

         if (no == null) {
            throw new NullPointerException("no is marked non-null but is null");
         }

         this.name = name;
         this.conditions = conditions;
         this.type = type;
         this.yes = yes;
         this.no = no;
      }
   }
}
