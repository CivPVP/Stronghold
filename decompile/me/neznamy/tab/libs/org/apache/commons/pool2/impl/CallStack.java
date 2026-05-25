package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.PrintWriter;

public interface CallStack {
   void clear();

   void fillInStackTrace();

   boolean printStackTrace(PrintWriter var1);
}
