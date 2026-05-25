package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public interface Node {
   String toString(Node.Parenthesize var1);

   @Override
   String toString();

   enum Parenthesize {
      ALWAYS,
      NEVER,
      DEFAULT;
   }
}
