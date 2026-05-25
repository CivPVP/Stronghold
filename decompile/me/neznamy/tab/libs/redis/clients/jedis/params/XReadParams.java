package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class XReadParams implements IParams {
   private Integer count = null;
   private Integer block = null;

   public static XReadParams xReadParams() {
      return new XReadParams();
   }

   public XReadParams count(int count) {
      this.count = count;
      return this;
   }

   public XReadParams block(int block) {
      this.block = block;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.count != null) {
         args.add(Protocol.Keyword.COUNT).add(this.count);
      }

      if (this.block != null) {
         args.add(Protocol.Keyword.BLOCK).add(this.block).blocking();
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XReadParams that = (XReadParams)o;
         return Objects.equals(this.count, that.count) && Objects.equals(this.block, that.block);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.count, this.block);
   }
}
