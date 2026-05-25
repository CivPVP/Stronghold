package me.neznamy.tab.libs.redis.clients.jedis.search.aggr;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;

public abstract class Reducer {
   private final String name;
   private final String field;
   private String alias;

   protected Reducer(String name) {
      this.name = name;
      this.field = null;
   }

   protected Reducer(String name, String field) {
      this.name = name;
      this.field = field;
   }

   public final Reducer as(String alias) {
      this.alias = alias;
      return this;
   }

   public final String getName() {
      return this.name;
   }

   public final String getField() {
      return this.field;
   }

   public final String getAlias() {
      return this.alias;
   }

   protected abstract List<Object> getOwnArgs();

   public final void addArgs(List<Object> args) {
      args.add(SearchProtocol.SearchKeyword.REDUCE);
      args.add(this.name);
      List<Object> ownArgs = this.getOwnArgs();
      if (this.field != null) {
         args.add(1 + ownArgs.size());
         args.add(this.field);
      } else {
         args.add(ownArgs.size());
      }

      args.addAll(ownArgs);
      if (this.alias != null) {
         args.add(SearchProtocol.SearchKeyword.AS);
         args.add(this.alias);
      }
   }
}
