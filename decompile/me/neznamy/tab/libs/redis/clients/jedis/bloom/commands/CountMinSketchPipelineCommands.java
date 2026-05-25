package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.Response;

public interface CountMinSketchPipelineCommands {
   Response<String> cmsInitByDim(String var1, long var2, long var4);

   Response<String> cmsInitByProb(String var1, double var2, double var4);

   Response<List<Long>> cmsIncrBy(String var1, Map<String, Long> var2);

   Response<List<Long>> cmsQuery(String var1, String... var2);

   Response<String> cmsMerge(String var1, String... var2);

   Response<String> cmsMerge(String var1, Map<String, Long> var2);

   Response<Map<String, Object>> cmsInfo(String var1);
}
