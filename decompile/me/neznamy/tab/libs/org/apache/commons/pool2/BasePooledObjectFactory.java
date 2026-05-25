package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.util.Objects;

public abstract class BasePooledObjectFactory<T> extends BaseObject implements PooledObjectFactory<T> {
   @Override
   public void activateObject(PooledObject<T> p) throws Exception {
   }

   public abstract T create() throws Exception;

   @Override
   public void destroyObject(PooledObject<T> p) throws Exception {
   }

   @Override
   public PooledObject<T> makeObject() throws Exception {
      return this.wrap(Objects.requireNonNull(this.create(), () -> String.format("BasePooledObjectFactory(%s).create() = null", this.getClass().getName())));
   }

   @Override
   public void passivateObject(PooledObject<T> p) throws Exception {
   }

   @Override
   public boolean validateObject(PooledObject<T> p) {
      return true;
   }

   public abstract PooledObject<T> wrap(T var1);
}
