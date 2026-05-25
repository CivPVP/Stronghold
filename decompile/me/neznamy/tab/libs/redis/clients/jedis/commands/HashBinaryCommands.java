package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.args.ExpiryOption;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface HashBinaryCommands {
   long hset(byte[] var1, byte[] var2, byte[] var3);

   long hset(byte[] var1, Map<byte[], byte[]> var2);

   byte[] hget(byte[] var1, byte[] var2);

   long hsetnx(byte[] var1, byte[] var2, byte[] var3);

   String hmset(byte[] var1, Map<byte[], byte[]> var2);

   List<byte[]> hmget(byte[] var1, byte[]... var2);

   long hincrBy(byte[] var1, byte[] var2, long var3);

   double hincrByFloat(byte[] var1, byte[] var2, double var3);

   boolean hexists(byte[] var1, byte[] var2);

   long hdel(byte[] var1, byte[]... var2);

   long hlen(byte[] var1);

   Set<byte[]> hkeys(byte[] var1);

   List<byte[]> hvals(byte[] var1);

   Map<byte[], byte[]> hgetAll(byte[] var1);

   byte[] hrandfield(byte[] var1);

   List<byte[]> hrandfield(byte[] var1, long var2);

   List<Entry<byte[], byte[]>> hrandfieldWithValues(byte[] var1, long var2);

   default ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
      return this.hscan(key, cursor, new ScanParams());
   }

   ScanResult<Entry<byte[], byte[]>> hscan(byte[] var1, byte[] var2, ScanParams var3);

   default ScanResult<byte[]> hscanNoValues(byte[] key, byte[] cursor) {
      return this.hscanNoValues(key, cursor, new ScanParams());
   }

   ScanResult<byte[]> hscanNoValues(byte[] var1, byte[] var2, ScanParams var3);

   long hstrlen(byte[] var1, byte[] var2);

   List<Long> hexpire(byte[] var1, long var2, byte[]... var4);

   List<Long> hexpire(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   List<Long> hpexpire(byte[] var1, long var2, byte[]... var4);

   List<Long> hpexpire(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   List<Long> hexpireAt(byte[] var1, long var2, byte[]... var4);

   List<Long> hexpireAt(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   List<Long> hpexpireAt(byte[] var1, long var2, byte[]... var4);

   List<Long> hpexpireAt(byte[] var1, long var2, ExpiryOption var4, byte[]... var5);

   List<Long> hexpireTime(byte[] var1, byte[]... var2);

   List<Long> hpexpireTime(byte[] var1, byte[]... var2);

   List<Long> httl(byte[] var1, byte[]... var2);

   List<Long> hpttl(byte[] var1, byte[]... var2);

   List<Long> hpersist(byte[] var1, byte[]... var2);
}
