package me.neznamy.tab.libs.redis.clients.jedis.graph.entities;

import java.util.List;
import java.util.Objects;

@Deprecated
public final class Point {
   private static final double EPSILON = 1.0E-5;
   private final double latitude;
   private final double longitude;

   public Point(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
   }

   public Point(List<Double> values) {
      if (values != null && values.size() == 2) {
         this.latitude = values.get(0);
         this.longitude = values.get(1);
      } else {
         throw new IllegalArgumentException("Point requires two doubles.");
      }
   }

   public double getLatitude() {
      return this.latitude;
   }

   public double getLongitude() {
      return this.longitude;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      }

      if (!(other instanceof Point)) {
         return false;
      }

      Point o = (Point)other;
      return Math.abs(this.latitude - o.latitude) < 1.0E-5 && Math.abs(this.longitude - o.longitude) < 1.0E-5;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.latitude, this.longitude);
   }

   @Override
   public String toString() {
      return "Point{latitude=" + this.latitude + ", longitude=" + this.longitude + "}";
   }
}
