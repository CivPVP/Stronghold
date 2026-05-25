package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public class OptionalNode extends IntersectNode {
   @Override
   public String toString(Node.Parenthesize mode) {
      String ret = super.toString(Node.Parenthesize.NEVER);
      return this.shouldParenthesize(mode) ? "~(" + ret + ")" : "~" + ret;
   }
}
