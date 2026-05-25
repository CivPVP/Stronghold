package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;

public interface FunctionPipelineBinaryCommands {
   Response<Object> fcall(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Response<Object> fcallReadonly(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Response<String> functionDelete(byte[] var1);

   Response<byte[]> functionDump();

   Response<String> functionFlush();

   Response<String> functionFlush(FlushMode var1);

   Response<String> functionKill();

   Response<List<Object>> functionListBinary();

   Response<List<Object>> functionList(byte[] var1);

   Response<List<Object>> functionListWithCodeBinary();

   Response<List<Object>> functionListWithCode(byte[] var1);

   Response<String> functionLoad(byte[] var1);

   Response<String> functionLoadReplace(byte[] var1);

   Response<String> functionRestore(byte[] var1);

   Response<String> functionRestore(byte[] var1, FunctionRestorePolicy var2);

   Response<Object> functionStatsBinary();
}
