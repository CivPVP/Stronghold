package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.ScanResult;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisClusterHashTag;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class ClusterCommandObjects extends CommandObjects {
   private static final String CLUSTER_UNSUPPORTED_MESSAGE = "Not supported in cluster mode.";
   private static final String KEYS_PATTERN_MESSAGE = "Cluster mode only supports KEYS command with pattern containing hash-tag ( curly-brackets enclosed string )";
   private static final String SCAN_PATTERN_MESSAGE = "Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )";

   protected ClusterCommandArguments commandArguments(ProtocolCommand command) {
      ClusterCommandArguments comArgs = new ClusterCommandArguments(command);
      if (this.keyPreProcessor != null) {
         comArgs.setKeyArgumentPreProcessor(this.keyPreProcessor);
      }

      return comArgs;
   }

   @Override
   public CommandObject<Long> dbSize() {
      throw new UnsupportedOperationException("Not supported in cluster mode.");
   }

   @Override
   public final CommandObject<Set<String>> keys(String pattern) {
      if (!JedisClusterHashTag.isClusterCompliantMatchPattern(pattern)) {
         throw new IllegalArgumentException("Cluster mode only supports KEYS command with pattern containing hash-tag ( curly-brackets enclosed string )");
      } else {
         return new CommandObject<>(this.commandArguments(Protocol.Command.KEYS).key(pattern).processKey(pattern), BuilderFactory.STRING_SET);
      }
   }

   @Override
   public final CommandObject<Set<byte[]>> keys(byte[] pattern) {
      if (!JedisClusterHashTag.isClusterCompliantMatchPattern(pattern)) {
         throw new IllegalArgumentException("Cluster mode only supports KEYS command with pattern containing hash-tag ( curly-brackets enclosed string )");
      } else {
         return new CommandObject<>(this.commandArguments(Protocol.Command.KEYS).key(pattern).processKey(pattern), BuilderFactory.BINARY_SET);
      }
   }

   @Override
   public final CommandObject<ScanResult<String>> scan(String cursor) {
      throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
   }

   @Override
   public final CommandObject<ScanResult<String>> scan(String cursor, ScanParams params) {
      String match = params.match();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match), BuilderFactory.SCAN_RESPONSE);
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<ScanResult<String>> scan(String cursor, ScanParams params, String type) {
      String match = params.match();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(
            this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match).add(Protocol.Keyword.TYPE).add(type),
            BuilderFactory.SCAN_RESPONSE
         );
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<ScanResult<byte[]>> scan(byte[] cursor) {
      throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
   }

   @Override
   public final CommandObject<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params) {
      byte[] match = params.binaryMatch();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(
            this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match), BuilderFactory.SCAN_BINARY_RESPONSE
         );
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<ScanResult<byte[]>> scan(byte[] cursor, ScanParams params, byte[] type) {
      byte[] match = params.binaryMatch();
      if (match != null && JedisClusterHashTag.isClusterCompliantMatchPattern(match)) {
         return new CommandObject<>(
            this.commandArguments(Protocol.Command.SCAN).add(cursor).addParams(params).processKey(match).add(Protocol.Keyword.TYPE).add(type),
            BuilderFactory.SCAN_BINARY_RESPONSE
         );
      } else {
         throw new IllegalArgumentException("Cluster mode only supports SCAN command with MATCH pattern containing hash-tag ( curly-brackets enclosed string )");
      }
   }

   @Override
   public final CommandObject<Long> waitReplicas(int replicas, long timeout) {
      throw new UnsupportedOperationException("Not supported in cluster mode.");
   }

   @Override
   public CommandObject<KeyValue<Long, Long>> waitAOF(long numLocal, long numReplicas, long timeout) {
      throw new UnsupportedOperationException("Not supported in cluster mode.");
   }
}
