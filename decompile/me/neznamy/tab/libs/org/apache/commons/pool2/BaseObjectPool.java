package me.neznamy.tab.libs.org.apache.commons.pool2;

public abstract class BaseObjectPool<T> extends BaseObject implements ObjectPool<T> {
   private volatile boolean closed;

   @Override
   public void addObject() throws Exception {
      throw new UnsupportedOperationException();
   }

   protected final void assertOpen() throws IllegalStateException {
      if (this.isClosed()) {
         throw new IllegalStateException("Pool not open");
      }
   }

   @Override
   public abstract T borrowObject() throws Exception;

   @Override
   public void clear() throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   public void close() {
      this.closed = true;
   }

   @Override
   public int getNumActive() {
      return -1;
   }

   @Override
   public int getNumIdle() {
      return -1;
   }

   @Override
   public abstract void invalidateObject(T var1) throws Exception;

   public final boolean isClosed() {
      return this.closed;
   }

   @Override
   public abstract void returnObject(T var1) throws Exception;

   @Override
   protected void toStringAppendFields(StringBuilder builder) {
      builder.append("closed=");
      builder.append(this.closed);
   }
}
