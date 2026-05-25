package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public class UnionNode extends QueryNode {
   @Override
   protected String getJoinString() {
      return "|";
   }
}
