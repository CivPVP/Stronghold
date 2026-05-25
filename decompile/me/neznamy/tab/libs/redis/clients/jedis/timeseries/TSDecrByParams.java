package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

public class TSDecrByParams extends TSArithByParams<TSDecrByParams> {
   public static TSDecrByParams decrByParams() {
      return new TSDecrByParams();
   }
}
