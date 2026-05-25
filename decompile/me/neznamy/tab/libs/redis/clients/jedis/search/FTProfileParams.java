package me.neznamy.tab.libs.redis.clients.jedis.search;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class FTProfileParams implements IParams {
   private boolean limited;

   public static FTProfileParams profileParams() {
      return new FTProfileParams();
   }

   public FTProfileParams limited() {
      this.limited = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.limited) {
         args.add(SearchProtocol.SearchKeyword.LIMITED);
      }
   }
}
