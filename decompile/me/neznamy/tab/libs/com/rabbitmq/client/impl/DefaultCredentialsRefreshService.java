package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCredentialsRefreshService implements CredentialsRefreshService {
   private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCredentialsRefreshService.class);
   private final ScheduledExecutorService scheduler;
   private final ConcurrentMap<CredentialsProvider, DefaultCredentialsRefreshService.CredentialsProviderState> credentialsProviderStates = new ConcurrentHashMap<>();
   private final boolean privateScheduler;
   private final Function<Duration, Duration> refreshDelayStrategy;
   private final Function<Duration, Boolean> approachingExpirationStrategy;

   public DefaultCredentialsRefreshService(
      ScheduledExecutorService scheduler, Function<Duration, Duration> refreshDelayStrategy, Function<Duration, Boolean> approachingExpirationStrategy
   ) {
      if (refreshDelayStrategy == null) {
         throw new IllegalArgumentException("Refresh delay strategy can not be null");
      }

      this.refreshDelayStrategy = refreshDelayStrategy;
      this.approachingExpirationStrategy = approachingExpirationStrategy == null ? duration -> false : approachingExpirationStrategy;
      if (scheduler == null) {
         this.scheduler = Executors.newScheduledThreadPool(1);
         this.privateScheduler = true;
      } else {
         this.scheduler = scheduler;
         this.privateScheduler = false;
      }
   }

   public static Function<Duration, Duration> ratioRefreshDelayStrategy(double ratio) {
      return new DefaultCredentialsRefreshService.RatioRefreshDelayStrategy(ratio);
   }

   public static Function<Duration, Duration> fixedDelayBeforeExpirationRefreshDelayStrategy(Duration duration) {
      return new DefaultCredentialsRefreshService.FixedDelayBeforeExpirationRefreshDelayStrategy(duration);
   }

   public static Function<Duration, Boolean> fixedTimeApproachingExpirationStrategy(Duration limitBeforeExpiration) {
      return new DefaultCredentialsRefreshService.FixedTimeApproachingExpirationStrategy(limitBeforeExpiration.toMillis());
   }

   private static Runnable refresh(
      ScheduledExecutorService scheduler,
      DefaultCredentialsRefreshService.CredentialsProviderState credentialsProviderState,
      Function<Duration, Duration> refreshDelayStrategy
   ) {
      return () -> {
         LOGGER.debug("Refreshing token");
         credentialsProviderState.refresh();
         Duration timeBeforeExpiration = credentialsProviderState.credentialsProvider.getTimeBeforeExpiration();
         Duration newDelay = refreshDelayStrategy.apply(timeBeforeExpiration);
         LOGGER.debug("Scheduling refresh in {} seconds", newDelay.getSeconds());
         ScheduledFuture<?> scheduledFuture = scheduler.schedule(
            refresh(scheduler, credentialsProviderState, refreshDelayStrategy), newDelay.getSeconds(), TimeUnit.SECONDS
         );
         credentialsProviderState.refreshTask.set(scheduledFuture);
      };
   }

   @Override
   public String register(CredentialsProvider credentialsProvider, Callable<Boolean> refreshAction) {
      String registrationId = UUID.randomUUID().toString();
      LOGGER.debug("New registration {}", registrationId);
      DefaultCredentialsRefreshService.Registration registration = new DefaultCredentialsRefreshService.Registration(registrationId, refreshAction);
      DefaultCredentialsRefreshService.CredentialsProviderState credentialsProviderState = this.credentialsProviderStates
         .computeIfAbsent(credentialsProvider, credentialsProviderKey -> new DefaultCredentialsRefreshService.CredentialsProviderState(credentialsProviderKey));
      credentialsProviderState.add(registration);
      credentialsProviderState.maybeSetRefreshTask(() -> {
         Duration delay = this.refreshDelayStrategy.apply(credentialsProvider.getTimeBeforeExpiration());
         LOGGER.debug("Scheduling refresh in {} seconds", delay.getSeconds());
         return this.scheduler.schedule(refresh(this.scheduler, credentialsProviderState, this.refreshDelayStrategy), delay.getSeconds(), TimeUnit.SECONDS);
      });
      return registrationId;
   }

   @Override
   public void unregister(CredentialsProvider credentialsProvider, String registrationId) {
      DefaultCredentialsRefreshService.CredentialsProviderState credentialsProviderState = this.credentialsProviderStates.get(credentialsProvider);
      if (credentialsProviderState != null) {
         credentialsProviderState.unregister(registrationId);
      }
   }

   @Override
   public boolean isApproachingExpiration(Duration timeBeforeExpiration) {
      return this.approachingExpirationStrategy.apply(timeBeforeExpiration);
   }

   public void close() {
      if (this.privateScheduler) {
         this.scheduler.shutdownNow();
      }
   }

   static class CredentialsProviderState {
      private final CredentialsProvider credentialsProvider;
      private final Map<String, DefaultCredentialsRefreshService.Registration> registrations = new ConcurrentHashMap<>();
      private final AtomicReference<ScheduledFuture<?>> refreshTask = new AtomicReference<>();
      private final AtomicBoolean refreshTaskSet = new AtomicBoolean(false);

      CredentialsProviderState(CredentialsProvider credentialsProvider) {
         this.credentialsProvider = credentialsProvider;
      }

      void add(DefaultCredentialsRefreshService.Registration registration) {
         this.registrations.put(registration.id, registration);
      }

      void maybeSetRefreshTask(Supplier<ScheduledFuture<?>> scheduledFutureSupplier) {
         if (this.refreshTaskSet.compareAndSet(false, true)) {
            this.refreshTask.set(scheduledFutureSupplier.get());
         }
      }

      void refresh() {
         if (!Thread.currentThread().isInterrupted()) {
            int attemptCount = 0;
            boolean refreshSucceeded = false;

            while (attemptCount < 3) {
               DefaultCredentialsRefreshService.LOGGER.debug("Refreshing token for credentials provider {}", this.credentialsProvider);

               try {
                  this.credentialsProvider.refresh();
                  DefaultCredentialsRefreshService.LOGGER.debug("Token refreshed for credentials provider {}", this.credentialsProvider);
                  refreshSucceeded = true;
                  break;
               } catch (Exception e) {
                  DefaultCredentialsRefreshService.LOGGER.warn("Error while trying to refresh token: {}", e.getMessage());
                  attemptCount++;

                  try {
                     Thread.sleep(1000L);
                  } catch (InterruptedException ex) {
                     Thread.currentThread().interrupt();
                     return;
                  }
               }
            }

            if (!refreshSucceeded) {
               DefaultCredentialsRefreshService.LOGGER.warn("Token refresh failed after retry, aborting callbacks");
            } else {
               Iterator<DefaultCredentialsRefreshService.Registration> iterator = this.registrations.values().iterator();

               while (iterator.hasNext() && !Thread.currentThread().isInterrupted()) {
                  DefaultCredentialsRefreshService.Registration registration = iterator.next();

                  try {
                     boolean refreshed = registration.refreshAction.call();
                     if (!refreshed) {
                        DefaultCredentialsRefreshService.LOGGER.debug("Registration did not refresh token");
                        iterator.remove();
                     }

                     registration.errorHistory.set(0);
                  } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                  } catch (Exception e) {
                     DefaultCredentialsRefreshService.LOGGER.warn("Error while trying to refresh a connection token", e);
                     registration.errorHistory.incrementAndGet();
                     if (registration.errorHistory.get() >= 5) {
                        this.registrations.remove(registration.id);
                     }
                  }
               }
            }
         }
      }

      void unregister(String registrationId) {
         this.registrations.remove(registrationId);
      }
   }

   public static class DefaultCredentialsRefreshServiceBuilder {
      private ScheduledExecutorService scheduler;
      private Function<Duration, Duration> refreshDelayStrategy = DefaultCredentialsRefreshService.ratioRefreshDelayStrategy(0.8);
      private Function<Duration, Boolean> approachingExpirationStrategy = ttl -> false;

      public DefaultCredentialsRefreshService.DefaultCredentialsRefreshServiceBuilder scheduler(ScheduledThreadPoolExecutor scheduler) {
         this.scheduler = scheduler;
         return this;
      }

      public DefaultCredentialsRefreshService.DefaultCredentialsRefreshServiceBuilder refreshDelayStrategy(Function<Duration, Duration> refreshDelayStrategy) {
         this.refreshDelayStrategy = refreshDelayStrategy;
         return this;
      }

      public DefaultCredentialsRefreshService.DefaultCredentialsRefreshServiceBuilder approachingExpirationStrategy(
         Function<Duration, Boolean> approachingExpirationStrategy
      ) {
         this.approachingExpirationStrategy = approachingExpirationStrategy;
         return this;
      }

      public DefaultCredentialsRefreshService build() {
         return new DefaultCredentialsRefreshService(this.scheduler, this.refreshDelayStrategy, this.approachingExpirationStrategy);
      }
   }

   private static class FixedDelayBeforeExpirationRefreshDelayStrategy implements Function<Duration, Duration> {
      private final Duration delay;

      private FixedDelayBeforeExpirationRefreshDelayStrategy(Duration delay) {
         this.delay = delay;
      }

      public Duration apply(Duration timeBeforeExpiration) {
         Duration refreshTimeBeforeExpiration = timeBeforeExpiration.minus(this.delay);
         return refreshTimeBeforeExpiration.isNegative() ? timeBeforeExpiration : refreshTimeBeforeExpiration;
      }
   }

   private static class FixedTimeApproachingExpirationStrategy implements Function<Duration, Boolean> {
      private final long limitBeforeExpiration;

      private FixedTimeApproachingExpirationStrategy(long limitBeforeExpiration) {
         this.limitBeforeExpiration = limitBeforeExpiration;
      }

      public Boolean apply(Duration timeBeforeExpiration) {
         return timeBeforeExpiration.toMillis() <= this.limitBeforeExpiration;
      }
   }

   private static class RatioRefreshDelayStrategy implements Function<Duration, Duration> {
      private final double ratio;

      private RatioRefreshDelayStrategy(double ratio) {
         if (!(ratio < 0.0) && !(ratio > 1.0)) {
            this.ratio = ratio;
         } else {
            throw new IllegalArgumentException("Ratio should be > 0 and <= 1: " + ratio);
         }
      }

      public Duration apply(Duration duration) {
         return Duration.ofSeconds((long)(duration.getSeconds() * this.ratio));
      }
   }

   static class Registration {
      private final Callable<Boolean> refreshAction;
      private final AtomicInteger errorHistory = new AtomicInteger(0);
      private final String id;

      Registration(String id, Callable<Boolean> refreshAction) {
         this.refreshAction = refreshAction;
         this.id = id;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultCredentialsRefreshService.Registration that = (DefaultCredentialsRefreshService.Registration)o;
            return this.id.equals(that.id);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.id.hashCode();
      }
   }
}
