package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

public interface DefaultPooledObjectInfoMBean {
   long getBorrowedCount();

   long getCreateTime();

   String getCreateTimeFormatted();

   long getLastBorrowTime();

   String getLastBorrowTimeFormatted();

   String getLastBorrowTrace();

   long getLastReturnTime();

   String getLastReturnTimeFormatted();

   String getPooledObjectToString();

   String getPooledObjectType();
}
