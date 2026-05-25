package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.csc.Cache;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisAccessControlException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisAskDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisBusyException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisClusterException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisMovedDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisNoScriptException;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;
import me.neznamy.tab.libs.redis.clients.jedis.util.RedisInputStream;
import me.neznamy.tab.libs.redis.clients.jedis.util.RedisOutputStream;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public final class Protocol {
   public static final String DEFAULT_HOST = "127.0.0.1";
   public static final int DEFAULT_PORT = 6379;
   public static final int DEFAULT_SENTINEL_PORT = 26379;
   public static final int DEFAULT_TIMEOUT = 2000;
   public static final int DEFAULT_DATABASE = 0;
   public static final int CLUSTER_HASHSLOTS = 16384;
   public static final Charset CHARSET = StandardCharsets.UTF_8;
   public static final byte ASTERISK_BYTE = 42;
   public static final byte COLON_BYTE = 58;
   public static final byte COMMA_BYTE = 44;
   public static final byte DOLLAR_BYTE = 36;
   public static final byte EQUAL_BYTE = 61;
   public static final byte GREATER_THAN_BYTE = 62;
   public static final byte HASH_BYTE = 35;
   public static final byte LEFT_BRACE_BYTE = 40;
   public static final byte MINUS_BYTE = 45;
   public static final byte PERCENT_BYTE = 37;
   public static final byte PLUS_BYTE = 43;
   public static final byte TILDE_BYTE = 126;
   public static final byte UNDERSCORE_BYTE = 95;
   public static final byte[] BYTES_TRUE = toByteArray(1);
   public static final byte[] BYTES_FALSE = toByteArray(0);
   public static final byte[] BYTES_TILDE = SafeEncoder.encode("~");
   public static final byte[] BYTES_EQUAL = SafeEncoder.encode("=");
   public static final byte[] BYTES_ASTERISK = SafeEncoder.encode("*");
   public static final byte[] POSITIVE_INFINITY_BYTES = "+inf".getBytes();
   public static final byte[] NEGATIVE_INFINITY_BYTES = "-inf".getBytes();
   static final List<KeyValue> PROTOCOL_EMPTY_MAP = Collections.unmodifiableList(new ArrayList<>(0));
   private static final String ASK_PREFIX = "ASK ";
   private static final String MOVED_PREFIX = "MOVED ";
   private static final String CLUSTERDOWN_PREFIX = "CLUSTERDOWN ";
   private static final String BUSY_PREFIX = "BUSY ";
   private static final String NOSCRIPT_PREFIX = "NOSCRIPT ";
   private static final String NOAUTH_PREFIX = "NOAUTH";
   private static final String WRONGPASS_PREFIX = "WRONGPASS";
   private static final String NOPERM_PREFIX = "NOPERM";
   private static final byte[] INVALIDATE_BYTES = SafeEncoder.encode("invalidate");

   private Protocol() {
      throw new InstantiationError("Must not instantiate this class");
   }

   public static void sendCommand(RedisOutputStream os, CommandArguments args) {
      try {
         os.write((byte)42);
         os.writeIntCrLf(args.size());

         for (Rawable arg : args) {
            os.write((byte)36);
            byte[] bin = arg.getRaw();
            os.writeIntCrLf(bin.length);
            os.write(bin);
            os.writeCrLf();
         }
      } catch (IOException e) {
         throw new JedisConnectionException(e);
      }
   }

   private static void processError(RedisInputStream is) {
      String message = is.readLine();
      if (message.startsWith("MOVED ")) {
         String[] movedInfo = parseTargetHostAndSlot(message);
         throw new JedisMovedDataException(message, HostAndPort.from(movedInfo[1]), Integer.parseInt(movedInfo[0]));
      } else if (message.startsWith("ASK ")) {
         String[] askInfo = parseTargetHostAndSlot(message);
         throw new JedisAskDataException(message, HostAndPort.from(askInfo[1]), Integer.parseInt(askInfo[0]));
      } else if (message.startsWith("CLUSTERDOWN ")) {
         throw new JedisClusterException(message);
      } else if (message.startsWith("BUSY ")) {
         throw new JedisBusyException(message);
      } else if (message.startsWith("NOSCRIPT ")) {
         throw new JedisNoScriptException(message);
      } else if (!message.startsWith("NOAUTH") && !message.startsWith("WRONGPASS") && !message.startsWith("NOPERM")) {
         throw new JedisDataException(message);
      } else {
         throw new JedisAccessControlException(message);
      }
   }

   public static String readErrorLineIfPossible(RedisInputStream is) {
      byte b = is.readByte();
      return b != 45 ? null : is.readLine();
   }

   private static String[] parseTargetHostAndSlot(String clusterRedirectResponse) {
      String[] response = new String[2];
      String[] messageInfo = clusterRedirectResponse.split(" ");
      response[0] = messageInfo[1];
      response[1] = messageInfo[2];
      return response;
   }

   private static Object process(RedisInputStream is) {
      byte b = is.readByte();
      switch (b) {
         case 35:
            return is.readBooleanCrLf();
         case 36:
         case 61:
            return processBulkReply(is);
         case 37:
            return processMapKeyValueReply(is);
         case 40:
            return is.readBigIntegerCrLf();
         case 42:
            return processMultiBulkReply(is);
         case 43:
            return is.readLineBytes();
         case 44:
            return is.readDoubleCrLf();
         case 45:
            processError(is);
            return null;
         case 58:
            return is.readLongCrLf();
         case 62:
            return processMultiBulkReply(is);
         case 95:
            return is.readNullCrLf();
         case 126:
            return processMultiBulkReply(is);
         default:
            throw new JedisConnectionException("Unknown reply: " + (char)b);
      }
   }

   private static byte[] processBulkReply(RedisInputStream is) {
      int len = is.readIntCrLf();
      if (len == -1) {
         return null;
      }

      byte[] read = new byte[len];
      int offset = 0;

      while (offset < len) {
         int size = is.read(read, offset, len - offset);
         if (size == -1) {
            throw new JedisConnectionException("It seems like server has closed the connection.");
         }

         offset += size;
      }

      is.readByte();
      is.readByte();
      return read;
   }

   private static List<Object> processMultiBulkReply(RedisInputStream is) {
      int num = is.readIntCrLf();
      if (num == -1) {
         return null;
      }

      List<Object> ret = new ArrayList<>(num);

      for (int i = 0; i < num; i++) {
         try {
            ret.add(process(is));
         } catch (JedisDataException e) {
            ret.add(e);
         }
      }

      return ret;
   }

   private static List<KeyValue> processMapKeyValueReply(RedisInputStream is) {
      int num = is.readIntCrLf();
      switch (num) {
         case -1:
            return null;
         case 0:
            return PROTOCOL_EMPTY_MAP;
         default:
            List<KeyValue> ret = new ArrayList<>(num);

            for (int i = 0; i < num; i++) {
               ret.add(new KeyValue<>(process(is), process(is)));
            }

            return ret;
      }
   }

   public static Object read(RedisInputStream is) {
      return process(is);
   }

   @Experimental
   public static Object read(RedisInputStream is, Cache cache) {
      readPushes(is, cache, false);
      return process(is);
   }

   @Experimental
   public static void readPushes(RedisInputStream is, Cache cache, boolean onlyPendingBuffer) {
      if (onlyPendingBuffer) {
         try {
            while (is.available() > 0 && is.peek((byte)62)) {
               is.readByte();
               processPush(is, cache);
            }
         } catch (IOException e) {
            throw new JedisConnectionException("Failed to read pending buffer for push messages!", e);
         }
      } else {
         while (is.peek((byte)62)) {
            is.readByte();
            processPush(is, cache);
         }
      }
   }

   private static void processPush(RedisInputStream is, Cache cache) {
      List<Object> list = processMultiBulkReply(is);
      if (list.size() == 2 && list.get(0) instanceof byte[] && Arrays.equals(INVALIDATE_BYTES, (byte[])list.get(0))) {
         cache.deleteByRedisKeys((List)list.get(1));
      }
   }

   public static final byte[] toByteArray(boolean value) {
      return value ? BYTES_TRUE : BYTES_FALSE;
   }

   public static final byte[] toByteArray(int value) {
      return SafeEncoder.encode(String.valueOf(value));
   }

   public static final byte[] toByteArray(long value) {
      return SafeEncoder.encode(String.valueOf(value));
   }

   public static final byte[] toByteArray(double value) {
      if (value == Double.POSITIVE_INFINITY) {
         return POSITIVE_INFINITY_BYTES;
      } else {
         return value == Double.NEGATIVE_INFINITY ? NEGATIVE_INFINITY_BYTES : SafeEncoder.encode(String.valueOf(value));
      }
   }

   public enum ClusterKeyword implements Rawable {
      MEET,
      RESET,
      INFO,
      FAILOVER,
      SLOTS,
      NODES,
      REPLICAS,
      SLAVES,
      MYID,
      ADDSLOTS,
      DELSLOTS,
      GETKEYSINSLOT,
      SETSLOT,
      NODE,
      MIGRATING,
      IMPORTING,
      STABLE,
      FORGET,
      FLUSHSLOTS,
      KEYSLOT,
      COUNTKEYSINSLOT,
      SAVECONFIG,
      REPLICATE,
      LINKS,
      ADDSLOTSRANGE,
      DELSLOTSRANGE,
      BUMPEPOCH,
      MYSHARDID,
      SHARDS;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum Command implements ProtocolCommand {
      PING,
      AUTH,
      HELLO,
      SET,
      GET,
      GETDEL,
      GETEX,
      EXISTS,
      DEL,
      UNLINK,
      TYPE,
      FLUSHDB,
      FLUSHALL,
      MOVE,
      KEYS,
      RANDOMKEY,
      RENAME,
      RENAMENX,
      DUMP,
      RESTORE,
      DBSIZE,
      SELECT,
      SWAPDB,
      MIGRATE,
      ECHO,
      EXPIRE,
      EXPIREAT,
      EXPIRETIME,
      PEXPIRE,
      PEXPIREAT,
      PEXPIRETIME,
      TTL,
      PTTL,
      MULTI,
      DISCARD,
      EXEC,
      WATCH,
      UNWATCH,
      SORT,
      SORT_RO,
      INFO,
      SHUTDOWN,
      MONITOR,
      CONFIG,
      LCS,
      GETSET,
      MGET,
      SETNX,
      SETEX,
      PSETEX,
      MSET,
      MSETNX,
      DECR,
      DECRBY,
      INCR,
      INCRBY,
      INCRBYFLOAT,
      STRLEN,
      APPEND,
      SUBSTR,
      SETBIT,
      GETBIT,
      BITPOS,
      SETRANGE,
      GETRANGE,
      BITCOUNT,
      BITOP,
      BITFIELD,
      BITFIELD_RO,
      HSET,
      HGET,
      HSETNX,
      HMSET,
      HMGET,
      HINCRBY,
      HEXISTS,
      HDEL,
      HLEN,
      HKEYS,
      HVALS,
      HGETALL,
      HSTRLEN,
      HEXPIRE,
      HPEXPIRE,
      HEXPIREAT,
      HPEXPIREAT,
      HTTL,
      HPTTL,
      HEXPIRETIME,
      HPEXPIRETIME,
      HPERSIST,
      HRANDFIELD,
      HINCRBYFLOAT,
      RPUSH,
      LPUSH,
      LLEN,
      LRANGE,
      LTRIM,
      LINDEX,
      LSET,
      LREM,
      LPOP,
      RPOP,
      BLPOP,
      BRPOP,
      LINSERT,
      LPOS,
      RPOPLPUSH,
      BRPOPLPUSH,
      BLMOVE,
      LMOVE,
      LMPOP,
      BLMPOP,
      LPUSHX,
      RPUSHX,
      SADD,
      SMEMBERS,
      SREM,
      SPOP,
      SMOVE,
      SCARD,
      SRANDMEMBER,
      SINTER,
      SINTERSTORE,
      SUNION,
      SUNIONSTORE,
      SDIFF,
      SDIFFSTORE,
      SISMEMBER,
      SMISMEMBER,
      SINTERCARD,
      ZADD,
      ZDIFF,
      ZDIFFSTORE,
      ZRANGE,
      ZREM,
      ZINCRBY,
      ZRANK,
      ZREVRANK,
      ZREVRANGE,
      ZRANDMEMBER,
      ZCARD,
      ZSCORE,
      ZPOPMAX,
      ZPOPMIN,
      ZCOUNT,
      ZUNION,
      ZUNIONSTORE,
      ZINTER,
      ZINTERSTORE,
      ZRANGEBYSCORE,
      ZREVRANGEBYSCORE,
      ZREMRANGEBYRANK,
      ZREMRANGEBYSCORE,
      ZLEXCOUNT,
      ZRANGEBYLEX,
      ZREVRANGEBYLEX,
      ZREMRANGEBYLEX,
      ZMSCORE,
      ZRANGESTORE,
      ZINTERCARD,
      ZMPOP,
      BZMPOP,
      BZPOPMIN,
      BZPOPMAX,
      GEOADD,
      GEODIST,
      GEOHASH,
      GEOPOS,
      GEORADIUS,
      GEORADIUS_RO,
      GEOSEARCH,
      GEOSEARCHSTORE,
      GEORADIUSBYMEMBER,
      GEORADIUSBYMEMBER_RO,
      PFADD,
      PFCOUNT,
      PFMERGE,
      XADD,
      XLEN,
      XDEL,
      XTRIM,
      XRANGE,
      XREVRANGE,
      XREAD,
      XACK,
      XGROUP,
      XREADGROUP,
      XPENDING,
      XCLAIM,
      XAUTOCLAIM,
      XINFO,
      EVAL,
      EVALSHA,
      SCRIPT,
      EVAL_RO,
      EVALSHA_RO,
      FUNCTION,
      FCALL,
      FCALL_RO,
      SUBSCRIBE,
      UNSUBSCRIBE,
      PSUBSCRIBE,
      PUNSUBSCRIBE,
      PUBLISH,
      PUBSUB,
      SSUBSCRIBE,
      SUNSUBSCRIBE,
      SPUBLISH,
      SAVE,
      BGSAVE,
      BGREWRITEAOF,
      LASTSAVE,
      PERSIST,
      ROLE,
      FAILOVER,
      SLOWLOG,
      OBJECT,
      CLIENT,
      TIME,
      SCAN,
      HSCAN,
      SSCAN,
      ZSCAN,
      WAIT,
      CLUSTER,
      ASKING,
      READONLY,
      READWRITE,
      SLAVEOF,
      REPLICAOF,
      COPY,
      SENTINEL,
      MODULE,
      ACL,
      TOUCH,
      MEMORY,
      LOLWUT,
      COMMAND,
      RESET,
      LATENCY,
      WAITAOF;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum Keyword implements Rawable {
      AGGREGATE,
      ALPHA,
      BY,
      GET,
      LIMIT,
      NO,
      NOSORT,
      ONE,
      SET,
      STORE,
      WEIGHTS,
      WITHSCORE,
      WITHSCORES,
      RESETSTAT,
      REWRITE,
      RESET,
      FLUSH,
      EXISTS,
      LOAD,
      LEN,
      HELP,
      SCHEDULE,
      MATCH,
      COUNT,
      TYPE,
      KEYS,
      REFCOUNT,
      ENCODING,
      IDLETIME,
      FREQ,
      REPLACE,
      GETNAME,
      SETNAME,
      SETINFO,
      LIST,
      ID,
      KILL,
      PERSIST,
      STREAMS,
      CREATE,
      MKSTREAM,
      SETID,
      DESTROY,
      DELCONSUMER,
      MAXLEN,
      GROUP,
      IDLE,
      TIME,
      BLOCK,
      NOACK,
      RETRYCOUNT,
      STREAM,
      GROUPS,
      CONSUMERS,
      JUSTID,
      WITHVALUES,
      NOMKSTREAM,
      MINID,
      CREATECONSUMER,
      SETUSER,
      GETUSER,
      DELUSER,
      WHOAMI,
      USERS,
      CAT,
      GENPASS,
      LOG,
      SAVE,
      DRYRUN,
      COPY,
      AUTH,
      AUTH2,
      NX,
      XX,
      EX,
      PX,
      EXAT,
      PXAT,
      ABSTTL,
      KEEPTTL,
      INCR,
      LT,
      GT,
      CH,
      INFO,
      PAUSE,
      UNPAUSE,
      UNBLOCK,
      REV,
      WITHCOORD,
      WITHDIST,
      WITHHASH,
      ANY,
      FROMMEMBER,
      FROMLONLAT,
      BYRADIUS,
      BYBOX,
      BYLEX,
      BYSCORE,
      STOREDIST,
      TO,
      FORCE,
      TIMEOUT,
      DB,
      UNLOAD,
      ABORT,
      IDX,
      MINMATCHLEN,
      WITHMATCHLEN,
      FULL,
      DELETE,
      LIBRARYNAME,
      WITHCODE,
      DESCRIPTION,
      GETKEYS,
      GETKEYSANDFLAGS,
      DOCS,
      FILTERBY,
      DUMP,
      MODULE,
      ACLCAT,
      PATTERN,
      DOCTOR,
      LATEST,
      HISTORY,
      USAGE,
      SAMPLES,
      PURGE,
      STATS,
      LOADEX,
      CONFIG,
      ARGS,
      RANK,
      NOW,
      VERSION,
      ADDR,
      SKIPME,
      USER,
      LADDR,
      FIELDS,
      CHANNELS,
      NUMPAT,
      NUMSUB,
      SHARDCHANNELS,
      SHARDNUMSUB,
      NOVALUES,
      MAXAGE;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum ResponseKeyword implements Rawable {
      SUBSCRIBE,
      PSUBSCRIBE,
      UNSUBSCRIBE,
      PUNSUBSCRIBE,
      MESSAGE,
      PMESSAGE,
      PONG,
      SSUBSCRIBE,
      SUNSUBSCRIBE,
      SMESSAGE;

      private final byte[] raw = SafeEncoder.encode(this.name().toLowerCase(Locale.ENGLISH));

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum SentinelKeyword implements Rawable {
      MYID,
      MASTERS,
      MASTER,
      SENTINELS,
      SLAVES,
      REPLICAS,
      RESET,
      FAILOVER,
      REMOVE,
      SET,
      MONITOR,
      GET_MASTER_ADDR_BY_NAME("GET-MASTER-ADDR-BY-NAME");

      private final byte[] raw;

      SentinelKeyword() {
         this.raw = SafeEncoder.encode(this.name());
      }

      SentinelKeyword(String str) {
         this.raw = SafeEncoder.encode(str);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
