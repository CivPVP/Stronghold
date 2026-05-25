package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.lang.ref.SoftReference;

public class PooledSoftReference<T> extends DefaultPooledObject<T> {
   private volatile SoftReference<T> reference;

   public PooledSoftReference(SoftReference<T> reference) {
      super(null);
      this.reference = reference;
   }

   @Override
   public T getObject() {
      return this.reference.get();
   }

   public synchronized SoftReference<T> getReference() {
      return this.reference;
   }

   public synchronized void setReference(SoftReference<T> reference) {
      this.reference = reference;
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder();
      result.append("Referenced Object: ");
      result.append(this.getObject().toString());
      result.append(", State: ");
      synchronized (this) {
         result.append(this.getState().toString());
      }

      return result.toString();
   }
}
