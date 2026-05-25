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

public interface KeyPipelineBinaryCommands {
   Response<Boolean> exists(byte[] var1);

   Response<Long> exists(byte[]... var1);

   Response<Long> persist(byte[] var1);

   Response<String> type(byte[] var1);

   Response<byte[]> dump(byte[] var1);

   Response<String> restore(byte[] var1, long var2, byte[] var4);

   Response<String> restore(byte[] var1, long var2, byte[] var4, RestoreParams var5);

   Response<Long> expire(byte[] var1, long var2);

   Response<Long> expire(byte[] var1, long var2, ExpiryOption var4);

   Response<Long> pexpire(byte[] var1, long var2);

   Response<Long> pexpire(byte[] var1, long var2, ExpiryOption var4);

   Response<Long> expireTime(byte[] var1);

   Response<Long> pexpireTime(byte[] var1);

   Response<Long> expireAt(byte[] var1, long var2);

   Response<Long> expireAt(byte[] var1, long var2, ExpiryOption var4);

   Response<Long> pexpireAt(byte[] var1, long var2);

   Response<Long> pexpireAt(byte[] var1, long var2, ExpiryOption var4);

   Response<Long> ttl(byte[] var1);

   Response<Long> pttl(byte[] var1);

   Response<Long> touch(byte[] var1);

   Response<Long> touch(byte[]... var1);

   Response<List<byte[]>> sort(byte[] var1);

   Response<List<byte[]>> sort(byte[] var1, SortingParams var2);

   Response<List<byte[]>> sortReadonly(byte[] var1, SortingParams var2);

   Response<Long> del(byte[] var1);

   Response<Long> del(byte[]... var1);

   Response<Long> unlink(byte[] var1);

   Response<Long> unlink(byte[]... var1);

   Response<Boolean> copy(byte[] var1, byte[] var2, boolean var3);

   Response<String> rename(byte[] var1, byte[] var2);

   Response<Long> renamenx(byte[] var1, byte[] var2);

   Response<Long> sort(byte[] var1, SortingParams var2, byte[] var3);

   Response<Long> sort(byte[] var1, byte[] var2);

   Response<Long> memoryUsage(byte[] var1);

   Response<Long> memoryUsage(byte[] var1, int var2);

   Response<Long> objectRefcount(byte[] var1);

   Response<byte[]> objectEncoding(byte[] var1);

   Response<Long> objectIdletime(byte[] var1);

   Response<Long> objectFreq(byte[] var1);

   Response<String> migrate(String var1, int var2, byte[] var3, int var4);

   Response<String> migrate(String var1, int var2, int var3, MigrateParams var4, byte[]... var5);

   Response<Set<byte[]>> keys(byte[] var1);

   Response<ScanResult<byte[]>> scan(byte[] var1);

   Response<ScanResult<byte[]>> scan(byte[] var1, ScanParams var2);

   Response<ScanResult<byte[]>> scan(byte[] var1, ScanParams var2, byte[] var3);

   Response<byte[]> randomBinaryKey();
}
