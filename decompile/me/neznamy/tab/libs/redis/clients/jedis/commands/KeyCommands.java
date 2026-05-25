package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.RestoreParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.SortingParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface KeyCommands {
   boolean exists(String var1);

   long exists(String... var1);

   long persist(String var1);

   String type(String var1);

   byte[] dump(String var1);

   String restore(String var1, long var2, byte[] var4);

   String restore(String var1, long var2, byte[] var4, RestoreParams var5);

   long expire(String var1, long var2);

   long expire(String var1, long var2, ExpiryOption var4);

   long pexpire(String var1, long var2);

   long pexpire(String var1, long var2, ExpiryOption var4);

   long expireTime(String var1);

   long pexpireTime(String var1);

   long expireAt(String var1, long var2);

   long expireAt(String var1, long var2, ExpiryOption var4);

   long pexpireAt(String var1, long var2);

   long pexpireAt(String var1, long var2, ExpiryOption var4);

   long ttl(String var1);

   long pttl(String var1);

   long touch(String var1);

   long touch(String... var1);

   List<String> sort(String var1);

   long sort(String var1, String var2);

   List<String> sort(String var1, SortingParams var2);

   long sort(String var1, SortingParams var2, String var3);

   List<String> sortReadonly(String var1, SortingParams var2);

   long del(String var1);

   long del(String... var1);

   long unlink(String var1);

   long unlink(String... var1);

   boolean copy(String var1, String var2, boolean var3);

   String rename(String var1, String var2);

   long renamenx(String var1, String var2);

   Long memoryUsage(String var1);

   Long memoryUsage(String var1, int var2);

   Long objectRefcount(String var1);

   String objectEncoding(String var1);

   Long objectIdletime(String var1);

   Long objectFreq(String var1);

   String migrate(String var1, int var2, String var3, int var4);

   String migrate(String var1, int var2, int var3, MigrateParams var4, String... var5);

   Set<String> keys(String var1);

   ScanResult<String> scan(String var1);

   ScanResult<String> scan(String var1, ScanParams var2);

   ScanResult<String> scan(String var1, ScanParams var2, String var3);

   String randomKey();
}
