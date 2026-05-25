package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class GetExParams implements IParams {
   private Protocol.Keyword expiration;
   private Long expirationValue;
   private boolean persist;

   public static GetExParams getExParams() {
      return new GetExParams();
   }

   private GetExParams expiration(Protocol.Keyword type, Long value) {
      this.expiration = type;
      this.expirationValue = value;
      return this;
   }

   public GetExParams ex(long secondsToExpire) {
      return this.expiration(Protocol.Keyword.EX, secondsToExpire);
   }

   public GetExParams px(long millisecondsToExpire) {
      return this.expiration(Protocol.Keyword.PX, millisecondsToExpire);
   }

   public GetExParams exAt(long seconds) {
      return this.expiration(Protocol.Keyword.EXAT, seconds);
   }

   public GetExParams pxAt(long milliseconds) {
      return this.expiration(Protocol.Keyword.PXAT, milliseconds);
   }

   public GetExParams persist() {
      return this.expiration(Protocol.Keyword.PERSIST, null);
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.expiration != null) {
         args.add(this.expiration);
         if (this.expirationValue != null) {
            args.add(this.expirationValue);
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         GetExParams that = (GetExParams)o;
         return this.persist == that.persist && this.expiration == that.expiration && Objects.equals(this.expirationValue, that.expirationValue);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.expiration, this.expirationValue, this.persist);
   }
}
