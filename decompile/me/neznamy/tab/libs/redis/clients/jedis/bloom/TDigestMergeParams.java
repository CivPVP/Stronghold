package me.neznamy.tab.libs.redis.clients.jedis.bloom;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class TDigestMergeParams implements IParams {
   private Integer compression;
   private boolean override = false;

   public static TDigestMergeParams mergeParams() {
      return new TDigestMergeParams();
   }

   public TDigestMergeParams compression(int compression) {
      this.compression = compression;
      return this;
   }

   public TDigestMergeParams override() {
      this.override = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.compression != null) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.COMPRESSION).add(Protocol.toByteArray(this.compression));
      }

      if (this.override) {
         args.add(RedisBloomProtocol.RedisBloomKeyword.OVERRIDE);
      }
   }
}
