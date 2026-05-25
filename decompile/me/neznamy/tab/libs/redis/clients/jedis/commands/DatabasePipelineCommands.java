package me.neznamy.tab.libs.redis.clients.jedis.commands;

import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;

public interface DatabasePipelineCommands {
   Response<String> select(int var1);

   Response<Long> dbSize();

   Response<String> swapDB(int var1, int var2);

   Response<Long> move(String var1, int var2);

   Response<Long> move(byte[] var1, int var2);

   Response<Boolean> copy(String var1, String var2, int var3, boolean var4);

   Response<Boolean> copy(byte[] var1, byte[] var2, int var3, boolean var4);

   Response<String> migrate(String var1, int var2, byte[] var3, int var4, int var5);

   Response<String> migrate(String var1, int var2, int var3, int var4, MigrateParams var5, byte[]... var6);

   Response<String> migrate(String var1, int var2, String var3, int var4, int var5);

   Response<String> migrate(String var1, int var2, int var3, int var4, MigrateParams var5, String... var6);
}
