package me.neznamy.tab.libs.redis.clients.jedis.gears;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.gears.resps.GearsLibraryInfo;

@Deprecated
public interface RedisGearsCommands {
   @Deprecated
   default String tFunctionLoad(String libraryCode) {
      return this.tFunctionLoad(libraryCode, TFunctionLoadParams.loadParams());
   }

   @Deprecated
   String tFunctionLoad(String var1, TFunctionLoadParams var2);

   @Deprecated
   default List<GearsLibraryInfo> tFunctionList() {
      return this.tFunctionList(TFunctionListParams.listParams());
   }

   @Deprecated
   List<GearsLibraryInfo> tFunctionList(TFunctionListParams var1);

   @Deprecated
   String tFunctionDelete(String var1);

   @Deprecated
   Object tFunctionCall(String var1, String var2, List<String> var3, List<String> var4);

   @Deprecated
   Object tFunctionCallAsync(String var1, String var2, List<String> var3, List<String> var4);
}
