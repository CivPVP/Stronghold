package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public class DisjunctNode extends IntersectNode {
   @Override
   public String toString(Node.Parenthesize mode) {
      String ret = super.toString(Node.Parenthesize.NEVER);
      return this.shouldParenthesize(mode) ? "-(" + ret + ")" : "-" + ret;
   }
}
