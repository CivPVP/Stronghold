package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class CommandListFilterByParams implements IParams {
   private String moduleName;
   private String category;
   private String pattern;

   public static CommandListFilterByParams commandListFilterByParams() {
      return new CommandListFilterByParams();
   }

   public CommandListFilterByParams filterByModule(String moduleName) {
      this.moduleName = moduleName;
      return this;
   }

   public CommandListFilterByParams filterByAclCat(String category) {
      this.category = category;
      return this;
   }

   public CommandListFilterByParams filterByPattern(String pattern) {
      this.pattern = pattern;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.add(Protocol.Keyword.FILTERBY);
      if (this.moduleName != null && this.category == null && this.pattern == null) {
         args.add(Protocol.Keyword.MODULE);
         args.add(this.moduleName);
      } else if (this.moduleName == null && this.category != null && this.pattern == null) {
         args.add(Protocol.Keyword.ACLCAT);
         args.add(this.category);
      } else {
         if (this.moduleName != null || this.category != null || this.pattern == null) {
            throw new IllegalArgumentException("Must choose exactly one filter in " + this.getClass().getSimpleName());
         }

         args.add(Protocol.Keyword.PATTERN);
         args.add(this.pattern);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommandListFilterByParams that = (CommandListFilterByParams)o;
         return Objects.equals(this.moduleName, that.moduleName) && Objects.equals(this.category, that.category) && Objects.equals(this.pattern, that.pattern);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.moduleName, this.category, this.pattern);
   }
}
