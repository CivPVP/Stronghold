package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.BFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.BFReserveParams;

public interface BloomFilterCommands {
   String bfReserve(String var1, double var2, long var4);

   String bfReserve(String var1, double var2, long var4, BFReserveParams var6);

   boolean bfAdd(String var1, String var2);

   List<Boolean> bfMAdd(String var1, String... var2);

   List<Boolean> bfInsert(String var1, String... var2);

   List<Boolean> bfInsert(String var1, BFInsertParams var2, String... var3);

   boolean bfExists(String var1, String var2);

   List<Boolean> bfMExists(String var1, String... var2);

   Entry<Long, byte[]> bfScanDump(String var1, long var2);

   String bfLoadChunk(String var1, long var2, byte[] var4);

   long bfCard(String var1);

   Map<String, Object> bfInfo(String var1);
}
