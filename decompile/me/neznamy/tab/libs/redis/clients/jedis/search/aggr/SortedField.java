package me.neznamy.tab.libs.redis.clients.jedis.search.aggr;

public class SortedField {
   private final String fieldName;
   private final SortedField.SortOrder sortOrder;

   public SortedField(String fieldName, SortedField.SortOrder order) {
      this.fieldName = fieldName;
      this.sortOrder = order;
   }

   public final String getOrder() {
      return this.sortOrder.toString();
   }

   public final String getField() {
      return this.fieldName;
   }

   public static SortedField asc(String field) {
      return new SortedField(field, SortedField.SortOrder.ASC);
   }

   public static SortedField desc(String field) {
      return new SortedField(field, SortedField.SortOrder.DESC);
   }

   public enum SortOrder {
      ASC,
      DESC;
   }
}
