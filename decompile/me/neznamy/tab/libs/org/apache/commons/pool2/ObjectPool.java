package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.io.Closeable;

public interface ObjectPool<T> extends Closeable {
   void addObject() throws Exception;

   default void addObjects(int count) throws Exception {
      for (int i = 0; i < count; i++) {
         this.addObject();
      }
   }

   T borrowObject() throws Exception;

   void clear() throws Exception;

   @Override
   void close();

   int getNumActive();

   int getNumIdle();

   void invalidateObject(T var1) throws Exception;

   default void invalidateObject(T obj, DestroyMode destroyMode) throws Exception {
      this.invalidateObject(obj);
   }

   void returnObject(T var1) throws Exception;
}
