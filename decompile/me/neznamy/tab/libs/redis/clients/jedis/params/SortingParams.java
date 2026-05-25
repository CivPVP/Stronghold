package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortingOrder;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class SortingParams implements IParams {
   private final List<Object> params = new ArrayList<>();

   public SortingParams by(String pattern) {
      return this.by(SafeEncoder.encode(pattern));
   }

   public SortingParams by(byte[] pattern) {
      this.params.add(Protocol.Keyword.BY);
      this.params.add(pattern);
      return this;
   }

   public SortingParams nosort() {
      this.params.add(Protocol.Keyword.BY);
      this.params.add(Protocol.Keyword.NOSORT);
      return this;
   }

   public SortingParams desc() {
      return this.sortingOrder(SortingOrder.DESC);
   }

   public SortingParams asc() {
      return this.sortingOrder(SortingOrder.ASC);
   }

   public SortingParams sortingOrder(SortingOrder order) {
      this.params.add(order.getRaw());
      return this;
   }

   public SortingParams limit(int start, int count) {
      this.params.add(Protocol.Keyword.LIMIT);
      this.params.add(start);
      this.params.add(count);
      return this;
   }

   public SortingParams alpha() {
      this.params.add(Protocol.Keyword.ALPHA);
      return this;
   }

   public SortingParams get(String... patterns) {
      for (String pattern : patterns) {
         this.params.add(Protocol.Keyword.GET);
         this.params.add(pattern);
      }

      return this;
   }

   public SortingParams get(byte[]... patterns) {
      for (byte[] pattern : patterns) {
         this.params.add(Protocol.Keyword.GET);
         this.params.add(pattern);
      }

      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addObjects(this.params);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SortingParams that = (SortingParams)o;
         return Objects.equals(this.params, that.params);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.params);
   }
}
