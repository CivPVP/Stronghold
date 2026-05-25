package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;

public class NumericField extends SchemaField {
   private boolean indexMissing;
   private boolean sortable;
   private boolean noIndex;

   public NumericField(String fieldName) {
      super(fieldName);
   }

   public NumericField(FieldName fieldName) {
      super(fieldName);
   }

   public static NumericField of(String fieldName) {
      return new NumericField(fieldName);
   }

   public static NumericField of(FieldName fieldName) {
      return new NumericField(fieldName);
   }

   public NumericField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public NumericField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public NumericField sortable() {
      this.sortable = true;
      return this;
   }

   public NumericField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add(SearchProtocol.SearchKeyword.NUMERIC);
      if (this.indexMissing) {
         args.add(SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.sortable) {
         args.add(SearchProtocol.SearchKeyword.SORTABLE);
      }

      if (this.noIndex) {
         args.add(SearchProtocol.SearchKeyword.NOINDEX);
      }
   }
}
