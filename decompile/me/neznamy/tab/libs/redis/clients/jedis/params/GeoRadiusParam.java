package me.neznamy.tab.libs.redis.clients.jedis.params;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortingOrder;

public class GeoRadiusParam implements IParams {
   private boolean withCoord = false;
   private boolean withDist = false;
   private boolean withHash = false;
   private Integer count = null;
   private boolean any = false;
   private SortingOrder sortingOrder = null;

   public static GeoRadiusParam geoRadiusParam() {
      return new GeoRadiusParam();
   }

   public GeoRadiusParam withCoord() {
      this.withCoord = true;
      return this;
   }

   public GeoRadiusParam withDist() {
      this.withDist = true;
      return this;
   }

   public GeoRadiusParam withHash() {
      this.withHash = true;
      return this;
   }

   public GeoRadiusParam sortAscending() {
      return this.sortingOrder(SortingOrder.ASC);
   }

   public GeoRadiusParam sortDescending() {
      return this.sortingOrder(SortingOrder.DESC);
   }

   public GeoRadiusParam sortingOrder(SortingOrder order) {
      this.sortingOrder = order;
      return this;
   }

   public GeoRadiusParam count(int count) {
      this.count = count;
      return this;
   }

   public GeoRadiusParam count(int count, boolean any) {
      this.count = count;
      this.any = any;
      return this;
   }

   public GeoRadiusParam any() {
      if (this.count == null) {
         throw new IllegalArgumentException("COUNT must be set before ANY to be set");
      }

      this.any = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.withCoord) {
         args.add(Protocol.Keyword.WITHCOORD);
      }

      if (this.withDist) {
         args.add(Protocol.Keyword.WITHDIST);
      }

      if (this.withHash) {
         args.add(Protocol.Keyword.WITHHASH);
      }

      if (this.count != null) {
         args.add(Protocol.Keyword.COUNT).add(this.count);
         if (this.any) {
            args.add(Protocol.Keyword.ANY);
         }
      }

      if (this.sortingOrder != null) {
         args.add(this.sortingOrder);
      }
   }
}
