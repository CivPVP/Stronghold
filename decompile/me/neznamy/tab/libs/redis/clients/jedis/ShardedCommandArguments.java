package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.Hashing;

@Deprecated
public class ShardedCommandArguments extends CommandArguments {
   private final Hashing algo;
   private final Pattern tagPattern;
   private Long keyHash = null;

   public ShardedCommandArguments(Hashing algo, ProtocolCommand command) {
      this(algo, null, command);
   }

   public ShardedCommandArguments(Hashing algo, Pattern tagPattern, ProtocolCommand command) {
      super(command);
      this.algo = algo;
      this.tagPattern = tagPattern;
   }

   public Long getKeyHash() {
      return this.keyHash;
   }

   @Override
   protected CommandArguments processKey(byte[] key) {
      long hash = this.algo.hash(key);
      if (this.keyHash == null) {
         this.keyHash = hash;
      } else if (this.keyHash != hash) {
         throw new JedisException("Keys must generate same hash.");
      }

      return this;
   }

   @Override
   protected CommandArguments processKey(String key) {
      key = this.getKeyTag(key);
      long hash = this.algo.hash(key);
      if (this.keyHash == null) {
         this.keyHash = hash;
      } else if (this.keyHash != hash) {
         throw new JedisException("Keys must generate same hash.");
      }

      return this;
   }

   private String getKeyTag(String key) {
      if (this.tagPattern != null) {
         Matcher m = this.tagPattern.matcher(key);
         if (m.find()) {
            return m.group(1);
         }
      }

      return key;
   }
}
