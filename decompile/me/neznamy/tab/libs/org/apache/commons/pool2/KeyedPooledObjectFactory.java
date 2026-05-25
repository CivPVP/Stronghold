package me.neznamy.tab.libs.org.apache.commons.pool2;

public interface KeyedPooledObjectFactory<K, V> {
   void activateObject(K var1, PooledObject<V> var2) throws Exception;

   void destroyObject(K var1, PooledObject<V> var2) throws Exception;

   default void destroyObject(K key, PooledObject<V> p, DestroyMode destroyMode) throws Exception {
      this.destroyObject(key, p);
   }

   PooledObject<V> makeObject(K var1) throws Exception;

   void passivateObject(K var1, PooledObject<V> var2) throws Exception;

   boolean validateObject(K var1, PooledObject<V> var2);
}
