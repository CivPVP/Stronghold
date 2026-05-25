package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;

public interface ScriptingKeyPipelineCommands {
   Response<Object> eval(String var1);

   Response<Object> eval(String var1, int var2, String... var3);

   Response<Object> eval(String var1, List<String> var2, List<String> var3);

   Response<Object> evalReadonly(String var1, List<String> var2, List<String> var3);

   Response<Object> evalsha(String var1);

   Response<Object> evalsha(String var1, int var2, String... var3);

   Response<Object> evalsha(String var1, List<String> var2, List<String> var3);

   Response<Object> evalshaReadonly(String var1, List<String> var2, List<String> var3);
}
