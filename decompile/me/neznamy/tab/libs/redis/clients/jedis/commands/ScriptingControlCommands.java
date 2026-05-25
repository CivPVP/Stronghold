package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;

public interface ScriptingControlCommands {
   Boolean scriptExists(String var1);

   List<Boolean> scriptExists(String... var1);

   Boolean scriptExists(byte[] var1);

   List<Boolean> scriptExists(byte[]... var1);

   String scriptLoad(String var1);

   byte[] scriptLoad(byte[] var1);

   String scriptFlush();

   String scriptFlush(FlushMode var1);

   String scriptKill();
}
