package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

import java.util.StringJoiner;

public class ValueNode implements Node {
   private final Value[] values;
   private final String field;
   private final String joinString;

   public ValueNode(String field, String joinstr, Value... values) {
      this.field = field;
      this.values = values;
      this.joinString = joinstr;
   }

   private static Value[] fromStrings(String[] values) {
      Value[] objs = new Value[values.length];

      for (int i = 0; i < values.length; i++) {
         objs[i] = Values.value(values[i]);
      }

      return objs;
   }

   public ValueNode(String field, String joinstr, String... values) {
      this(field, joinstr, fromStrings(values));
   }

   private String formatField() {
      return this.field != null && !this.field.isEmpty() ? '@' + this.field + ':' : "";
   }

   private String toStringCombinable(Node.Parenthesize mode) {
      StringBuilder sb = new StringBuilder(this.formatField());
      if (this.values.length > 1 || mode == Node.Parenthesize.ALWAYS) {
         sb.append('(');
      }

      StringJoiner sj = new StringJoiner(this.joinString);

      for (Value v : this.values) {
         sj.add(v.toString());
      }

      sb.append(sj.toString());
      if (this.values.length > 1 || mode == Node.Parenthesize.ALWAYS) {
         sb.append(')');
      }

      return sb.toString();
   }

   private String toStringDefault(Node.Parenthesize mode) {
      boolean useParen = mode == Node.Parenthesize.ALWAYS;
      if (!useParen) {
         useParen = mode != Node.Parenthesize.NEVER && this.values.length > 1;
      }

      StringBuilder sb = new StringBuilder();
      if (useParen) {
         sb.append('(');
      }

      StringJoiner sj = new StringJoiner(this.joinString);

      for (Value v : this.values) {
         sj.add(this.formatField() + v.toString());
      }

      sb.append(sj.toString());
      if (useParen) {
         sb.append(')');
      }

      return sb.toString();
   }

   @Override
   public String toString(Node.Parenthesize mode) {
      return this.values[0].isCombinable() ? this.toStringCombinable(mode) : this.toStringDefault(mode);
   }
}
