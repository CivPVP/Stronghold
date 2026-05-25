package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Collection;
import java.util.function.Function;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisCommandIterationBase;

public class ScanIteration extends JedisCommandIterationBase<ScanResult<String>, String> {
   private final int count;
   private final Function<String, CommandArguments> args;

   public ScanIteration(ConnectionProvider connectionProvider, int batchCount, String match) {
      super(connectionProvider, BuilderFactory.SCAN_RESPONSE);
      this.count = batchCount;
      this.args = cursor -> new CommandArguments(Protocol.Command.SCAN)
         .add(cursor)
         .add(Protocol.Keyword.MATCH)
         .add(match)
         .add(Protocol.Keyword.COUNT)
         .add(this.count);
   }

   public ScanIteration(ConnectionProvider connectionProvider, int batchCount, String match, String type) {
      super(connectionProvider, BuilderFactory.SCAN_RESPONSE);
      this.count = batchCount;
      this.args = cursor -> new CommandArguments(Protocol.Command.SCAN)
         .add(cursor)
         .add(Protocol.Keyword.MATCH)
         .add(match)
         .add(Protocol.Keyword.COUNT)
         .add(this.count)
         .add(Protocol.Keyword.TYPE)
         .add(type);
   }

   protected boolean isNodeCompleted(ScanResult<String> reply) {
      return reply.isCompleteIteration();
   }

   @Override
   protected CommandArguments initCommandArguments() {
      return this.args.apply(ScanParams.SCAN_POINTER_START);
   }

   protected CommandArguments nextCommandArguments(ScanResult<String> lastReply) {
      return this.args.apply(lastReply.getCursor());
   }

   protected Collection<String> convertBatchToData(ScanResult<String> batch) {
      return batch.getResult();
   }
}
