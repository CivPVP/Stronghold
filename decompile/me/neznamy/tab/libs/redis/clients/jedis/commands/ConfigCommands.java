package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.Map;

public interface ConfigCommands {
   Map<String, String> configGet(String var1);

   Map<String, String> configGet(String... var1);

   Map<byte[], byte[]> configGet(byte[] var1);

   Map<byte[], byte[]> configGet(byte[]... var1);

   String configSet(String var1, String var2);

   String configSet(String... var1);

   String configSet(Map<String, String> var1);

   String configSet(byte[] var1, byte[] var2);

   String configSet(byte[]... var1);

   String configSetBinary(Map<byte[], byte[]> var1);

   String configResetStat();

   String configRewrite();
}
