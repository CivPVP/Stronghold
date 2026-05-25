package me.neznamy.tab.libs.redis.clients.jedis;

public class GeoCoordinate {
   private double longitude;
   private double latitude;

   public GeoCoordinate(double longitude, double latitude) {
      this.longitude = longitude;
      this.latitude = latitude;
   }

   public double getLongitude() {
      return this.longitude;
   }

   public double getLatitude() {
      return this.latitude;
   }

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      }

      if (o == this) {
         return true;
      }

      if (!(o instanceof GeoCoordinate)) {
         return false;
      }

      GeoCoordinate that = (GeoCoordinate)o;
      return Double.compare(that.longitude, this.longitude) != 0 ? false : Double.compare(that.latitude, this.latitude) == 0;
   }

   @Override
   public int hashCode() {
      long temp = Double.doubleToLongBits(this.longitude);
      int result = (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.latitude);
      return 31 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public String toString() {
      return "(" + this.longitude + "," + this.latitude + ")";
   }
}
