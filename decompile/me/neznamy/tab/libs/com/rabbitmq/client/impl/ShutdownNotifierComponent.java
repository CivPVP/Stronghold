package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownListener;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownNotifier;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;

public class ShutdownNotifierComponent implements ShutdownNotifier {
   private final Object monitor = new Object();
   private final List<ShutdownListener> shutdownListeners = new ArrayList<>();
   private volatile ShutdownSignalException shutdownCause = null;

   @Override
   public void addShutdownListener(ShutdownListener listener) {
      ShutdownSignalException sse = null;
      synchronized (this.monitor) {
         sse = this.shutdownCause;
         this.shutdownListeners.add(listener);
      }

      if (sse != null) {
         listener.shutdownCompleted(sse);
      }
   }

   @Override
   public ShutdownSignalException getCloseReason() {
      synchronized (this.monitor) {
         return this.shutdownCause;
      }
   }

   @Override
   public void notifyListeners() {
      ShutdownSignalException sse = null;
      ShutdownListener[] sdls = null;
      synchronized (this.monitor) {
         sdls = this.shutdownListeners.toArray(new ShutdownListener[this.shutdownListeners.size()]);
         sse = this.shutdownCause;
      }

      for (ShutdownListener l : sdls) {
         try {
            l.shutdownCompleted(sse);
         } catch (Exception var8) {
         }
      }
   }

   @Override
   public void removeShutdownListener(ShutdownListener listener) {
      synchronized (this.monitor) {
         this.shutdownListeners.remove(listener);
      }
   }

   @Override
   public boolean isOpen() {
      synchronized (this.monitor) {
         return this.shutdownCause == null;
      }
   }

   public boolean setShutdownCauseIfOpen(ShutdownSignalException sse) {
      synchronized (this.monitor) {
         if (this.isOpen()) {
            this.shutdownCause = sse;
            return true;
         } else {
            return false;
         }
      }
   }
}
