package me.neznamy.tab.libs.org.apache.commons.pool2.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import me.neznamy.tab.libs.org.apache.commons.pool2.UsageTracking;

public class JdkProxySource<T> implements ProxySource<T> {
   private final ClassLoader classLoader;
   private final Class<?>[] interfaces;

   public JdkProxySource(ClassLoader classLoader, Class<?>[] interfaces) {
      this.classLoader = classLoader;
      this.interfaces = Arrays.copyOf(interfaces, interfaces.length);
   }

   @Override
   public T createProxy(T pooledObject, UsageTracking<T> usageTracking) {
      return (T)Proxy.newProxyInstance(this.classLoader, this.interfaces, new JdkProxyHandler<>(pooledObject, usageTracking));
   }

   @Override
   public T resolveProxy(T proxy) {
      return (T)((JdkProxyHandler)Proxy.getInvocationHandler(proxy)).disableProxy();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("JdkProxySource [classLoader=");
      builder.append(this.classLoader);
      builder.append(", interfaces=");
      builder.append(Arrays.toString(this.interfaces));
      builder.append("]");
      return builder.toString();
   }
}
