package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface SampleBinaryKeyedPipelineCommands {
   Response<Long> waitReplicas(byte[] var1, int var2, long var3);

   Response<KeyValue<Long, Long>> waitAOF(byte[] var1, long var2, long var4, long var6);

   Response<Object> eval(byte[] var1, byte[] var2);

   Response<Object> evalsha(byte[] var1, byte[] var2);

   Response<List<Boolean>> scriptExists(byte[] var1, byte[]... var2);

   Response<byte[]> scriptLoad(byte[] var1, byte[] var2);

   Response<String> scriptFlush(byte[] var1);

   Response<String> scriptFlush(byte[] var1, FlushMode var2);

   Response<String> scriptKill(byte[] var1);
}
