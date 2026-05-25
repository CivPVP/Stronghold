package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface SampleKeyedCommands {
   long waitReplicas(String var1, int var2, long var3);

   KeyValue<Long, Long> waitAOF(String var1, long var2, long var4, long var6);

   Object eval(String var1, String var2);

   Object evalsha(String var1, String var2);

   Boolean scriptExists(String var1, String var2);

   List<Boolean> scriptExists(String var1, String... var2);

   String scriptLoad(String var1, String var2);

   String scriptFlush(String var1);

   String scriptFlush(String var1, FlushMode var2);

   String scriptKill(String var1);
}
