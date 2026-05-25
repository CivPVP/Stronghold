package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface KeyedObjectPool<K, V> extends Closeable {
   void addObject(K var1) throws Exception;

   default void addObjects(Collection<K> keys, int count) throws Exception {
      if (keys == null) {
         throw new IllegalArgumentException("keys must not be null.");
      }

      for (K key : keys) {
         this.addObjects(key, count);
      }
   }

   default void addObjects(K key, int count) throws Exception {
      if (key == null) {
         throw new IllegalArgumentException("key must not be null.");
      }

      for (int i = 0; i < count; i++) {
         this.addObject(key);
      }
   }

   V borrowObject(K var1) throws Exception;

   void clear() throws Exception;

   void clear(K var1) throws Exception;

   @Override
   void close();

   default List<K> getKeys() {
      return Collections.emptyList();
   }

   int getNumActive();

   int getNumActive(K var1);

   int getNumIdle();

   int getNumIdle(K var1);

   void invalidateObject(K var1, V var2) throws Exception;

   default void invalidateObject(K key, V obj, DestroyMode destroyMode) throws Exception {
      this.invalidateObject(key, obj);
   }

   void returnObject(K var1, V var2) throws Exception;
}
