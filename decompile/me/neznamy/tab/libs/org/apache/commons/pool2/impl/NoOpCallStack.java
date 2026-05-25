package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.PrintWriter;

public class NoOpCallStack implements CallStack {
   public static final CallStack INSTANCE = new NoOpCallStack();

   private NoOpCallStack() {
   }

   @Override
   public void clear() {
   }

   @Override
   public void fillInStackTrace() {
   }

   @Override
   public boolean printStackTrace(PrintWriter writer) {
      return false;
   }
}
