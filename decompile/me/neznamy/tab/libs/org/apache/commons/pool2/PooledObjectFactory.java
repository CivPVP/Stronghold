package me.neznamy.tab.libs.org.apache.commons.pool2;

public interface PooledObjectFactory<T> {
   void activateObject(PooledObject<T> var1) throws Exception;

   void destroyObject(PooledObject<T> var1) throws Exception;

   default void destroyObject(PooledObject<T> p, DestroyMode destroyMode) throws Exception {
      this.destroyObject(p);
   }

   PooledObject<T> makeObject() throws Exception;

   void passivateObject(PooledObject<T> var1) throws Exception;

   boolean validateObject(PooledObject<T> var1);
}
