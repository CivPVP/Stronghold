package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitCountOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.BitOP;
import me.neznamy.tab.libs.redis.clients.jedis.params.BitPosParams;

public interface BitBinaryCommands {
   boolean setbit(byte[] var1, long var2, boolean var4);

   boolean getbit(byte[] var1, long var2);

   long bitcount(byte[] var1);

   long bitcount(byte[] var1, long var2, long var4);

   long bitcount(byte[] var1, long var2, long var4, BitCountOption var6);

   long bitpos(byte[] var1, boolean var2);

   long bitpos(byte[] var1, boolean var2, BitPosParams var3);

   List<Long> bitfield(byte[] var1, byte[]... var2);

   List<Long> bitfieldReadonly(byte[] var1, byte[]... var2);

   long bitop(BitOP var1, byte[] var2, byte[]... var3);
}
