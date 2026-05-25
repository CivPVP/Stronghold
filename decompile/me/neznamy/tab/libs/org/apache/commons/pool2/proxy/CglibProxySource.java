package me.neznamy.tab.libs.org.apache.commons.pool2.proxy;

import me.neznamy.tab.libs.org.apache.commons.pool2.UsageTracking;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

public class CglibProxySource<T> implements ProxySource<T> {
   private final Class<? extends T> superclass;

   public CglibProxySource(Class<? extends T> superclass) {
      this.superclass = superclass;
   }

   @Override
   public T createProxy(T pooledObject, UsageTracking<T> usageTracking) {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(this.superclass);
      CglibProxyHandler<T> proxyInterceptor = new CglibProxyHandler<>(pooledObject, usageTracking);
      enhancer.setCallback(proxyInterceptor);
      return (T)enhancer.create();
   }

   @Override
   public T resolveProxy(T proxy) {
      CglibProxyHandler<T> cglibProxyHandler = (CglibProxyHandler<T>)((Factory)proxy).getCallback(0);
      return cglibProxyHandler.disableProxy();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("CglibProxySource [superclass=");
      builder.append(this.superclass);
      builder.append("]");
      return builder.toString();
   }
}
