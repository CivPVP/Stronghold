package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.RestoreParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SortingParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface KeyPipelineCommands {
   Response<Boolean> exists(String var1);

   Response<Long> exists(String... var1);

   Response<Long> persist(String var1);

   Response<String> type(String var1);

   Response<byte[]> dump(String var1);

   Response<String> restore(String var1, long var2, byte[] var4);

   Response<String> restore(String var1, long var2, byte[] var4, RestoreParams var5);

   Response<Long> expire(String var1, long var2);

   Response<Long> expire(String var1, long var2, ExpiryOption var4);

   Response<Long> pexpire(String var1, long var2);

   Response<Long> pexpire(String var1, long var2, ExpiryOption var4);

   Response<Long> expireTime(String var1);

   Response<Long> pexpireTime(String var1);

   Response<Long> expireAt(String var1, long var2);

   Response<Long> expireAt(String var1, long var2, ExpiryOption var4);

   Response<Long> pexpireAt(String var1, long var2);

   Response<Long> pexpireAt(String var1, long var2, ExpiryOption var4);

   Response<Long> ttl(String var1);

   Response<Long> pttl(String var1);

   Response<Long> touch(String var1);

   Response<Long> touch(String... var1);

   Response<List<String>> sort(String var1);

   Response<Long> sort(String var1, String var2);

   Response<List<String>> sort(String var1, SortingParams var2);

   Response<Long> sort(String var1, SortingParams var2, String var3);

   Response<List<String>> sortReadonly(String var1, SortingParams var2);

   Response<Long> del(String var1);

   Response<Long> del(String... var1);

   Response<Long> unlink(String var1);

   Response<Long> unlink(String... var1);

   Response<Boolean> copy(String var1, String var2, boolean var3);

   Response<String> rename(String var1, String var2);

   Response<Long> renamenx(String var1, String var2);

   Response<Long> memoryUsage(String var1);

   Response<Long> memoryUsage(String var1, int var2);

   Response<Long> objectRefcount(String var1);

   Response<String> objectEncoding(String var1);

   Response<Long> objectIdletime(String var1);

   Response<Long> objectFreq(String var1);

   Response<String> migrate(String var1, int var2, String var3, int var4);

   Response<String> migrate(String var1, int var2, int var3, MigrateParams var4, String... var5);

   Response<Set<String>> keys(String var1);

   Response<ScanResult<String>> scan(String var1);

   Response<ScanResult<String>> scan(String var1, ScanParams var2);

   Response<ScanResult<String>> scan(String var1, ScanParams var2, String var3);

   Response<String> randomKey();
}
