package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;

public interface ScriptingKeyPipelineBinaryCommands {
   Response<Object> eval(byte[] var1);

   Response<Object> eval(byte[] var1, int var2, byte[]... var3);

   Response<Object> eval(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Response<Object> evalReadonly(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Response<Object> evalsha(byte[] var1);

   Response<Object> evalsha(byte[] var1, int var2, byte[]... var3);

   Response<Object> evalsha(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Response<Object> evalshaReadonly(byte[] var1, List<byte[]> var2, List<byte[]> var3);
}
