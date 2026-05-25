package me.neznamy.tab.libs.redis.clients.jedis.bloom;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class BFInsertParams implements IParams {
   private Long capacity;
   private Double errorRate;
   private Integer expansion;
   private boolean noCreate = false;
   private boolean nonScaling = false;

   public static BFInsertParams insertParams() {
      return new BFInsertParams();
   }

   public BFInsertParams capacity(long capacity) {
      this.capacity = capacity;
      return this;
   }

   public BFInsertParams error(double errorRate) {
      this.errorRate = errorRate;
      return this;
   }

   public BFInsertParams expansion(int expansion) {
      this.expansion = expansion;
      return this;
   }

   public BFInsertParams noCreate() {
      this.noCreate = true;
      return this;
   }

   public BFInsertParams nonScaling() {
      this.nonScaling = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.capacity != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.CAPACITY).add(Protocol.toByteArray(this.capacity));
      }

      if (this.errorRate != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.ERROR).add(Protocol.toByteArray(this.errorRate));
      }

      if (this.expansion != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.EXPANSION).add(Protocol.toByteArray(this.expansion));
      }

      if (this.noCreate) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.NOCREATE);
      }

      if (this.nonScaling) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.NONSCALING);
      }
   }
}
