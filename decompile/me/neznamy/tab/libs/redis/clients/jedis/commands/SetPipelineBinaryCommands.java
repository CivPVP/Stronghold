package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;

public interface SetPipelineBinaryCommands {
   Response<Long> sadd(byte[] var1, byte[]... var2);

   Response<Set<byte[]>> smembers(byte[] var1);

   Response<Long> srem(byte[] var1, byte[]... var2);

   Response<byte[]> spop(byte[] var1);

   Response<Set<byte[]>> spop(byte[] var1, long var2);

   Response<Long> scard(byte[] var1);

   Response<Boolean> sismember(byte[] var1, byte[] var2);

   Response<List<Boolean>> smismember(byte[] var1, byte[]... var2);

   Response<byte[]> srandmember(byte[] var1);

   Response<List<byte[]>> srandmember(byte[] var1, int var2);

   default Response<ScanResult<byte[]>> sscan(byte[] key, byte[] cursor) {
      return this.sscan(key, cursor, new ScanParams());
   }

   Response<ScanResult<byte[]>> sscan(byte[] var1, byte[] var2, ScanParams var3);

   Response<Set<byte[]>> sdiff(byte[]... var1);

   Response<Long> sdiffstore(byte[] var1, byte[]... var2);

   Response<Set<byte[]>> sinter(byte[]... var1);

   Response<Long> sinterstore(byte[] var1, byte[]... var2);

   Response<Long> sintercard(byte[]... var1);

   Response<Long> sintercard(int var1, byte[]... var2);

   Response<Set<byte[]>> sunion(byte[]... var1);

   Response<Long> sunionstore(byte[] var1, byte[]... var2);

   Response<Long> smove(byte[] var1, byte[] var2, byte[] var3);
}
