package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.args.FlushMode;
import me.neznamy.tab.libs.redis.clients.jedis.args.LatencyEvent;
import me.neznamy.tab.libs.redis.clients.jedis.args.SaveMode;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.params.LolwutParams;
import me.neznamy.tab.libs.redis.clients.jedis.params.ShutdownParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LatencyHistoryInfo;
import me.neznamy.tab.libs.redis.clients.jedis.resps.LatencyLatestInfo;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface ServerCommands {
   String ping();

   String ping(String var1);

   String echo(String var1);

   byte[] echo(byte[] var1);

   String flushDB();

   String flushDB(FlushMode var1);

   String flushAll();

   String flushAll(FlushMode var1);

   String auth(String var1);

   String auth(String var1, String var2);

   String save();

   String bgsave();

   String bgsaveSchedule();

   String bgrewriteaof();

   long lastsave();

   void shutdown() throws JedisException;

   default void shutdown(SaveMode saveMode) throws JedisException {
      this.shutdown(ShutdownParams.shutdownParams().saveMode(saveMode));
   }

   void shutdown(ShutdownParams var1) throws JedisException;

   String shutdownAbort();

   String info();

   String info(String var1);

   @Deprecated
   String slaveof(String var1, int var2);

   @Deprecated
   String slaveofNoOne();

   String replicaof(String var1, int var2);

   String replicaofNoOne();

   long waitReplicas(int var1, long var2);

   KeyValue<Long, Long> waitAOF(long var1, long var3, long var5);

   String lolwut();

   String lolwut(LolwutParams var1);

   String reset();

   String latencyDoctor();

   Map<String, LatencyLatestInfo> latencyLatest();

   List<LatencyHistoryInfo> latencyHistory(LatencyEvent var1);

   long latencyReset(LatencyEvent... var1);
}
