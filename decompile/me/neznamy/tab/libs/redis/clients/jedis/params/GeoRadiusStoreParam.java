package me.neznamy.tab.libs.redis.clients.jedis.params;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class GeoRadiusStoreParam implements IParams {
   private boolean store = false;
   private boolean storeDist = false;
   private String key;

   public static GeoRadiusStoreParam geoRadiusStoreParam() {
      return new GeoRadiusStoreParam();
   }

   public GeoRadiusStoreParam store(String key) {
      if (key != null) {
         this.store = true;
         this.key = key;
      }

      return this;
   }

   public GeoRadiusStoreParam storeDist(String key) {
      if (key != null) {
         this.storeDist = true;
         this.key = key;
      }

      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.storeDist) {
         args.add(Protocol.Keyword.STOREDIST).key(this.key);
      } else {
         if (!this.store) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " must has store or storedist option");
         }

         args.add(Protocol.Keyword.STORE).key(this.key);
      }
   }
}
