package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityManagerCallStack implements CallStack {
   private final String messageFormat;
   private final DateFormat dateFormat;
   private final SecurityManagerCallStack.PrivateSecurityManager securityManager;
   private volatile SecurityManagerCallStack.Snapshot snapshot;

   public SecurityManagerCallStack(String messageFormat, boolean useTimestamp) {
      this.messageFormat = messageFormat;
      this.dateFormat = useTimestamp ? new SimpleDateFormat(messageFormat) : null;
      this.securityManager = AccessController.doPrivileged(() -> new SecurityManagerCallStack.PrivateSecurityManager());
   }

   @Override
   public void clear() {
      this.snapshot = null;
   }

   @Override
   public void fillInStackTrace() {
      this.snapshot = new SecurityManagerCallStack.Snapshot(this.securityManager.getCallStack());
   }

   @Override
   public boolean printStackTrace(PrintWriter writer) {
      SecurityManagerCallStack.Snapshot snapshotRef = this.snapshot;
      if (snapshotRef == null) {
         return false;
      }

      String message;
      if (this.dateFormat == null) {
         message = this.messageFormat;
      } else {
         synchronized (this.dateFormat) {
            message = this.dateFormat.format(snapshotRef.timestampMillis);
         }
      }

      writer.println(message);
      snapshotRef.stack.forEach(reference -> writer.println(reference.get()));
      return true;
   }

   private static class PrivateSecurityManager extends SecurityManager {
      private PrivateSecurityManager() {
      }

      private List<WeakReference<Class<?>>> getCallStack() {
         Stream<WeakReference<Class<?>>> map = Stream.of(this.getClassContext()).map(WeakReference::new);
         return map.collect(Collectors.toList());
      }
   }

   private static class Snapshot {
      private final long timestampMillis = System.currentTimeMillis();
      private final List<WeakReference<Class<?>>> stack;

      private Snapshot(List<WeakReference<Class<?>>> stack) {
         this.stack = stack;
      }
   }
}
