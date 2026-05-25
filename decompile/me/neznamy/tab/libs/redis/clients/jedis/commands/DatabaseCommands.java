package me.neznamy.tab.libs.redis.clients.jedis.commands;

import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;

public interface DatabaseCommands {
   String select(int var1);

   long dbSize();

   String flushDB();

   String flushDB(FlushMode var1);

   String swapDB(int var1, int var2);

   long move(String var1, int var2);

   long move(byte[] var1, int var2);

   boolean copy(String var1, String var2, int var3, boolean var4);

   boolean copy(byte[] var1, byte[] var2, int var3, boolean var4);

   String migrate(String var1, int var2, String var3, int var4, int var5);

   String migrate(String var1, int var2, byte[] var3, int var4, int var5);

   String migrate(String var1, int var2, int var3, int var4, MigrateParams var5, String... var6);

   String migrate(String var1, int var2, int var3, int var4, MigrateParams var5, byte[]... var6);
}
