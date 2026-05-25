package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.Response;

public interface TopKFilterPipelineCommands {
   Response<String> topkReserve(String var1, long var2);

   Response<String> topkReserve(String var1, long var2, long var4, long var6, double var8);

   Response<List<String>> topkAdd(String var1, String... var2);

   Response<List<String>> topkIncrBy(String var1, Map<String, Long> var2);

   Response<List<Boolean>> topkQuery(String var1, String... var2);

   Response<List<String>> topkList(String var1);

   Response<Map<String, Long>> topkListWithCount(String var1);

   Response<Map<String, Object>> topkInfo(String var1);
}
