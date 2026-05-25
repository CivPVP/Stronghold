package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public abstract class Value {
   public boolean isCombinable() {
      return false;
   }

   @Override
   public abstract String toString();
}
