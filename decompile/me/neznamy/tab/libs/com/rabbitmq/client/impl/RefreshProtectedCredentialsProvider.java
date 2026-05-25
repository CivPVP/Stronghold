package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RefreshProtectedCredentialsProvider<T> implements CredentialsProvider {
   private static final Logger LOGGER = LoggerFactory.getLogger(RefreshProtectedCredentialsProvider.class);
   private final AtomicReference<T> token = new AtomicReference<>();
   private final Lock refreshLock = new ReentrantLock();
   private final AtomicReference<CountDownLatch> latch = new AtomicReference<>();
   private final AtomicBoolean refreshInProcess = new AtomicBoolean(false);

   @Override
   public String getUsername() {
      if (this.token.get() == null) {
         this.refresh();
      }

      return this.usernameFromToken(this.token.get());
   }

   @Override
   public String getPassword() {
      if (this.token.get() == null) {
         this.refresh();
      }

      return this.passwordFromToken(this.token.get());
   }

   @Override
   public Duration getTimeBeforeExpiration() {
      if (this.token.get() == null) {
         this.refresh();
      }

      return this.timeBeforeExpiration(this.token.get());
   }

   @Override
   public void refresh() {
      if (this.refreshLock.tryLock()) {
         LOGGER.debug("Refreshing token");

         try {
            this.latch.set(new CountDownLatch(1));
            this.refreshInProcess.set(true);
            this.token.set(this.retrieveToken());
            LOGGER.debug("Token refreshed");
         } finally {
            this.latch.get().countDown();
            this.refreshInProcess.set(false);
            this.refreshLock.unlock();
         }
      } else {
         try {
            LOGGER.debug("Waiting for token refresh to be finished");

            while (!this.refreshInProcess.get()) {
               Thread.sleep(10L);
            }

            this.latch.get().await();
            LOGGER.debug("Done waiting for token refresh");
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }

   protected abstract T retrieveToken();

   protected abstract String usernameFromToken(T var1);

   protected abstract String passwordFromToken(T var1);

   protected abstract Duration timeBeforeExpiration(T var1);
}
