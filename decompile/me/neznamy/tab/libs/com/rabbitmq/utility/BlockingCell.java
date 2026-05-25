package me.neznamy.tab.libs.com.rabbitmq.utility;

import java.util.concurrent.TimeoutException;

public class BlockingCell<T> {
   private boolean _filled = false;
   private T _value;
   private static final long NANOS_IN_MILLI = 1000000L;
   private static final long INFINITY = -1L;

   public synchronized T get() throws InterruptedException {
      while (!this._filled) {
         this.wait();
      }

      return this._value;
   }

   public synchronized T get(long timeout) throws InterruptedException, TimeoutException {
      if (timeout == -1L) {
         return this.get();
      }

      if (timeout < 0L) {
         throw new IllegalArgumentException("Timeout cannot be less than zero");
      }

      long now = System.nanoTime() / 1000000L;
      long maxTime = now + timeout;

      while (!this._filled && (now = System.nanoTime() / 1000000L) < maxTime) {
         this.wait(maxTime - now);
      }

      if (!this._filled) {
         throw new TimeoutException();
      } else {
         return this._value;
      }
   }

   public synchronized T uninterruptibleGet() {
      boolean wasInterrupted = false;

      try {
         while (true) {
            try {
               return this.get();
            } catch (InterruptedException ex) {
               wasInterrupted = true;
            }
         }
      } finally {
         if (wasInterrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   public synchronized T uninterruptibleGet(int timeout) throws TimeoutException {
      long now = System.nanoTime() / 1000000L;
      long runTime = now + timeout;
      boolean wasInterrupted = false;

      try {
         while (true) {
            try {
               return this.get(runTime - now);
            } catch (InterruptedException e) {
               wasInterrupted = true;
               if (timeout != -1L && (now = System.nanoTime() / 1000000L) >= runTime) {
                  throw new TimeoutException();
               }
            }
         }
      } finally {
         if (wasInterrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   public synchronized void set(T newValue) {
      if (this._filled) {
         throw new IllegalStateException("BlockingCell can only be set once");
      }

      this._value = newValue;
      this._filled = true;
      this.notifyAll();
   }

   public synchronized boolean setIfUnset(T newValue) {
      if (this._filled) {
         return false;
      }

      this.set(newValue);
      return true;
   }
}
