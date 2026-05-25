package me.neznamy.tab.libs.redis.clients.jedis.bloom;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class CFReserveParams implements IParams {
   private Long bucketSize;
   private Integer maxIterations;
   private Integer expansion;

   public static CFReserveParams reserveParams() {
      return new CFReserveParams();
   }

   public CFReserveParams bucketSize(long bucketSize) {
      this.bucketSize = bucketSize;
      return this;
   }

   public CFReserveParams maxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return this;
   }

   public CFReserveParams expansion(int expansion) {
      this.expansion = expansion;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.bucketSize != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.BUCKETSIZE).add(Protocol.toByteArray(this.bucketSize));
      }

      if (this.maxIterations != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.MAXITERATIONS).add(Protocol.toByteArray(this.maxIterations));
      }

      if (this.expansion != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.EXPANSION).add(Protocol.toByteArray(this.expansion));
      }
   }
}
