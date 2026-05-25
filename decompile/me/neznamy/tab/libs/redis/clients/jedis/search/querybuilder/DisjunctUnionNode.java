package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public class DisjunctUnionNode extends DisjunctNode {
   @Override
   protected String getJoinString() {
      return "|";
   }
}
