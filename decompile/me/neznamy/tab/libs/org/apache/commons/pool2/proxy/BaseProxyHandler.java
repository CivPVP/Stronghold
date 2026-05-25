package me.neznamy.tab.libs.org.apache.commons.pool2.proxy;

import java.lang.reflect.Method;
import me.neznamy.tab.libs.org.apache.commons.pool2.UsageTracking;

class BaseProxyHandler<T> {
   private volatile T pooledObject;
   private final UsageTracking<T> usageTracking;

   BaseProxyHandler(T pooledObject, UsageTracking<T> usageTracking) {
      this.pooledObject = pooledObject;
      this.usageTracking = usageTracking;
   }

   T disableProxy() {
      T result = this.pooledObject;
      this.pooledObject = null;
      return result;
   }

   Object doInvoke(Method method, Object[] args) throws Throwable {
      this.validateProxiedObject();
      T object = this.getPooledObject();
      if (this.usageTracking != null) {
         this.usageTracking.use(object);
      }

      return method.invoke(object, args);
   }

   T getPooledObject() {
      return this.pooledObject;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getClass().getName());
      builder.append(" [pooledObject=");
      builder.append(this.pooledObject);
      builder.append(", usageTracking=");
      builder.append(this.usageTracking);
      builder.append("]");
      return builder.toString();
   }

   void validateProxiedObject() {
      if (this.pooledObject == null) {
         throw new IllegalStateException("This object may no longer be used as it has been returned to the Object Pool.");
      }
   }
}
