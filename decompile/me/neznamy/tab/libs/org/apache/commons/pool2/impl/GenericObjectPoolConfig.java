package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

public class GenericObjectPoolConfig<T> extends BaseObjectPoolConfig<T> {
   public static final int DEFAULT_MAX_TOTAL = 8;
   public static final int DEFAULT_MAX_IDLE = 8;
   public static final int DEFAULT_MIN_IDLE = 0;
   private int maxTotal = 8;
   private int maxIdle = 8;
   private int minIdle = 0;

   public GenericObjectPoolConfig<T> clone() {
      try {
         return (GenericObjectPoolConfig<T>)super.clone();
      } catch (CloneNotSupportedException e) {
         throw new AssertionError();
      }
   }

   public int getMaxIdle() {
      return this.maxIdle;
   }

   public int getMaxTotal() {
      return this.maxTotal;
   }

   public int getMinIdle() {
      return this.minIdle;
   }

   public void setMaxIdle(int maxIdle) {
      this.maxIdle = maxIdle;
   }

   public void setMaxTotal(int maxTotal) {
      this.maxTotal = maxTotal;
   }

   public void setMinIdle(int minIdle) {
      this.minIdle = minIdle;
   }

   @Override
   protected void toStringAppendFields(StringBuilder builder) {
      super.toStringAppendFields(builder);
      builder.append(", maxTotal=");
      builder.append(this.maxTotal);
      builder.append(", maxIdle=");
      builder.append(this.maxIdle);
      builder.append(", minIdle=");
      builder.append(this.minIdle);
   }
}
