package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.concurrent.ThreadFactory;

public class Environment {
   @Deprecated
   public static boolean isAllowedToModifyThreads() {
      return true;
   }

   public static Thread newThread(ThreadFactory factory, Runnable runnable, String name) {
      Thread t = factory.newThread(runnable);
      t.setName(name);
      return t;
   }

   public static Thread newThread(ThreadFactory factory, Runnable runnable, String name, boolean isDaemon) {
      Thread t = newThread(factory, runnable, name);
      t.setDaemon(isDaemon);
      return t;
   }
}
