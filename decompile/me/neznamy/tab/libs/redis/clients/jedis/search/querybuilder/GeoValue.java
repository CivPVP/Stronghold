package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

import java.util.Locale;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;

public class GeoValue extends Value {
   private final GeoUnit unit;
   private final double lon;
   private final double lat;
   private final double radius;

   public GeoValue(double lon, double lat, double radius, GeoUnit unit) {
      this.lon = lon;
      this.lat = lat;
      this.radius = radius;
      this.unit = unit;
   }

   @Override
   public String toString() {
      return "[" + this.lon + " " + this.lat + " " + this.radius + " " + this.unit.name().toLowerCase(Locale.ENGLISH) + "]";
   }

   @Override
   public boolean isCombinable() {
      return false;
   }
}
