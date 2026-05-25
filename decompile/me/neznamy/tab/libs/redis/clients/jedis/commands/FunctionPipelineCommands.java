package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;
import me.neznamy.tab.libs.redis.clients.jedis.resps.FunctionStats;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LibraryInfo;

public interface FunctionPipelineCommands {
   Response<Object> fcall(String var1, List<String> var2, List<String> var3);

   Response<Object> fcallReadonly(String var1, List<String> var2, List<String> var3);

   Response<String> functionDelete(String var1);

   Response<byte[]> functionDump();

   Response<String> functionFlush();

   Response<String> functionFlush(FlushMode var1);

   Response<String> functionKill();

   Response<List<LibraryInfo>> functionList();

   Response<List<LibraryInfo>> functionList(String var1);

   Response<List<LibraryInfo>> functionListWithCode();

   Response<List<LibraryInfo>> functionListWithCode(String var1);

   Response<String> functionLoad(String var1);

   Response<String> functionLoadReplace(String var1);

   Response<String> functionRestore(byte[] var1);

   Response<String> functionRestore(byte[] var1, FunctionRestorePolicy var2);

   Response<FunctionStats> functionStats();
}
