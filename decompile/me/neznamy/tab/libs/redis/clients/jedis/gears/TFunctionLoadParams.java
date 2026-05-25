package me.neznamy.tab.libs.redis.clients.jedis.gears;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

@Deprecated
public class TFunctionLoadParams implements IParams {
   private boolean replace = false;
   private String config;

   public static TFunctionLoadParams loadParams() {
      return new TFunctionLoadParams();
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.replace) {
         args.add(RedisGearsProtocol.GearsKeyword.REPLACE);
      }

      if (this.config != null && !this.config.isEmpty()) {
         args.add(RedisGearsProtocol.GearsKeyword.CONFIG).add(this.config);
      }
   }

   public TFunctionLoadParams replace() {
      this.replace = true;
      return this;
   }

   public TFunctionLoadParams config(String config) {
      this.config = config;
      return this;
   }
}
