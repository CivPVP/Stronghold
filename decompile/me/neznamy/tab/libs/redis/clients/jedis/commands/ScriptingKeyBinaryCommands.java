package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;

public interface ScriptingKeyBinaryCommands {
   Object eval(byte[] var1);

   Object eval(byte[] var1, int var2, byte[]... var3);

   Object eval(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Object evalReadonly(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Object evalsha(byte[] var1);

   Object evalsha(byte[] var1, int var2, byte[]... var3);

   Object evalsha(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Object evalshaReadonly(byte[] var1, List<byte[]> var2, List<byte[]> var3);
}
