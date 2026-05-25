package me.neznamy.tab.libs.redis.clients.jedis.json;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class JsonSetParams implements IParams {
   private boolean nx = false;
   private boolean xx = false;

   public static JsonSetParams jsonSetParams() {
      return new JsonSetParams();
   }

   public JsonSetParams nx() {
      this.nx = true;
      this.xx = false;
      return this;
   }

   public JsonSetParams xx() {
      this.nx = false;
      this.xx = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.nx) {
         args.add("NX");
      }

      if (this.xx) {
         args.add("XX");
      }
   }
}
