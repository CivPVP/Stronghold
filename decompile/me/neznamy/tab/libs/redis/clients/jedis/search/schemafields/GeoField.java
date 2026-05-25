package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;

public class GeoField extends SchemaField {
   private boolean indexMissing;
   private boolean sortable;
   private boolean noIndex;

   public GeoField(String fieldName) {
      super(fieldName);
   }

   public GeoField(FieldName fieldName) {
      super(fieldName);
   }

   public static GeoField of(String fieldName) {
      return new GeoField(fieldName);
   }

   public static GeoField of(FieldName fieldName) {
      return new GeoField(fieldName);
   }

   public GeoField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public GeoField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public GeoField sortable() {
      this.sortable = true;
      return this;
   }

   public GeoField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add(SearchProtocol.SearchKeyword.GEO);
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
