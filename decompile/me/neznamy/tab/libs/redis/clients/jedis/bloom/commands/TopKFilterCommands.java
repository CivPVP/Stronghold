package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface TopKFilterCommands {
   String topkReserve(String var1, long var2);

   String topkReserve(String var1, long var2, long var4, long var6, double var8);

   List<String> topkAdd(String var1, String... var2);

   default String topkIncrBy(String key, String item, long increment) {
      return this.topkIncrBy(key, Collections.singletonMap(item, increment)).get(0);
   }

   List<String> topkIncrBy(String var1, Map<String, Long> var2);

   List<Boolean> topkQuery(String var1, String... var2);

   List<String> topkList(String var1);

   Map<String, Long> topkListWithCount(String var1);

   Map<String, Object> topkInfo(String var1);
}
