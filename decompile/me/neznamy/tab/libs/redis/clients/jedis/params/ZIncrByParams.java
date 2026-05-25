package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class ZIncrByParams implements IParams {
   private Protocol.Keyword existance;

   public static ZIncrByParams zIncrByParams() {
      return new ZIncrByParams();
   }

   public ZIncrByParams nx() {
      this.existance = Protocol.Keyword.NX;
      return this;
   }

   public ZIncrByParams xx() {
      this.existance = Protocol.Keyword.XX;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.existance != null) {
         args.add(this.existance);
      }

      args.add(Protocol.Keyword.INCR);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ZIncrByParams that = (ZIncrByParams)o;
         return this.existance == that.existance;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.existance);
   }
}
