package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.json.JsonProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.TimeSeriesProtocol;

public class DefaultCacheable implements Cacheable {
   public static final DefaultCacheable INSTANCE = new DefaultCacheable();
   private static final Set<ProtocolCommand> DEFAULT_CACHEABLE_COMMANDS = new HashSet<ProtocolCommand>() {
      {
         this.add(Protocol.Command.BITCOUNT);
         this.add(Protocol.Command.BITFIELD_RO);
         this.add(Protocol.Command.BITPOS);
         this.add(Protocol.Command.EXISTS);
         this.add(Protocol.Command.GEODIST);
         this.add(Protocol.Command.GEOHASH);
         this.add(Protocol.Command.GEOPOS);
         this.add(Protocol.Command.GEORADIUSBYMEMBER_RO);
         this.add(Protocol.Command.GEORADIUS_RO);
         this.add(Protocol.Command.GEOSEARCH);
         this.add(Protocol.Command.GET);
         this.add(Protocol.Command.GETBIT);
         this.add(Protocol.Command.GETRANGE);
         this.add(Protocol.Command.HEXISTS);
         this.add(Protocol.Command.HGET);
         this.add(Protocol.Command.HGETALL);
         this.add(Protocol.Command.HKEYS);
         this.add(Protocol.Command.HLEN);
         this.add(Protocol.Command.HMGET);
         this.add(Protocol.Command.HSTRLEN);
         this.add(Protocol.Command.HVALS);
         this.add(JsonProtocol.JsonCommand.ARRINDEX);
         this.add(JsonProtocol.JsonCommand.ARRLEN);
         this.add(JsonProtocol.JsonCommand.GET);
         this.add(JsonProtocol.JsonCommand.MGET);
         this.add(JsonProtocol.JsonCommand.OBJKEYS);
         this.add(JsonProtocol.JsonCommand.OBJLEN);
         this.add(JsonProtocol.JsonCommand.STRLEN);
         this.add(JsonProtocol.JsonCommand.TYPE);
         this.add(Protocol.Command.LCS);
         this.add(Protocol.Command.LINDEX);
         this.add(Protocol.Command.LLEN);
         this.add(Protocol.Command.LPOS);
         this.add(Protocol.Command.LRANGE);
         this.add(Protocol.Command.MGET);
         this.add(Protocol.Command.SCARD);
         this.add(Protocol.Command.SDIFF);
         this.add(Protocol.Command.SINTER);
         this.add(Protocol.Command.SISMEMBER);
         this.add(Protocol.Command.SMEMBERS);
         this.add(Protocol.Command.SMISMEMBER);
         this.add(Protocol.Command.STRLEN);
         this.add(Protocol.Command.SUBSTR);
         this.add(Protocol.Command.SUNION);
         this.add(TimeSeriesProtocol.TimeSeriesCommand.GET);
         this.add(TimeSeriesProtocol.TimeSeriesCommand.INFO);
         this.add(TimeSeriesProtocol.TimeSeriesCommand.RANGE);
         this.add(TimeSeriesProtocol.TimeSeriesCommand.REVRANGE);
         this.add(Protocol.Command.TYPE);
         this.add(Protocol.Command.XLEN);
         this.add(Protocol.Command.XPENDING);
         this.add(Protocol.Command.XRANGE);
         this.add(Protocol.Command.XREVRANGE);
         this.add(Protocol.Command.ZCARD);
         this.add(Protocol.Command.ZCOUNT);
         this.add(Protocol.Command.ZLEXCOUNT);
         this.add(Protocol.Command.ZMSCORE);
         this.add(Protocol.Command.ZRANGE);
         this.add(Protocol.Command.ZRANGEBYLEX);
         this.add(Protocol.Command.ZRANGEBYSCORE);
         this.add(Protocol.Command.ZRANK);
         this.add(Protocol.Command.ZREVRANGE);
         this.add(Protocol.Command.ZREVRANGEBYLEX);
         this.add(Protocol.Command.ZREVRANGEBYSCORE);
         this.add(Protocol.Command.ZREVRANK);
         this.add(Protocol.Command.ZSCORE);
      }
   };

   public static boolean isDefaultCacheableCommand(ProtocolCommand command) {
      return DEFAULT_CACHEABLE_COMMANDS.contains(command);
   }

   @Override
   public boolean isCacheable(ProtocolCommand command, List<Object> keys) {
      return isDefaultCacheableCommand(command);
   }
}
