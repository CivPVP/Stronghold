package me.neznamy.tab.libs.redis.clients.jedis.params;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.GeoCoordinate;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortingOrder;

public class GeoSearchParam implements IParams {
   private boolean fromMember = false;
   private boolean fromLonLat = false;
   private String member;
   private GeoCoordinate coord;
   private boolean byRadius = false;
   private boolean byBox = false;
   private double radius;
   private double width;
   private double height;
   private GeoUnit unit;
   private boolean withCoord = false;
   private boolean withDist = false;
   private boolean withHash = false;
   private Integer count = null;
   private boolean any = false;
   private SortingOrder sortingOrder = null;

   public static GeoSearchParam geoSearchParam() {
      return new GeoSearchParam();
   }

   public GeoSearchParam fromMember(String member) {
      this.fromMember = true;
      this.member = member;
      return this;
   }

   public GeoSearchParam fromLonLat(double longitude, double latitude) {
      this.fromLonLat = true;
      this.coord = new GeoCoordinate(longitude, latitude);
      return this;
   }

   public GeoSearchParam fromLonLat(GeoCoordinate coord) {
      this.fromLonLat = true;
      this.coord = coord;
      return this;
   }

   public GeoSearchParam byRadius(double radius, GeoUnit unit) {
      this.byRadius = true;
      this.radius = radius;
      this.unit = unit;
      return this;
   }

   public GeoSearchParam byBox(double width, double height, GeoUnit unit) {
      this.byBox = true;
      this.width = width;
      this.height = height;
      this.unit = unit;
      return this;
   }

   public GeoSearchParam withCoord() {
      this.withCoord = true;
      return this;
   }

   public GeoSearchParam withDist() {
      this.withDist = true;
      return this;
   }

   public GeoSearchParam withHash() {
      this.withHash = true;
      return this;
   }

   public GeoSearchParam asc() {
      return this.sortingOrder(SortingOrder.ASC);
   }

   public GeoSearchParam desc() {
      return this.sortingOrder(SortingOrder.DESC);
   }

   public GeoSearchParam sortingOrder(SortingOrder order) {
      this.sortingOrder = order;
      return this;
   }

   public GeoSearchParam count(int count) {
      this.count = count;
      return this;
   }

   public GeoSearchParam count(int count, boolean any) {
      this.count = count;
      this.any = true;
      return this;
   }

   public GeoSearchParam any() {
      if (this.count == null) {
         throw new IllegalArgumentException("COUNT must be set before ANY to be set");
      }

      this.any = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.fromMember && this.fromLonLat) {
         throw new IllegalArgumentException("Both FROMMEMBER and FROMLONLAT cannot be used.");
      }

      if (this.fromMember) {
         args.add(Protocol.Keyword.FROMMEMBER).add(this.member);
      } else {
         if (!this.fromLonLat) {
            throw new IllegalArgumentException("Either FROMMEMBER or FROMLONLAT must be used.");
         }

         args.add(Protocol.Keyword.FROMLONLAT).add(this.coord.getLongitude()).add(this.coord.getLatitude());
      }

      if (this.byRadius && this.byBox) {
         throw new IllegalArgumentException("Both BYRADIUS and BYBOX cannot be used.");
      }

      if (this.byRadius) {
         args.add(Protocol.Keyword.BYRADIUS).add(this.radius).add(this.unit);
      } else {
         if (!this.byBox) {
            throw new IllegalArgumentException("Either BYRADIUS or BYBOX must be used.");
         }

         args.add(Protocol.Keyword.BYBOX).add(this.width).add(this.height).add(this.unit);
      }

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
