package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public abstract class QueryNode implements Node {
   private final List<Node> children = new ArrayList<>();

   protected abstract String getJoinString();

   public QueryNode add(String field, Value... values) {
      this.children.add(new ValueNode(field, this.getJoinString(), values));
      return this;
   }

   public QueryNode add(String field, String... values) {
      this.children.add(new ValueNode(field, this.getJoinString(), values));
      return this;
   }

   public QueryNode add(String field, Collection<Value> values) {
      return this.add(field, values.toArray(new Value[0]));
   }

   public QueryNode add(Node... nodes) {
      this.children.addAll(Arrays.asList(nodes));
      return this;
   }

   protected boolean shouldParenthesize(Node.Parenthesize mode) {
      if (mode == Node.Parenthesize.ALWAYS) {
         return true;
      } else {
         return mode == Node.Parenthesize.NEVER ? false : this.children.size() > 1;
      }
   }

   @Override
   public String toString(Node.Parenthesize parenMode) {
      StringBuilder sb = new StringBuilder();
      StringJoiner sj = new StringJoiner(this.getJoinString());
      if (this.shouldParenthesize(parenMode)) {
         sb.append('(');
      }

      for (Node n : this.children) {
         sj.add(n.toString(parenMode));
      }

      sb.append(sj.toString());
      if (this.shouldParenthesize(parenMode)) {
         sb.append(')');
      }

      return sb.toString();
   }

   @Override
   public String toString() {
      return this.toString(Node.Parenthesize.DEFAULT);
   }
}
