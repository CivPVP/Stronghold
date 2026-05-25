package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Arrays;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.util.ByteArrayComparator;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class Tuple implements Comparable<Tuple> {
   private byte[] element;
   private Double score;

   public Tuple(String element, Double score) {
      this(SafeEncoder.encode(element), score);
   }

   public Tuple(byte[] element, Double score) {
      this.element = element;
      this.score = score;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result;
      if (null != this.element) {
         for (byte b : this.element) {
            result = 31 * result + b;
         }
      }

      long temp = Double.doubleToLongBits(this.score);
      return 31 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj == this) {
         return true;
      }

      if (!(obj instanceof Tuple)) {
         return false;
      }

      Tuple other = (Tuple)obj;
      return !Arrays.equals(this.element, other.element) ? false : Objects.equals(this.score, other.score);
   }

   public int compareTo(Tuple other) {
      return compare(this, other);
   }

   public static int compare(Tuple t1, Tuple t2) {
      int compScore = Double.compare(t1.score, t2.score);
      return compScore != 0 ? compScore : ByteArrayComparator.compare(t1.element, t2.element);
   }

   public String getElement() {
      return null != this.element ? SafeEncoder.encode(this.element) : null;
   }

   public byte[] getBinaryElement() {
      return this.element;
   }

   public double getScore() {
      return this.score;
   }

   @Override
   public String toString() {
      return '[' + SafeEncoder.encode(this.element) + ',' + this.score + ']';
   }
}
