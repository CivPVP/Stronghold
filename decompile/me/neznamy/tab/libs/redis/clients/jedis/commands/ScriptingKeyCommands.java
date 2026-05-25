package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;

public interface ScriptingKeyCommands {
   Object eval(String var1);

   Object eval(String var1, int var2, String... var3);

   Object eval(String var1, List<String> var2, List<String> var3);

   Object evalReadonly(String var1, List<String> var2, List<String> var3);

   Object evalsha(String var1);

   Object evalsha(String var1, int var2, String... var3);

   Object evalsha(String var1, List<String> var2, List<String> var3);

   Object evalshaReadonly(String var1, List<String> var2, List<String> var3);
}
