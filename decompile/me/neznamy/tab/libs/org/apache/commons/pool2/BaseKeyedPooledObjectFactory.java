package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.util.Objects;

public abstract class BaseKeyedPooledObjectFactory<K, V> extends BaseObject implements KeyedPooledObjectFactory<K, V> {
   @Override
   public void activateObject(K key, PooledObject<V> p) throws Exception {
   }

   public abstract V create(K var1) throws Exception;

   @Override
   public void destroyObject(K key, PooledObject<V> p) throws Exception {
   }

   @Override
   public PooledObject<V> makeObject(K key) throws Exception {
      return this.wrap(
         Objects.requireNonNull(this.create(key), () -> String.format("BaseKeyedPooledObjectFactory(%s).create(key=%s) = null", this.getClass().getName(), key))
      );
   }

   @Override
   public void passivateObject(K key, PooledObject<V> p) throws Exception {
   }

   @Override
   public boolean validateObject(K key, PooledObject<V> p) {
      return true;
   }

   public abstract PooledObject<V> wrap(V var1);
}
