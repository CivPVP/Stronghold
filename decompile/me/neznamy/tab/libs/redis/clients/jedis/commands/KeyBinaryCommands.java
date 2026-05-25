package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.RestoreParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SortingParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface KeyBinaryCommands {
   boolean exists(byte[] var1);

   long exists(byte[]... var1);

   long persist(byte[] var1);

   String type(byte[] var1);

   byte[] dump(byte[] var1);

   String restore(byte[] var1, long var2, byte[] var4);

   String restore(byte[] var1, long var2, byte[] var4, RestoreParams var5);

   long expire(byte[] var1, long var2);

   long expire(byte[] var1, long var2, ExpiryOption var4);

   long pexpire(byte[] var1, long var2);

   long pexpire(byte[] var1, long var2, ExpiryOption var4);

   long expireTime(byte[] var1);

   long pexpireTime(byte[] var1);

   long expireAt(byte[] var1, long var2);

   long expireAt(byte[] var1, long var2, ExpiryOption var4);

   long pexpireAt(byte[] var1, long var2);

   long pexpireAt(byte[] var1, long var2, ExpiryOption var4);

   long ttl(byte[] var1);

   long pttl(byte[] var1);

   long touch(byte[] var1);

   long touch(byte[]... var1);

   List<byte[]> sort(byte[] var1);

   List<byte[]> sort(byte[] var1, SortingParams var2);

   long del(byte[] var1);

   long del(byte[]... var1);

   long unlink(byte[] var1);

   long unlink(byte[]... var1);

   boolean copy(byte[] var1, byte[] var2, boolean var3);

   String rename(byte[] var1, byte[] var2);

   long renamenx(byte[] var1, byte[] var2);

   long sort(byte[] var1, SortingParams var2, byte[] var3);

   long sort(byte[] var1, byte[] var2);

   List<byte[]> sortReadonly(byte[] var1, SortingParams var2);

   Long memoryUsage(byte[] var1);

   Long memoryUsage(byte[] var1, int var2);

   Long objectRefcount(byte[] var1);

   byte[] objectEncoding(byte[] var1);

   Long objectIdletime(byte[] var1);

   Long objectFreq(byte[] var1);

   String migrate(String var1, int var2, byte[] var3, int var4);

   String migrate(String var1, int var2, int var3, MigrateParams var4, byte[]... var5);

   Set<byte[]> keys(byte[] var1);

   ScanResult<byte[]> scan(byte[] var1);

   ScanResult<byte[]> scan(byte[] var1, ScanParams var2);

   ScanResult<byte[]> scan(byte[] var1, ScanParams var2, byte[] var3);

   byte[] randomBinaryKey();
}
