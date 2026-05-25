package me.neznamy.tab.libs.org.apache.commons.pool2;

public abstract class BaseObject {
   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getClass().getSimpleName());
      builder.append(" [");
      this.toStringAppendFields(builder);
      builder.append("]");
      return builder.toString();
   }

   protected void toStringAppendFields(StringBuilder builder) {
   }
}
