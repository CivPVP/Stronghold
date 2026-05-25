package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFReserveParams;

public interface CuckooFilterPipelineCommands {
   Response<String> cfReserve(String var1, long var2);

   Response<String> cfReserve(String var1, long var2, CFReserveParams var4);

   Response<Boolean> cfAdd(String var1, String var2);

   Response<Boolean> cfAddNx(String var1, String var2);

   Response<List<Boolean>> cfInsert(String var1, String... var2);

   Response<List<Boolean>> cfInsert(String var1, CFInsertParams var2, String... var3);

   Response<List<Boolean>> cfInsertNx(String var1, String... var2);

   Response<List<Boolean>> cfInsertNx(String var1, CFInsertParams var2, String... var3);

   Response<Boolean> cfExists(String var1, String var2);

   Response<List<Boolean>> cfMExists(String var1, String... var2);

   Response<Boolean> cfDel(String var1, String var2);

   Response<Long> cfCount(String var1, String var2);

   Response<Entry<Long, byte[]>> cfScanDump(String var1, long var2);

   Response<String> cfLoadChunk(String var1, long var2, byte[] var4);

   Response<Map<String, Object>> cfInfo(String var1);
}
