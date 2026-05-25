package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface SampleBinaryKeyedCommands {
   long waitReplicas(byte[] var1, int var2, long var3);

   KeyValue<Long, Long> waitAOF(byte[] var1, long var2, long var4, long var6);

   Object eval(byte[] var1, byte[] var2);

   Object evalsha(byte[] var1, byte[] var2);

   Boolean scriptExists(byte[] var1, byte[] var2);

   List<Boolean> scriptExists(byte[] var1, byte[]... var2);

   byte[] scriptLoad(byte[] var1, byte[] var2);

   String scriptFlush(byte[] var1);

   String scriptFlush(byte[] var1, FlushMode var2);

   String scriptKill(byte[] var1);
}
