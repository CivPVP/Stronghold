package me.neznamy.tab.libs.redis.clients.jedis.args;

import java.util.Arrays;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public final class RawableFactory {
   public static Rawable from(boolean b) {
      return from(Protocol.toByteArray(b));
   }

   public static Rawable from(int i) {
      return from(Protocol.toByteArray(i));
   }

   public static Rawable from(long l) {
      return from(Protocol.toByteArray(l));
   }

   public static Rawable from(double d) {
      return from(Protocol.toByteArray(d));
   }

   public static Rawable from(byte[] binary) {
      return new RawableFactory.Raw(binary);
   }

   public static Rawable from(String string) {
      return new RawableFactory.RawString(string);
   }

   private RawableFactory() {
      throw new InstantiationError();
   }

   public static class Raw implements Rawable {
      private final byte[] raw;

      public Raw(byte[] raw) {
         this.raw = Arrays.copyOf(raw, raw.length);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return o != null && this.getClass() == o.getClass() ? Arrays.equals(this.raw, ((RawableFactory.Raw)o).raw) : false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.raw);
      }
   }

   public static class RawString extends RawableFactory.Raw {
      public RawString(String str) {
         super(SafeEncoder.encode(str));
      }
   }
}
