package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;

public interface SentinelCommands {
   String sentinelMyId();

   List<Map<String, String>> sentinelMasters();

   Map<String, String> sentinelMaster(String var1);

   List<Map<String, String>> sentinelSentinels(String var1);

   List<String> sentinelGetMasterAddrByName(String var1);

   Long sentinelReset(String var1);

   @Deprecated
   List<Map<String, String>> sentinelSlaves(String var1);

   List<Map<String, String>> sentinelReplicas(String var1);

   String sentinelFailover(String var1);

   String sentinelMonitor(String var1, String var2, int var3, int var4);

   String sentinelRemove(String var1);

   String sentinelSet(String var1, Map<String, String> var2);
}
