package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Objects;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObject;

public class DefaultPooledObjectInfo implements DefaultPooledObjectInfoMBean {
   private static final String PATTERN = "yyyy-MM-dd HH:mm:ss Z";
   private final PooledObject<?> pooledObject;

   public DefaultPooledObjectInfo(PooledObject<?> pooledObject) {
      this.pooledObject = Objects.requireNonNull(pooledObject, "pooledObject");
   }

   @Override
   public long getBorrowedCount() {
      return this.pooledObject.getBorrowedCount();
   }

   @Override
   public long getCreateTime() {
      return this.pooledObject.getCreateInstant().toEpochMilli();
   }

   @Override
   public String getCreateTimeFormatted() {
      return this.getTimeMillisFormatted(this.getCreateTime());
   }

   @Override
   public long getLastBorrowTime() {
      return this.pooledObject.getLastBorrowInstant().toEpochMilli();
   }

   @Override
   public String getLastBorrowTimeFormatted() {
      return this.getTimeMillisFormatted(this.getLastBorrowTime());
   }

   @Override
   public String getLastBorrowTrace() {
      StringWriter sw = new StringWriter();
      this.pooledObject.printStackTrace(new PrintWriter(sw));
      return sw.toString();
   }

   @Override
   public long getLastReturnTime() {
      return this.pooledObject.getLastReturnInstant().toEpochMilli();
   }

   @Override
   public String getLastReturnTimeFormatted() {
      return this.getTimeMillisFormatted(this.getLastReturnTime());
   }

   @Override
   public String getPooledObjectToString() {
      return Objects.toString(this.pooledObject.getObject(), null);
   }

   @Override
   public String getPooledObjectType() {
      Object object = this.pooledObject.getObject();
      return object != null ? object.getClass().getName() : null;
   }

   private String getTimeMillisFormatted(long millis) {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(millis);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("DefaultPooledObjectInfo [pooledObject=");
      builder.append(this.pooledObject);
      builder.append("]");
      return builder.toString();
   }
}
