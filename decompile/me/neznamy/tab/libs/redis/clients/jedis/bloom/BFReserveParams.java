package me.neznamy.tab.libs.redis.clients.jedis.bloom;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class BFReserveParams implements IParams {
   private Integer expansion;
   private boolean nonScaling = false;

   public static BFReserveParams reserveParams() {
      return new BFReserveParams();
   }

   public BFReserveParams expansion(int expansion) {
      this.expansion = expansion;
      return this;
   }

   public BFReserveParams nonScaling() {
      this.nonScaling = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.expansion != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.EXPANSION).add(Protocol.toByteArray(this.expansion));
      }

      if (this.nonScaling) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.NONSCALING);
      }
   }
}
