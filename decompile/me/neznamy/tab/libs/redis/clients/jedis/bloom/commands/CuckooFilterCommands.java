package me.neznamy.tab.libs.redis.clients.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFInsertParams;
import me.neznamy.tab.libs.redis.clients.jedis.bloom.CFReserveParams;

public interface CuckooFilterCommands {
   String cfReserve(String var1, long var2);

   String cfReserve(String var1, long var2, CFReserveParams var4);

   boolean cfAdd(String var1, String var2);

   boolean cfAddNx(String var1, String var2);

   List<Boolean> cfInsert(String var1, String... var2);

   List<Boolean> cfInsert(String var1, CFInsertParams var2, String... var3);

   List<Boolean> cfInsertNx(String var1, String... var2);

   List<Boolean> cfInsertNx(String var1, CFInsertParams var2, String... var3);

   boolean cfExists(String var1, String var2);

   List<Boolean> cfMExists(String var1, String... var2);

   boolean cfDel(String var1, String var2);

   long cfCount(String var1, String var2);

   Entry<Long, byte[]> cfScanDump(String var1, long var2);

   String cfLoadChunk(String var1, long var2, byte[] var4);

   Map<String, Object> cfInfo(String var1);
}
