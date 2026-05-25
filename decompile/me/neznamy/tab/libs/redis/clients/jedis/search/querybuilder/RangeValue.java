package me.neznamy.tab.libs.redis.clients.jedis.search.querybuilder;

public abstract class RangeValue extends Value {
   private boolean inclusiveMin = true;
   private boolean inclusiveMax = true;

   @Override
   public boolean isCombinable() {
      return false;
   }

   protected abstract void appendFrom(StringBuilder var1, boolean var2);

   protected abstract void appendTo(StringBuilder var1, boolean var2);

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      this.appendFrom(sb, this.inclusiveMin);
      sb.append(' ');
      this.appendTo(sb, this.inclusiveMax);
      sb.append(']');
      return sb.toString();
   }

   public RangeValue inclusiveMin(boolean val) {
      this.inclusiveMin = val;
      return this;
   }

   public RangeValue inclusiveMax(boolean val) {
      this.inclusiveMax = val;
      return this;
   }
}
