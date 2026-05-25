package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;

public interface PooledObject<T> extends Comparable<PooledObject<T>> {
   static boolean isNull(PooledObject<?> pooledObject) {
      return pooledObject == null || pooledObject.getObject() == null;
   }

   boolean allocate();

   int compareTo(PooledObject<T> var1);

   boolean deallocate();

   boolean endEvictionTest(Deque<PooledObject<T>> var1);

   @Override
   boolean equals(Object var1);

   default Duration getActiveDuration() {
      Instant lastReturnInstant = this.getLastReturnInstant();
      Instant lastBorrowInstant = this.getLastBorrowInstant();
      return lastReturnInstant.isAfter(lastBorrowInstant)
         ? Duration.between(lastBorrowInstant, lastReturnInstant)
         : Duration.between(lastBorrowInstant, Instant.now());
   }

   @Deprecated
   default Duration getActiveTime() {
      return this.getActiveDuration();
   }

   @Deprecated
   long getActiveTimeMillis();

   default long getBorrowedCount() {
      return -1L;
   }

   default Instant getCreateInstant() {
      return Instant.ofEpochMilli(this.getCreateTime());
   }

   @Deprecated
   long getCreateTime();

   default Duration getFullDuration() {
      return Duration.between(this.getCreateInstant(), Instant.now());
   }

   default Duration getIdleDuration() {
      return Duration.ofMillis(this.getIdleTimeMillis());
   }

   @Deprecated
   default Duration getIdleTime() {
      return Duration.ofMillis(this.getIdleTimeMillis());
   }

   @Deprecated
   long getIdleTimeMillis();

   default Instant getLastBorrowInstant() {
      return Instant.ofEpochMilli(this.getLastBorrowTime());
   }

   @Deprecated
   long getLastBorrowTime();

   default Instant getLastReturnInstant() {
      return Instant.ofEpochMilli(this.getLastReturnTime());
   }

   @Deprecated
   long getLastReturnTime();

   default Instant getLastUsedInstant() {
      return Instant.ofEpochMilli(this.getLastUsedTime());
   }

   @Deprecated
   long getLastUsedTime();

   T getObject();

   PooledObjectState getState();

   @Override
   int hashCode();

   void invalidate();

   void markAbandoned();

   void markReturning();

   void printStackTrace(PrintWriter var1);

   void setLogAbandoned(boolean var1);

   default void setRequireFullStackTrace(boolean requireFullStackTrace) {
   }

   boolean startEvictionTest();

   @Override
   String toString();

   void use();
}
