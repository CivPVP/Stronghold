package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class SetParams implements IParams {
   private Protocol.Keyword existance;
   private Protocol.Keyword expiration;
   private Long expirationValue;

   public static SetParams setParams() {
      return new SetParams();
   }

   public SetParams nx() {
      this.existance = Protocol.Keyword.NX;
      return this;
   }

   public SetParams xx() {
      this.existance = Protocol.Keyword.XX;
      return this;
   }

   private SetParams expiration(Protocol.Keyword type, Long value) {
      this.expiration = type;
      this.expirationValue = value;
      return this;
   }

   public SetParams ex(long remainingSeconds) {
      return this.expiration(Protocol.Keyword.EX, remainingSeconds);
   }

   public SetParams px(long remainingMilliseconds) {
      return this.expiration(Protocol.Keyword.PX, remainingMilliseconds);
   }

   public SetParams exAt(long timestampSeconds) {
      return this.expiration(Protocol.Keyword.EXAT, timestampSeconds);
   }

   public SetParams pxAt(long timestampMilliseconds) {
      return this.expiration(Protocol.Keyword.PXAT, timestampMilliseconds);
   }

   public SetParams keepttl() {
      return this.keepTtl();
   }

   public SetParams keepTtl() {
      return this.expiration(Protocol.Keyword.KEEPTTL, null);
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.existance != null) {
         args.add(this.existance);
      }

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
         SetParams setParams = (SetParams)o;
         return Objects.equals(this.existance, setParams.existance)
            && Objects.equals(this.expiration, setParams.expiration)
            && Objects.equals(this.expirationValue, setParams.expirationValue);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.existance, this.expiration, this.expirationValue);
   }
}
