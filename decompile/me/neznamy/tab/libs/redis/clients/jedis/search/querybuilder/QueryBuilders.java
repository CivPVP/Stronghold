package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

import java.util.Arrays;

public class QueryBuilders {
   private QueryBuilders() {
      throw new InstantiationError("Must not instantiate this class");
   }

   public static QueryNode intersect(Node... n) {
      return new IntersectNode().add(n);
   }

   public static QueryNode intersect(String field, Value... values) {
      return new IntersectNode().add(field, values);
   }

   public static QueryNode intersect(String field, String stringValue) {
      return intersect(field, Values.value(stringValue));
   }

   public static QueryNode union(Node... n) {
      return new UnionNode().add(n);
   }

   public static QueryNode union(String field, Value... values) {
      return new UnionNode().add(field, values);
   }

   public static QueryNode union(String field, String... values) {
      return union(field, (Value[])Arrays.stream(values).map(Values::value).toArray());
   }

   public static QueryNode disjunct(Node... n) {
      return new DisjunctNode().add(n);
   }

   public static QueryNode disjunct(String field, Value... values) {
      return new DisjunctNode().add(field, values);
   }

   public static QueryNode disjunct(String field, String... values) {
      return disjunct(field, (Value[])Arrays.stream(values).map(Values::value).toArray());
   }

   public static QueryNode disjunctUnion(Node... n) {
      return new DisjunctUnionNode().add(n);
   }

   public static QueryNode disjunctUnion(String field, Value... values) {
      return new DisjunctUnionNode().add(field, values);
   }

   public static QueryNode disjunctUnion(String field, String... values) {
      return disjunctUnion(field, (Value[])Arrays.stream(values).map(Values::value).toArray());
   }

   public static QueryNode optional(Node... n) {
      return new OptionalNode().add(n);
   }

   public static QueryNode optional(String field, Value... values) {
      return new OptionalNode().add(field, values);
   }
}
