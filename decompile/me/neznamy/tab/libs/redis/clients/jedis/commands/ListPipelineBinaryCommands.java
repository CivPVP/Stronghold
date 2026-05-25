package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface ListPipelineBinaryCommands {
   Response<Long> rpush(byte[] var1, byte[]... var2);

   Response<Long> lpush(byte[] var1, byte[]... var2);

   Response<Long> llen(byte[] var1);

   Response<List<byte[]>> lrange(byte[] var1, long var2, long var4);

   Response<String> ltrim(byte[] var1, long var2, long var4);

   Response<byte[]> lindex(byte[] var1, long var2);

   Response<String> lset(byte[] var1, long var2, byte[] var4);

   Response<Long> lrem(byte[] var1, long var2, byte[] var4);

   Response<byte[]> lpop(byte[] var1);

   Response<List<byte[]>> lpop(byte[] var1, int var2);

   Response<Long> lpos(byte[] var1, byte[] var2);

   Response<Long> lpos(byte[] var1, byte[] var2, LPosParams var3);

   Response<List<Long>> lpos(byte[] var1, byte[] var2, LPosParams var3, long var4);

   Response<byte[]> rpop(byte[] var1);

   Response<List<byte[]>> rpop(byte[] var1, int var2);

   Response<Long> linsert(byte[] var1, ListPosition var2, byte[] var3, byte[] var4);

   Response<Long> lpushx(byte[] var1, byte[]... var2);

   Response<Long> rpushx(byte[] var1, byte[]... var2);

   Response<List<byte[]>> blpop(int var1, byte[]... var2);

   Response<KeyValue<byte[], byte[]>> blpop(double var1, byte[]... var3);

   Response<List<byte[]>> brpop(int var1, byte[]... var2);

   Response<KeyValue<byte[], byte[]>> brpop(double var1, byte[]... var3);

   Response<byte[]> rpoplpush(byte[] var1, byte[] var2);

   Response<byte[]> brpoplpush(byte[] var1, byte[] var2, int var3);

   Response<byte[]> lmove(byte[] var1, byte[] var2, ListDirection var3, ListDirection var4);

   Response<byte[]> blmove(byte[] var1, byte[] var2, ListDirection var3, ListDirection var4, double var5);

   Response<KeyValue<byte[], List<byte[]>>> lmpop(ListDirection var1, byte[]... var2);

   Response<KeyValue<byte[], List<byte[]>>> lmpop(ListDirection var1, int var2, byte[]... var3);

   Response<KeyValue<byte[], List<byte[]>>> blmpop(double var1, ListDirection var3, byte[]... var4);

   Response<KeyValue<byte[], List<byte[]>>> blmpop(double var1, ListDirection var3, int var4, byte[]... var5);
}
