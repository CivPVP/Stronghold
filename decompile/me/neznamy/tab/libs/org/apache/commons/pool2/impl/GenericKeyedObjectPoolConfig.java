package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

public class GenericKeyedObjectPoolConfig<T> extends BaseObjectPoolConfig<T> {
   public static final int DEFAULT_MAX_TOTAL_PER_KEY = 8;
   public static final int DEFAULT_MAX_TOTAL = -1;
   public static final int DEFAULT_MIN_IDLE_PER_KEY = 0;
   public static final int DEFAULT_MAX_IDLE_PER_KEY = 8;
   private int minIdlePerKey = 0;
   private int maxIdlePerKey = 8;
   private int maxTotalPerKey = 8;
   private int maxTotal = -1;

   public GenericKeyedObjectPoolConfig<T> clone() {
      try {
         return (GenericKeyedObjectPoolConfig<T>)super.clone();
      } catch (CloneNotSupportedException e) {
         throw new AssertionError();
      }
   }

   public int getMaxIdlePerKey() {
      return this.maxIdlePerKey;
   }

   public int getMaxTotal() {
      return this.maxTotal;
   }

   public int getMaxTotalPerKey() {
      return this.maxTotalPerKey;
   }

   public int getMinIdlePerKey() {
      return this.minIdlePerKey;
   }

   public void setMaxIdlePerKey(int maxIdlePerKey) {
      this.maxIdlePerKey = maxIdlePerKey;
   }

   public void setMaxTotal(int maxTotal) {
      this.maxTotal = maxTotal;
   }

   public void setMaxTotalPerKey(int maxTotalPerKey) {
      this.maxTotalPerKey = maxTotalPerKey;
   }

   public void setMinIdlePerKey(int minIdlePerKey) {
      this.minIdlePerKey = minIdlePerKey;
   }

   @Override
   protected void toStringAppendFields(StringBuilder builder) {
      super.toStringAppendFields(builder);
      builder.append(", minIdlePerKey=");
      builder.append(this.minIdlePerKey);
      builder.append(", maxIdlePerKey=");
      builder.append(this.maxIdlePerKey);
      builder.append(", maxTotalPerKey=");
      builder.append(this.maxTotalPerKey);
      builder.append(", maxTotal=");
      builder.append(this.maxTotal);
   }
}
