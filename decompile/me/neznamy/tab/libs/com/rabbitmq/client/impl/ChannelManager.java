package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;
import me.neznamy.tab.libs.com.rabbitmq.utility.IntAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);
   private final Object monitor = new Object();
   private final Map<Integer, ChannelN> _channelMap = new HashMap<>();
   private final IntAllocator channelNumberAllocator;
   private final ConsumerWorkService workService;
   private final Set<CountDownLatch> shutdownSet = new HashSet<>();
   private final int _channelMax;
   private ExecutorService shutdownExecutor;
   private final ThreadFactory threadFactory;
   private int channelShutdownTimeout = 63000;
   protected final MetricsCollector metricsCollector;
   protected final ObservationCollector observationCollector;

   public int getChannelMax() {
      return this._channelMax;
   }

   public ChannelManager(ConsumerWorkService workService, int channelMax) {
      this(workService, channelMax, Executors.defaultThreadFactory());
   }

   public ChannelManager(ConsumerWorkService workService, int channelMax, ThreadFactory threadFactory) {
      this(workService, channelMax, threadFactory, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public ChannelManager(
      ConsumerWorkService workService,
      int channelMax,
      ThreadFactory threadFactory,
      MetricsCollector metricsCollector,
      ObservationCollector observationCollector
   ) {
      if (channelMax < 0) {
         throw new IllegalArgumentException("create ChannelManager: 'channelMax' must be greater or equal to 0.");
      }

      if (channelMax == 0) {
         channelMax = 65535;
      }

      this._channelMax = channelMax;
      this.channelNumberAllocator = new IntAllocator(1, channelMax);
      this.workService = workService;
      this.threadFactory = threadFactory;
      this.metricsCollector = metricsCollector;
      this.observationCollector = observationCollector;
   }

   public ChannelN getChannel(int channelNumber) {
      synchronized (this.monitor) {
         ChannelN ch = this._channelMap.get(channelNumber);
         if (ch == null) {
            throw new UnknownChannelException(channelNumber);
         } else {
            return ch;
         }
      }
   }

   public void handleSignal(final ShutdownSignalException signal) {
      Set<ChannelN> channels;
      synchronized (this.monitor) {
         channels = new HashSet<>(this._channelMap.values());
      }

      for (final ChannelN channel : channels) {
         this.releaseChannelNumber(channel);
         Runnable channelShutdownRunnable = new Runnable() {
            @Override
            public void run() {
               channel.processShutdownSignal(signal, true, true);
            }
         };
         if (this.shutdownExecutor == null) {
            channelShutdownRunnable.run();
         } else {
            Future<?> channelShutdownTask = this.shutdownExecutor.submit(channelShutdownRunnable);

            try {
               channelShutdownTask.get(this.channelShutdownTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
               LOGGER.warn("Couldn't properly close channel {} on shutdown after waiting for {} ms", channel.getChannelNumber(), this.channelShutdownTimeout);
               channelShutdownTask.cancel(true);
            }
         }

         this.shutdownSet.add(channel.getShutdownLatch());
         channel.notifyListeners();
      }

      this.scheduleShutdownProcessing();
   }

   private void scheduleShutdownProcessing() {
      final Set<CountDownLatch> sdSet = new HashSet<>(this.shutdownSet);
      final ConsumerWorkService ssWorkService = this.workService;
      Runnable target = new Runnable() {
         @Override
         public void run() {
            for (CountDownLatch latch : sdSet) {
               try {
                  int shutdownTimeout = ssWorkService.getShutdownTimeout();
                  if (shutdownTimeout == 0) {
                     latch.await();
                  } else {
                     boolean completed = latch.await(shutdownTimeout, TimeUnit.MILLISECONDS);
                     if (!completed) {
                        ChannelManager.LOGGER.warn("Consumer dispatcher for channel didn't shutdown after waiting for {} ms", shutdownTimeout);
                     }
                  }
               } catch (Throwable var5) {
               }
            }

            ssWorkService.shutdown();
         }
      };
      if (this.shutdownExecutor != null) {
         this.shutdownExecutor.execute(target);
      } else {
         Thread shutdownThread = Environment.newThread(this.threadFactory, target, "ConsumerWorkService shutdown monitor", true);
         shutdownThread.start();
      }
   }

   public ChannelN createChannel(AMQConnection connection) throws IOException {
      ChannelN ch;
      synchronized (this.monitor) {
         int channelNumber = this.channelNumberAllocator.allocate();
         if (channelNumber == -1) {
            return null;
         }

         ch = this.addNewChannel(connection, channelNumber);
      }

      ch.open();
      return ch;
   }

   public ChannelN createChannel(AMQConnection connection, int channelNumber) throws IOException {
      ChannelN ch;
      synchronized (this.monitor) {
         if (!this.channelNumberAllocator.reserve(channelNumber)) {
            return null;
         }

         ch = this.addNewChannel(connection, channelNumber);
      }

      ch.open();
      return ch;
   }

   private ChannelN addNewChannel(AMQConnection connection, int channelNumber) {
      if (this._channelMap.containsKey(channelNumber)) {
         throw new IllegalStateException(
            "We have attempted to create a channel with a number that is already in use. This should never happen. Please report this as a bug."
         );
      }

      ChannelN ch = this.instantiateChannel(connection, channelNumber, this.workService);
      this._channelMap.put(ch.getChannelNumber(), ch);
      return ch;
   }

   protected ChannelN instantiateChannel(AMQConnection connection, int channelNumber, ConsumerWorkService workService) {
      return new ChannelN(connection, channelNumber, workService, this.metricsCollector, this.observationCollector);
   }

   public void releaseChannelNumber(ChannelN channel) {
      synchronized (this.monitor) {
         int channelNumber = channel.getChannelNumber();
         ChannelN existing = this._channelMap.remove(channelNumber);
         if (existing != null) {
            if (existing != channel) {
               this._channelMap.put(channelNumber, existing);
            } else {
               this.channelNumberAllocator.free(channelNumber);
            }
         }
      }
   }

   public ExecutorService getShutdownExecutor() {
      return this.shutdownExecutor;
   }

   public void setShutdownExecutor(ExecutorService shutdownExecutor) {
      this.shutdownExecutor = shutdownExecutor;
   }

   public void setChannelShutdownTimeout(int channelShutdownTimeout) {
      this.channelShutdownTimeout = channelShutdownTimeout;
   }
}
