package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;
import me.neznamy.tab.libs.redis.clients.jedis.resps.FunctionStats;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LibraryInfo;

public interface FunctionCommands {
   Object fcall(String var1, List<String> var2, List<String> var3);

   Object fcallReadonly(String var1, List<String> var2, List<String> var3);

   String functionDelete(String var1);

   byte[] functionDump();

   String functionFlush();

   String functionFlush(FlushMode var1);

   String functionKill();

   List<LibraryInfo> functionList();

   List<LibraryInfo> functionList(String var1);

   List<LibraryInfo> functionListWithCode();

   List<LibraryInfo> functionListWithCode(String var1);

   String functionLoad(String var1);

   String functionLoadReplace(String var1);

   String functionRestore(byte[] var1);

   String functionRestore(byte[] var1, FunctionRestorePolicy var2);

   FunctionStats functionStats();
}
