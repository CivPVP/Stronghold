package me.neznamy.tab.libs.redis.clients.jedis.gears;

import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

@Deprecated
public class RedisGearsProtocol {
   @Deprecated
   public enum GearsCommand implements ProtocolCommand {
      @Deprecated
      TFUNCTION,
      @Deprecated
      TFCALL,
      @Deprecated
      TFCALLASYNC;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   @Deprecated
   public enum GearsKeyword implements Rawable {
      CONFIG,
      REPLACE,
      LOAD,
      DELETE,
      LIST,
      WITHCODE,
      LIBRARY,
      VERBOSE;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
