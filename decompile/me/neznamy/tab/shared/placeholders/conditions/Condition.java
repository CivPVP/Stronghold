package me.neznamy.tab.shared.placeholders.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Generated;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.conditions.expression.ComparatorExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import me.neznamy.tab.shared.placeholders.conditions.expression.NotPermission;
import me.neznamy.tab.shared.placeholders.conditions.expression.Permission;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class Condition {
   @NotNull
   private final String name;
   @NotNull
   protected final List<ConditionalExpression> expressions;
   private final boolean type;
   @NotNull
   private final String yes;
   @NotNull
   private final String no;
   private int refresh = -1;
   @NotNull
   private final List<String> placeholdersInConditions = new ArrayList<>();

   public Condition(@NotNull ConditionsSection.ConditionDefinition definition) {
      this.type = definition.isType();
      this.name = definition.getName();
      this.expressions = new ArrayList<>(definition.getConditions());
      this.yes = definition.getYes();
      this.no = definition.getNo();
      this.analyzeContent();
   }

   public Condition(@NotNull String shortFormat) {
      this.name = "AnonymousCondition[" + shortFormat + "]";
      this.yes = "true";
      this.no = "false";
      List<String> conditions;
      if (shortFormat.contains(";")) {
         this.type = true;
         conditions = Arrays.asList(shortFormat.split(";"));
      } else {
         this.type = false;
         conditions = this.splitString(shortFormat);
      }

      this.expressions = conditions.stream().map(expressionString -> {
         ConditionalExpression expression = ConditionalExpression.compile(expressionString.trim());
         if (expression == null) {
            TAB.getInstance().getConfigHelper().startup().startupWarn("Line \"" + expressionString + "\" is not a valid conditional expression.");
         }

         return expression;
      }).filter(Objects::nonNull).collect(Collectors.toList());
      this.analyzeContent();
   }

   private void analyzeContent() {
      for (ConditionalExpression expression : this.expressions) {
         if (!(expression instanceof Permission) && !(expression instanceof NotPermission)) {
            ComparatorExpression comparator = (ComparatorExpression)expression;
            this.placeholdersInConditions.addAll(Arrays.asList(comparator.getLeftSidePlaceholders()));
            this.placeholdersInConditions.addAll(Arrays.asList(comparator.getRightSidePlaceholders()));
         } else {
            int permissionRefresh = TAB.getInstance().getConfiguration().getConfig().getPermissionRefreshInterval();
            if (this.refresh > permissionRefresh || this.refresh == -1) {
               this.refresh = permissionRefresh;
            }
         }
      }
   }

   private List<String> splitString(@NotNull String input) {
      List<String> result = new ArrayList<>();
      Pattern pattern = Pattern.compile("(?<!-)[|](?!-)");
      Matcher matcher = pattern.matcher(input);

      int start;
      for (start = 0; matcher.find(); start = matcher.end()) {
         int end = matcher.start();
         result.add(input.substring(start, end));
      }

      result.add(input.substring(start));
      return result;
   }

   public void finishSetup() {
      for (String placeholder : this.placeholdersInConditions) {
         TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder).addParent(TabConstants.Placeholder.condition(this.name));
         Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder);
         if (pl.getRefresh() < this.refresh && pl.getRefresh() != -1) {
            this.refresh = pl.getRefresh();
         }
      }

      TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(this.placeholdersInConditions);
   }

   public String getText(TabPlayer p) {
      return this.isMet(p) ? this.yes : this.no;
   }

   public boolean isMet(TabPlayer p) {
      if (this.type) {
         for (ConditionalExpression condition : this.expressions) {
            if (!condition.isMet(p)) {
               return false;
            }
         }

         return true;
      } else {
         for (ConditionalExpression condition : this.expressions) {
            if (condition.isMet(p)) {
               return true;
            }
         }

         return false;
      }
   }

   @NotNull
   public Condition invert() {
      return new Condition(
         new ConditionsSection.ConditionDefinition(
            "inverted:" + this.name, this.expressions.stream().map(ConditionalExpression::invert).collect(Collectors.toList()), !this.type, this.yes, this.no
         )
      );
   }

   @NotNull
   public String toShortFormat() {
      return this.expressions.stream().map(ConditionalExpression::toShortFormat).collect(Collectors.joining(this.type ? ";" : "|"));
   }

   @Generated
   public Condition(@NotNull String name, @NotNull List<ConditionalExpression> expressions, boolean type, @NotNull String yes, @NotNull String no) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (expressions == null) {
         throw new NullPointerException("expressions is marked non-null but is null");
      }

      if (yes == null) {
         throw new NullPointerException("yes is marked non-null but is null");
      }

      if (no == null) {
         throw new NullPointerException("no is marked non-null but is null");
      }

      this.name = name;
      this.expressions = expressions;
      this.type = type;
      this.yes = yes;
      this.no = no;
   }

   @NotNull
   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getRefresh() {
      return this.refresh;
   }
}
