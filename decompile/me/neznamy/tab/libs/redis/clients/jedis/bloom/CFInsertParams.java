package me.neznamy.tab.libs.redis.clients.jedis.bloom;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class CFInsertParams implements IParams {
   private Long capacity;
   private boolean noCreate = false;

   public static CFInsertParams insertParams() {
      return new CFInsertParams();
   }

   public CFInsertParams capacity(long capacity) {
      this.capacity = capacity;
      return this;
   }

   public CFInsertParams noCreate() {
      this.noCreate = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.capacity != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.CAPACITY).add(Protocol.toByteArray(this.capacity));
      }

      if (this.noCreate) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.NOCREATE);
      }
   }
}
