package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitCountOption;

public class BitPosParams implements IParams {
   private Long start;
   private Long end;
   private BitCountOption modifier;

   public BitPosParams() {
   }

   public BitPosParams(long start) {
      this.start = start;
   }

   public BitPosParams(long start, long end) {
      this(start);
      this.end = end;
   }

   public static BitPosParams bitPosParams() {
      return new BitPosParams();
   }

   public BitPosParams start(long start) {
      this.start = start;
      return this;
   }

   public BitPosParams end(long end) {
      this.end = end;
      return this;
   }

   public BitPosParams modifier(BitCountOption modifier) {
      this.modifier = modifier;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.start != null) {
         args.add(this.start);
         if (this.end != null) {
            args.add(this.end);
            if (this.modifier != null) {
               args.add(this.modifier);
            }
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BitPosParams that = (BitPosParams)o;
         return Objects.equals(this.start, that.start) && Objects.equals(this.end, that.end) && Objects.equals(this.modifier, that.modifier);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.start, this.end, this.modifier);
   }
}
