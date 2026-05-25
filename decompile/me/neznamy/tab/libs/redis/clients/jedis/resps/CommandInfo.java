package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;

public class CommandInfo {
   private final long arity;
   private final List<String> flags;
   private final long firstKey;
   private final long lastKey;
   private final long step;
   private final List<String> aclCategories;
   private final List<String> tips;
   private final List<String> subcommands;
   public static final Builder<CommandInfo> COMMAND_INFO_BUILDER = new Builder<CommandInfo>() {
      public CommandInfo build(Object data) {
         List<Object> commandData = (List<Object>)data;
         long arity = BuilderFactory.LONG.build(commandData.get(1));
         List<String> flags = BuilderFactory.STRING_LIST.build(commandData.get(2));
         long firstKey = BuilderFactory.LONG.build(commandData.get(3));
         long lastKey = BuilderFactory.LONG.build(commandData.get(4));
         long step = BuilderFactory.LONG.build(commandData.get(5));
         List<String> aclCategories = BuilderFactory.STRING_LIST.build(commandData.get(6));
         List<String> tips = BuilderFactory.STRING_LIST.build(commandData.get(7));
         List<String> subcommands = BuilderFactory.STRING_LIST.build(commandData.get(9));
         return new CommandInfo(arity, flags, firstKey, lastKey, step, aclCategories, tips, subcommands);
      }
   };

   public CommandInfo(
      long arity, List<String> flags, long firstKey, long lastKey, long step, List<String> aclCategories, List<String> tips, List<String> subcommands
   ) {
      this.arity = arity;
      this.flags = flags;
      this.firstKey = firstKey;
      this.lastKey = lastKey;
      this.step = step;
      this.aclCategories = aclCategories;
      this.tips = tips;
      this.subcommands = subcommands;
   }

   public long getArity() {
      return this.arity;
   }

   public List<String> getFlags() {
      return this.flags;
   }

   public long getFirstKey() {
      return this.firstKey;
   }

   public long getLastKey() {
      return this.lastKey;
   }

   public long getStep() {
      return this.step;
   }

   public List<String> getAclCategories() {
      return this.aclCategories;
   }

   public List<String> getTips() {
      return this.tips;
   }

   public List<String> getSubcommands() {
      return this.subcommands;
   }
}
