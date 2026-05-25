package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListDirection;
import me.neznamy.tab.libs.redis.clients.jedis.args.ListPosition;
import me.neznamy.tab.libs.redis.clients.jedis.params.LPosParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface ListBinaryCommands {
   long rpush(byte[] var1, byte[]... var2);

   long lpush(byte[] var1, byte[]... var2);

   long llen(byte[] var1);

   List<byte[]> lrange(byte[] var1, long var2, long var4);

   String ltrim(byte[] var1, long var2, long var4);

   byte[] lindex(byte[] var1, long var2);

   String lset(byte[] var1, long var2, byte[] var4);

   long lrem(byte[] var1, long var2, byte[] var4);

   byte[] lpop(byte[] var1);

   List<byte[]> lpop(byte[] var1, int var2);

   Long lpos(byte[] var1, byte[] var2);

   Long lpos(byte[] var1, byte[] var2, LPosParams var3);

   List<Long> lpos(byte[] var1, byte[] var2, LPosParams var3, long var4);

   byte[] rpop(byte[] var1);

   List<byte[]> rpop(byte[] var1, int var2);

   long linsert(byte[] var1, ListPosition var2, byte[] var3, byte[] var4);

   long lpushx(byte[] var1, byte[]... var2);

   long rpushx(byte[] var1, byte[]... var2);

   List<byte[]> blpop(int var1, byte[]... var2);

   KeyValue<byte[], byte[]> blpop(double var1, byte[]... var3);

   List<byte[]> brpop(int var1, byte[]... var2);

   KeyValue<byte[], byte[]> brpop(double var1, byte[]... var3);

   byte[] rpoplpush(byte[] var1, byte[] var2);

   byte[] brpoplpush(byte[] var1, byte[] var2, int var3);

   byte[] lmove(byte[] var1, byte[] var2, ListDirection var3, ListDirection var4);

   byte[] blmove(byte[] var1, byte[] var2, ListDirection var3, ListDirection var4, double var5);

   KeyValue<byte[], List<byte[]>> lmpop(ListDirection var1, byte[]... var2);

   KeyValue<byte[], List<byte[]>> lmpop(ListDirection var1, int var2, byte[]... var3);

   KeyValue<byte[], List<byte[]>> blmpop(double var1, ListDirection var3, byte[]... var4);

   KeyValue<byte[], List<byte[]>> blmpop(double var1, ListDirection var3, int var4, byte[]... var5);
}
