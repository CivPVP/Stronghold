package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClusterFailoverOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClusterResetType;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ClusterShardInfo;

public interface ClusterCommands {
   String asking();

   String readonly();

   String readwrite();

   String clusterNodes();

   String clusterMeet(String var1, int var2);

   String clusterAddSlots(int... var1);

   String clusterDelSlots(int... var1);

   String clusterInfo();

   List<String> clusterGetKeysInSlot(int var1, int var2);

   List<byte[]> clusterGetKeysInSlotBinary(int var1, int var2);

   String clusterSetSlotNode(int var1, String var2);

   String clusterSetSlotMigrating(int var1, String var2);

   String clusterSetSlotImporting(int var1, String var2);

   String clusterSetSlotStable(int var1);

   String clusterForget(String var1);

   String clusterFlushSlots();

   long clusterKeySlot(String var1);

   long clusterCountFailureReports(String var1);

   long clusterCountKeysInSlot(int var1);

   String clusterSaveConfig();

   String clusterSetConfigEpoch(long var1);

   String clusterBumpEpoch();

   String clusterReplicate(String var1);

   @Deprecated
   List<String> clusterSlaves(String var1);

   List<String> clusterReplicas(String var1);

   String clusterFailover();

   String clusterFailover(ClusterFailoverOption var1);

   @Deprecated
   List<Object> clusterSlots();

   List<ClusterShardInfo> clusterShards();

   String clusterReset();

   String clusterReset(ClusterResetType var1);

   String clusterMyId();

   String clusterMyShardId();

   List<Map<String, Object>> clusterLinks();

   String clusterAddSlotsRange(int... var1);

   String clusterDelSlotsRange(int... var1);
}
