package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.args.RawableFactory;

public class ZRangeParams implements IParams {
   private final Protocol.Keyword by;
   private final Rawable min;
   private final Rawable max;
   private boolean rev = false;
   private boolean limit = false;
   private int offset;
   private int count;

   private ZRangeParams() {
      throw new InstantiationError("Empty constructor must not be called.");
   }

   public ZRangeParams(int min, int max) {
      this.by = null;
      this.min = RawableFactory.from(min);
      this.max = RawableFactory.from(max);
   }

   public static ZRangeParams zrangeParams(int min, int max) {
      return new ZRangeParams(min, max);
   }

   public ZRangeParams(double min, double max) {
      this.by = Protocol.Keyword.BYSCORE;
      this.min = RawableFactory.from(min);
      this.max = RawableFactory.from(max);
   }

   public static ZRangeParams zrangeByScoreParams(double min, double max) {
      return new ZRangeParams(min, max);
   }

   private ZRangeParams(Protocol.Keyword by, Rawable min, Rawable max) {
      if (by != null && by != Protocol.Keyword.BYSCORE && by != Protocol.Keyword.BYLEX) {
         throw new IllegalArgumentException(by.name() + " is not a valid ZRANGE type argument.");
      }

      this.by = by;
      this.min = min;
      this.max = max;
   }

   public ZRangeParams(Protocol.Keyword by, String min, String max) {
      this(by, RawableFactory.from(min), RawableFactory.from(max));
   }

   public ZRangeParams(Protocol.Keyword by, byte[] min, byte[] max) {
      this(by, RawableFactory.from(min), RawableFactory.from(max));
   }

   public static ZRangeParams zrangeByLexParams(String min, String max) {
      return new ZRangeParams(Protocol.Keyword.BYLEX, min, max);
   }

   public static ZRangeParams zrangeByLexParams(byte[] min, byte[] max) {
      return new ZRangeParams(Protocol.Keyword.BYLEX, min, max);
   }

   public ZRangeParams rev() {
      this.rev = true;
      return this;
   }

   public ZRangeParams limit(int offset, int count) {
      this.limit = true;
      this.offset = offset;
      this.count = count;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.add(this.min).add(this.max);
      if (this.by != null) {
         args.add(this.by);
      }

      if (this.rev) {
         args.add(Protocol.Keyword.REV);
      }

      if (this.limit) {
         args.add(Protocol.Keyword.LIMIT).add(this.offset).add(this.count);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ZRangeParams that = (ZRangeParams)o;
         return this.rev == that.rev
            && this.limit == that.limit
            && this.offset == that.offset
            && this.count == that.count
            && this.by == that.by
            && Objects.equals(this.min, that.min)
            && Objects.equals(this.max, that.max);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.by, this.min, this.max, this.rev, this.limit, this.offset, this.count);
   }
}
