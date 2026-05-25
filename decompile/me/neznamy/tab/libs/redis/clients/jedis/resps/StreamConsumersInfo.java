package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Map;

@Deprecated
public class StreamConsumersInfo extends StreamConsumerInfo {
   public StreamConsumersInfo(Map<String, Object> map) {
      super(map);
   }
}
