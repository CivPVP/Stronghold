package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

public class TSIncrByParams extends TSArithByParams<TSIncrByParams> {
   public static TSIncrByParams incrByParams() {
      return new TSIncrByParams();
   }
}
