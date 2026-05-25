package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Arrays;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.GeoCoordinate;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class GeoRadiusResponse {
   private byte[] member;
   private double distance;
   private GeoCoordinate coordinate;
   private long rawScore;

   public GeoRadiusResponse(byte[] member) {
      this.member = member;
   }

   public void setDistance(double distance) {
      this.distance = distance;
   }

   public void setCoordinate(GeoCoordinate coordinate) {
      this.coordinate = coordinate;
   }

   public void setRawScore(long rawScore) {
      this.rawScore = rawScore;
   }

   public byte[] getMember() {
      return this.member;
   }

   public String getMemberByString() {
      return SafeEncoder.encode(this.member);
   }

   public double getDistance() {
      return this.distance;
   }

   public GeoCoordinate getCoordinate() {
      return this.coordinate;
   }

   public long getRawScore() {
      return this.rawScore;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof GeoRadiusResponse)) {
         return false;
      }

      GeoRadiusResponse response = (GeoRadiusResponse)obj;
      return Double.compare(this.distance, response.getDistance()) == 0
         && this.rawScore == response.getRawScore()
         && this.coordinate.equals(response.coordinate)
         && Arrays.equals(this.member, response.getMember());
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 67 * hash + Arrays.hashCode(this.member);
      hash = 67 * hash + (int)(Double.doubleToLongBits(this.distance) ^ Double.doubleToLongBits(this.distance) >>> 32);
      hash = 67 * hash + Objects.hashCode(this.coordinate);
      return 67 * hash + (int)(this.rawScore ^ this.rawScore >>> 32);
   }
}
