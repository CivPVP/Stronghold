package me.neznamy.tab.libs.redis.clients.jedis.bloom;

import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class RedisBloomProtocol {
   public enum BloomFilterCommand implements ProtocolCommand {
      RESERVE("BF.RESERVE"),
      ADD("BF.ADD"),
      MADD("BF.MADD"),
      EXISTS("BF.EXISTS"),
      MEXISTS("BF.MEXISTS"),
      INSERT("BF.INSERT"),
      SCANDUMP("BF.SCANDUMP"),
      LOADCHUNK("BF.LOADCHUNK"),
      CARD("BF.CARD"),
      INFO("BF.INFO");

      private final byte[] raw;

      BloomFilterCommand(String alt) {
         this.raw = SafeEncoder.encode(alt);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum CountMinSketchCommand implements ProtocolCommand {
      INITBYDIM("CMS.INITBYDIM"),
      INITBYPROB("CMS.INITBYPROB"),
      INCRBY("CMS.INCRBY"),
      QUERY("CMS.QUERY"),
      MERGE("CMS.MERGE"),
      INFO("CMS.INFO");

      private final byte[] raw;

      CountMinSketchCommand(String alt) {
         this.raw = SafeEncoder.encode(alt);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum CuckooFilterCommand implements ProtocolCommand {
      RESERVE("CF.RESERVE"),
      ADD("CF.ADD"),
      ADDNX("CF.ADDNX"),
      INSERT("CF.INSERT"),
      INSERTNX("CF.INSERTNX"),
      EXISTS("CF.EXISTS"),
      MEXISTS("CF.MEXISTS"),
      DEL("CF.DEL"),
      COUNT("CF.COUNT"),
      SCANDUMP("CF.SCANDUMP"),
      LOADCHUNK("CF.LOADCHUNK"),
      INFO("CF.INFO");

      private final byte[] raw;

      CuckooFilterCommand(String alt) {
         this.raw = SafeEncoder.encode(alt);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum RedisBloomKeyword implements Rawable {
      CAPACITY,
      ERROR,
      NOCREATE,
      EXPANSION,
      NONSCALING,
      BUCKETSIZE,
      MAXITERATIONS,
      ITEMS,
      WEIGHTS,
      COMPRESSION,
      OVERRIDE,
      WITHCOUNT;

      private final byte[] raw = SafeEncoder.encode(this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum TDigestCommand implements ProtocolCommand {
      CREATE,
      INFO,
      ADD,
      RESET,
      MERGE,
      CDF,
      QUANTILE,
      MIN,
      MAX,
      TRIMMED_MEAN,
      RANK,
      REVRANK,
      BYRANK,
      BYREVRANK;

      private final byte[] raw = SafeEncoder.encode("TDIGEST." + this.name());

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }

   public enum TopKCommand implements ProtocolCommand {
      RESERVE("TOPK.RESERVE"),
      ADD("TOPK.ADD"),
      INCRBY("TOPK.INCRBY"),
      QUERY("TOPK.QUERY"),
      LIST("TOPK.LIST"),
      INFO("TOPK.INFO");

      private final byte[] raw;

      TopKCommand(String alt) {
         this.raw = SafeEncoder.encode(alt);
      }

      @Override
      public byte[] getRaw() {
         return this.raw;
      }
   }
}
