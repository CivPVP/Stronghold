package me.neznamy.tab.libs.redis.clients.jedis.graph;

import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

@Deprecated
public class GraphProtocol {
   @Deprecated
   public enum GraphCommand implements ProtocolCommand {
      QUERY,
      RO_QUERY,
      DELETE,
      LIST,
      PROFILE,
      EXPLAIN,
      SLOWLOG,
      CONFIG;

      private final byte[] raw = SafeEncoder.encode("GRAPH." + this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   @Deprecated
   public enum GraphKeyword implements Rawable {
      CYPHER,
      TIMEOUT,
      SET,
      GET,
      __COMPACT("--COMPACT");

      private final byte[] raw;

      GraphKeyword() {
         this.raw = SafeEncoder.encode(this.name());
      }

      GraphKeyword(String alt) {
         this.raw = SafeEncoder.encode(alt);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
