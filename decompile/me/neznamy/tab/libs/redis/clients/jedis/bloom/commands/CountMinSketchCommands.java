package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface CountMinSketchCommands {
   String cmsInitByDim(String var1, long var2, long var4);

   String cmsInitByProb(String var1, double var2, double var4);

   default long cmsIncrBy(String key, String item, long increment) {
      return this.cmsIncrBy(key, Collections.singletonMap(item, increment)).get(0);
   }

   List<Long> cmsIncrBy(String var1, Map<String, Long> var2);

   List<Long> cmsQuery(String var1, String... var2);

   String cmsMerge(String var1, String... var2);

   String cmsMerge(String var1, Map<String, Long> var2);

   Map<String, Object> cmsInfo(String var1);
}
