package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.security.AccessControlException;

public final class CallStackUtils {
   private static boolean canCreateSecurityManager() {
      SecurityManager manager = System.getSecurityManager();
      if (manager == null) {
         return true;
      }

      try {
         manager.checkPermission(new RuntimePermission("createSecurityManager"));
         return true;
      } catch (AccessControlException ignored) {
         return false;
      }
   }

   @Deprecated
   public static CallStack newCallStack(String messageFormat, boolean useTimestamp) {
      return newCallStack(messageFormat, useTimestamp, false);
   }

   public static CallStack newCallStack(String messageFormat, boolean useTimestamp, boolean requireFullStackTrace) {
      return canCreateSecurityManager() && !requireFullStackTrace
         ? new SecurityManagerCallStack(messageFormat, useTimestamp)
         : new ThrowableCallStack(messageFormat, useTimestamp);
   }

   private CallStackUtils() {
   }
}
