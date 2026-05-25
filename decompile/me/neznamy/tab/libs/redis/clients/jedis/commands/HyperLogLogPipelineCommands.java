package me.neznamy.tab.libs.redis.clients.jedis.commands;

import me.neznamy.tab.libs.redis.clients.jedis.Response;

public interface HyperLogLogPipelineCommands {
   Response<Long> pfadd(String var1, String... var2);

   Response<String> pfmerge(String var1, String... var2);

   Response<Long> pfcount(String var1);

   Response<Long> pfcount(String... var1);
}
