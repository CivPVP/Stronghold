package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;

public class GeoShapeField extends SchemaField {
   private final GeoShapeField.CoordinateSystem system;
   private boolean indexMissing;
   private boolean noIndex;

   public GeoShapeField(String fieldName, GeoShapeField.CoordinateSystem system) {
      super(fieldName);
      this.system = system;
   }

   public GeoShapeField(FieldName fieldName, GeoShapeField.CoordinateSystem system) {
      super(fieldName);
      this.system = system;
   }

   public static GeoShapeField of(String fieldName, GeoShapeField.CoordinateSystem system) {
      return new GeoShapeField(fieldName, system);
   }

   public GeoShapeField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public GeoShapeField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public GeoShapeField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName).add(SearchProtocol.SearchKeyword.GEOSHAPE).add(this.system);
      if (this.indexMissing) {
         args.add(SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.noIndex) {
         args.add(SearchProtocol.SearchKeyword.NOINDEX);
      }
   }

   public enum CoordinateSystem {
      FLAT,
      SPHERICAL;
   }
}
