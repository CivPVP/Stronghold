package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.FunctionRestorePolicy;

public interface FunctionBinaryCommands {
   Object fcall(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   Object fcallReadonly(byte[] var1, List<byte[]> var2, List<byte[]> var3);

   String functionDelete(byte[] var1);

   byte[] functionDump();

   String functionFlush();

   String functionFlush(FlushMode var1);

   String functionKill();

   List<Object> functionListBinary();

   List<Object> functionList(byte[] var1);

   List<Object> functionListWithCodeBinary();

   List<Object> functionListWithCode(byte[] var1);

   String functionLoad(byte[] var1);

   String functionLoadReplace(byte[] var1);

   String functionRestore(byte[] var1);

   String functionRestore(byte[] var1, FunctionRestorePolicy var2);

   Object functionStatsBinary();
}
