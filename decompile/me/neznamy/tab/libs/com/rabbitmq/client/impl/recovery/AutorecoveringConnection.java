package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.Address;
import me.neznamy.tab.libs.com.rabbitmq.client.AddressResolver;
import me.neznamy.tab.libs.com.rabbitmq.client.BlockedCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.BlockedListener;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.ExceptionHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.ListAddressResolver;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.MissedHeartbeatException;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.RecoverableConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.RecoveryListener;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownListener;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.TopologyRecoveryException;
import me.neznamy.tab.libs.com.rabbitmq.client.UnblockedCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ConnectionParams;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandlerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.NetworkConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;
import me.neznamy.tab.libs.com.rabbitmq.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutorecoveringConnection implements RecoverableConnection, NetworkConnection {
   public static final Predicate<ShutdownSignalException> DEFAULT_CONNECTION_RECOVERY_TRIGGERING_CONDITION = cause -> !cause.isInitiatedByApplication()
      || cause.getCause() instanceof MissedHeartbeatException;
   private static final Logger LOGGER = LoggerFactory.getLogger(AutorecoveringConnection.class);
   private final RecoveryAwareAMQConnectionFactory cf;
   private final Map<Integer, AutorecoveringChannel> channels;
   private final ConnectionParams params;
   private volatile RecoveryAwareAMQConnection delegate;
   private final List<ShutdownListener> shutdownHooks = Collections.synchronizedList(new ArrayList<>());
   private final List<RecoveryListener> recoveryListeners = Collections.synchronizedList(new ArrayList<>());
   private final List<BlockedListener> blockedListeners = Collections.synchronizedList(new ArrayList<>());
   private final Map<String, RecordedQueue> recordedQueues = Collections.synchronizedMap(new LinkedHashMap<>());
   private final List<RecordedBinding> recordedBindings = Collections.synchronizedList(new ArrayList<>());
   private final Map<String, RecordedExchange> recordedExchanges = Collections.synchronizedMap(new LinkedHashMap<>());
   private final Map<String, RecordedConsumer> consumers = Collections.synchronizedMap(new LinkedHashMap<>());
   private final List<ConsumerRecoveryListener> consumerRecoveryListeners = Collections.synchronizedList(new ArrayList<>());
   private final List<QueueRecoveryListener> queueRecoveryListeners = Collections.synchronizedList(new ArrayList<>());
   private final TopologyRecoveryFilter topologyRecoveryFilter;
   private volatile boolean manuallyClosed = false;
   private final Object recoveryLock = new Object();
   private final Predicate<ShutdownSignalException> connectionRecoveryTriggeringCondition;
   private final RetryHandler retryHandler;
   private final RecoveredQueueNameSupplier recoveredQueueNameSupplier;

   public AutorecoveringConnection(ConnectionParams params, FrameHandlerFactory f, List<Address> addrs) {
      this(params, f, new ListAddressResolver(addrs));
   }

   public AutorecoveringConnection(ConnectionParams params, FrameHandlerFactory f, AddressResolver addressResolver) {
      this(params, f, addressResolver, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public AutorecoveringConnection(
      ConnectionParams params,
      FrameHandlerFactory f,
      AddressResolver addressResolver,
      MetricsCollector metricsCollector,
      ObservationCollector observationCollector
   ) {
      this.cf = new RecoveryAwareAMQConnectionFactory(params, f, addressResolver, metricsCollector, observationCollector);
      this.params = params;
      this.connectionRecoveryTriggeringCondition = params.getConnectionRecoveryTriggeringCondition() == null
         ? DEFAULT_CONNECTION_RECOVERY_TRIGGERING_CONDITION
         : params.getConnectionRecoveryTriggeringCondition();
      this.setupErrorOnWriteListenerForPotentialRecovery();
      this.channels = new ConcurrentHashMap<>();
      this.topologyRecoveryFilter = params.getTopologyRecoveryFilter() == null ? letAllPassFilter() : params.getTopologyRecoveryFilter();
      this.retryHandler = params.getTopologyRecoveryRetryHandler();
      this.recoveredQueueNameSupplier = params.getRecoveredQueueNameSupplier() == null
         ? RecordedQueue.DEFAULT_QUEUE_NAME_SUPPLIER
         : params.getRecoveredQueueNameSupplier();
   }

   private void setupErrorOnWriteListenerForPotentialRecovery() {
      ThreadFactory threadFactory = this.params.getThreadFactory();
      Lock errorOnWriteLock = new ReentrantLock();
      this.params.setErrorOnWriteListener((connection, exception) -> {
         if (errorOnWriteLock.tryLock()) {
            try {
               Thread recoveryThread = threadFactory.newThread(() -> {
                  AMQConnection c = (AMQConnection)connection;
                  c.handleIoError(exception);
               });
               recoveryThread.setName("RabbitMQ Error On Write Thread");
               recoveryThread.start();
            } finally {
               errorOnWriteLock.unlock();
            }
         }

         throw exception;
      });
   }

   private static TopologyRecoveryFilter letAllPassFilter() {
      return new TopologyRecoveryFilter() {};
   }

   public void init() throws IOException, TimeoutException {
      this.delegate = this.cf.newConnection();
      this.addAutomaticRecoveryListener(this.delegate);
   }

   @Override
   public Channel createChannel() throws IOException {
      RecoveryAwareChannelN ch = (RecoveryAwareChannelN)this.delegate.createChannel();
      return ch == null ? null : this.wrapChannel(ch);
   }

   @Override
   public Channel createChannel(int channelNumber) throws IOException {
      RecoveryAwareChannelN ch = (RecoveryAwareChannelN)this.delegate.createChannel(channelNumber);
      return ch == null ? null : this.wrapChannel(ch);
   }

   private Channel wrapChannel(RecoveryAwareChannelN delegateChannel) {
      if (delegateChannel == null) {
         return null;
      }

      AutorecoveringChannel channel = new AutorecoveringChannel(this, delegateChannel);
      this.registerChannel(channel);
      return channel;
   }

   void registerChannel(AutorecoveringChannel channel) {
      this.channels.put(channel.getChannelNumber(), channel);
   }

   void unregisterChannel(AutorecoveringChannel channel) {
      this.channels.remove(channel.getChannelNumber());
   }

   @Override
   public Map<String, Object> getServerProperties() {
      return this.delegate.getServerProperties();
   }

   @Override
   public Map<String, Object> getClientProperties() {
      return this.delegate.getClientProperties();
   }

   @Override
   public String getClientProvidedName() {
      return this.delegate.getClientProvidedName();
   }

   @Override
   public int getFrameMax() {
      return this.delegate.getFrameMax();
   }

   @Override
   public int getHeartbeat() {
      return this.delegate.getHeartbeat();
   }

   @Override
   public int getChannelMax() {
      return this.delegate.getChannelMax();
   }

   @Override
   public boolean isOpen() {
      return this.delegate.isOpen();
   }

   @Override
   public void close() throws IOException {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.close();
   }

   @Override
   public void close(int timeout) throws IOException {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.close(timeout);
   }

   @Override
   public void close(int closeCode, String closeMessage, int timeout) throws IOException {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.close(closeCode, closeMessage, timeout);
   }

   @Override
   public void abort() {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.abort();
   }

   @Override
   public void abort(int closeCode, String closeMessage, int timeout) {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.abort(closeCode, closeMessage, timeout);
   }

   @Override
   public void abort(int closeCode, String closeMessage) {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.abort(closeCode, closeMessage);
   }

   @Override
   public void abort(int timeout) {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.abort(timeout);
   }

   public AMQConnection getDelegate() {
      return this.delegate;
   }

   @Override
   public ShutdownSignalException getCloseReason() {
      return this.delegate.getCloseReason();
   }

   @Override
   public void addBlockedListener(BlockedListener listener) {
      this.blockedListeners.add(listener);
      this.delegate.addBlockedListener(listener);
   }

   @Override
   public BlockedListener addBlockedListener(final BlockedCallback blockedCallback, final UnblockedCallback unblockedCallback) {
      BlockedListener blockedListener = new BlockedListener() {
         @Override
         public void handleBlocked(String reason) throws IOException {
            blockedCallback.handle(reason);
         }

         @Override
         public void handleUnblocked() throws IOException {
            unblockedCallback.handle();
         }
      };
      this.addBlockedListener(blockedListener);
      return blockedListener;
   }

   @Override
   public boolean removeBlockedListener(BlockedListener listener) {
      this.blockedListeners.remove(listener);
      return this.delegate.removeBlockedListener(listener);
   }

   @Override
   public void clearBlockedListeners() {
      this.blockedListeners.clear();
      this.delegate.clearBlockedListeners();
   }

   @Override
   public void close(int closeCode, String closeMessage) throws IOException {
      synchronized (this.recoveryLock) {
         this.manuallyClosed = true;
      }

      this.delegate.close(closeCode, closeMessage);
   }

   @Override
   public void addShutdownListener(ShutdownListener listener) {
      this.shutdownHooks.add(listener);
      this.delegate.addShutdownListener(listener);
   }

   @Override
   public void removeShutdownListener(ShutdownListener listener) {
      this.shutdownHooks.remove(listener);
      this.delegate.removeShutdownListener(listener);
   }

   @Override
   public void notifyListeners() {
      this.delegate.notifyListeners();
   }

   @Override
   public void addRecoveryListener(RecoveryListener listener) {
      this.recoveryListeners.add(listener);
   }

   @Override
   public void removeRecoveryListener(RecoveryListener listener) {
      this.recoveryListeners.remove(listener);
   }

   @Override
   public ExceptionHandler getExceptionHandler() {
      return this.delegate.getExceptionHandler();
   }

   @Override
   public int getPort() {
      return this.delegate.getPort();
   }

   @Override
   public InetAddress getAddress() {
      return this.delegate.getAddress();
   }

   @Override
   public InetAddress getLocalAddress() {
      return this.delegate.getLocalAddress();
   }

   @Override
   public int getLocalPort() {
      return this.delegate.getLocalPort();
   }

   private void addAutomaticRecoveryListener(RecoveryAwareAMQConnection newConn) {
      AutorecoveringConnection c = this;
      RecoveryCanBeginListener starter = cause -> {
         try {
            if (this.shouldTriggerConnectionRecovery(cause)) {
               c.beginAutomaticRecovery();
            }
         } catch (Exception e) {
            newConn.getExceptionHandler().handleConnectionRecoveryException(c, e);
         }
      };
      synchronized (this) {
         newConn.addRecoveryCanBeginListener(starter);
      }
   }

   protected boolean shouldTriggerConnectionRecovery(ShutdownSignalException cause) {
      return this.connectionRecoveryTriggeringCondition.test(cause);
   }

   public void addQueueRecoveryListener(QueueRecoveryListener listener) {
      this.queueRecoveryListeners.add(listener);
   }

   public void removeQueueRecoveryListener(QueueRecoveryListener listener) {
      this.queueRecoveryListeners.remove(listener);
   }

   public void addConsumerRecoveryListener(ConsumerRecoveryListener listener) {
      this.consumerRecoveryListeners.add(listener);
   }

   public void removeConsumerRecoveryListener(ConsumerRecoveryListener listener) {
      this.consumerRecoveryListeners.remove(listener);
   }

   RecoveredQueueNameSupplier getRecoveredQueueNameSupplier() {
      return this.recoveredQueueNameSupplier;
   }

   private synchronized void beginAutomaticRecovery() throws InterruptedException {
      long delay = this.params.getRecoveryDelayHandler().getDelay(0);
      if (delay > 0L) {
         this.wait(delay);
      }

      this.notifyRecoveryListenersStarted();
      RecoveryAwareAMQConnection newConn = this.recoverConnection();
      if (newConn != null) {
         LOGGER.debug("Connection {} has recovered", newConn);
         this.addAutomaticRecoveryListener(newConn);
         this.recoverShutdownListeners(newConn);
         this.recoverBlockedListeners(newConn);
         this.recoverChannels(newConn);
         this.delegate = newConn;
         if (this.params.isTopologyRecoveryEnabled()) {
            this.notifyTopologyRecoveryListenersStarted();
            this.recoverTopology(this.params.getTopologyRecoveryExecutor());
         }

         this.notifyRecoveryListenersComplete();
      }
   }

   private void recoverShutdownListeners(RecoveryAwareAMQConnection newConn) {
      for (ShutdownListener sh : Utility.copy(this.shutdownHooks)) {
         newConn.addShutdownListener(sh);
      }
   }

   private void recoverBlockedListeners(RecoveryAwareAMQConnection newConn) {
      for (BlockedListener bl : Utility.copy(this.blockedListeners)) {
         newConn.addBlockedListener(bl);
      }
   }

   private RecoveryAwareAMQConnection recoverConnection() throws InterruptedException {
      int attempts = 0;

      while (!this.manuallyClosed) {
         try {
            attempts++;
            RecoveryAwareAMQConnection newConn = this.cf.newConnection();
            synchronized (this.recoveryLock) {
               if (!this.manuallyClosed) {
                  return newConn;
               }
            }

            newConn.abort();
            return null;
         } catch (Exception e) {
            Thread.sleep(this.params.getRecoveryDelayHandler().getDelay(attempts));
            this.getExceptionHandler().handleConnectionRecoveryException(this, e);
         }
      }

      return null;
   }

   private void recoverChannels(RecoveryAwareAMQConnection newConn) {
      for (AutorecoveringChannel ch : this.channels.values()) {
         try {
            ch.automaticallyRecover(this, newConn);
            LOGGER.debug("Channel {} has recovered", ch);
         } catch (Throwable t) {
            newConn.getExceptionHandler().handleChannelRecoveryException(ch, t);
         }
      }
   }

   public void recoverChannel(AutorecoveringChannel channel) throws IOException {
      channel.automaticallyRecover(this, this.delegate);
   }

   private void notifyRecoveryListenersComplete() {
      for (RecoveryListener f : Utility.copy(this.recoveryListeners)) {
         f.handleRecovery(this);
      }
   }

   private void notifyRecoveryListenersStarted() {
      for (RecoveryListener f : Utility.copy(this.recoveryListeners)) {
         f.handleRecoveryStarted(this);
      }
   }

   private void notifyTopologyRecoveryListenersStarted() {
      for (RecoveryListener f : Utility.copy(this.recoveryListeners)) {
         f.handleTopologyRecoveryStarted(this);
      }
   }

   public void recoverChannelAndTopology(AutorecoveringChannel channel) {
      if (!this.channels.containsValue(channel)) {
         throw new IllegalArgumentException("This channel is not owned by this connection");
      }

      try {
         LOGGER.debug("Recovering channel={}", channel);
         this.recoverChannel(channel);
         LOGGER.debug("Recovered channel={}. Now recovering its topology", channel);
         Utility.copy(this.recordedExchanges).values().stream().filter(e -> e.getChannel() == channel).forEach(e -> this.recoverExchange(e, false));
         Utility.copy(this.recordedQueues).values().stream().filter(q -> q.getChannel() == channel).forEach(q -> this.recoverQueue(q.getName(), q, false));
         Utility.copy(this.recordedBindings).stream().filter(b -> b.getChannel() == channel).forEach(b -> this.recoverBinding(b, false));
         Utility.copy(this.consumers).values().stream().filter(c -> c.getChannel() == channel).forEach(c -> this.recoverConsumer(c.getConsumerTag(), c, false));
         LOGGER.debug("Recovered topology for channel={}", channel);
      } catch (Exception e) {
         this.getExceptionHandler().handleChannelRecoveryException(channel, e);
      }
   }

   private void recoverTopology(ExecutorService executor) {
      if (executor == null) {
         for (RecordedExchange exchange : Utility.copy(this.recordedExchanges).values()) {
            this.recoverExchange(exchange, true);
         }

         for (Entry<String, RecordedQueue> entry : Utility.copy(this.recordedQueues).entrySet()) {
            this.recoverQueue(entry.getKey(), entry.getValue(), true);
         }

         for (RecordedBinding b : Utility.copy(this.recordedBindings)) {
            this.recoverBinding(b, true);
         }

         for (Entry<String, RecordedConsumer> entry : Utility.copy(this.consumers).entrySet()) {
            this.recoverConsumer(entry.getKey(), entry.getValue(), true);
         }
      } else {
         try {
            this.recoverEntitiesAsynchronously(executor, Utility.copy(this.recordedExchanges).values());
            this.recoverEntitiesAsynchronously(executor, Utility.copy(this.recordedQueues).values());
            this.recoverEntitiesAsynchronously(executor, Utility.copy(this.recordedBindings));
            this.recoverEntitiesAsynchronously(executor, Utility.copy(this.consumers).values());
         } catch (Exception cause) {
            String message = "Caught an exception while recovering topology: " + cause.getMessage();
            TopologyRecoveryException e = new TopologyRecoveryException(message, cause);
            this.getExceptionHandler().handleTopologyRecoveryException(this.delegate, null, e);
         }
      }
   }

   public void recoverExchange(RecordedExchange x, boolean retry) {
      try {
         if (this.topologyRecoveryFilter.filterExchange(x)) {
            if (retry) {
               RecordedExchange entity = x;
               x = (RecordedExchange)this.wrapRetryIfNecessary(x, () -> {
                  entity.recover();
                  return null;
               }).getRecordedEntity();
            } else {
               x.recover();
            }

            LOGGER.debug("{} has recovered", x);
         }
      } catch (Exception cause) {
         String message = "Caught an exception while recovering exchange " + x.getName() + ": " + cause.getMessage();
         TopologyRecoveryException e = new TopologyRecoveryException(message, cause, x);
         this.getExceptionHandler().handleTopologyRecoveryException(this.delegate, x.getDelegateChannel(), e);
      }
   }

   public void recoverQueue(String oldName, RecordedQueue q, boolean retry) {
      try {
         this.internalRecoverQueue(oldName, q, retry);
      } catch (Exception cause) {
         String message = "Caught an exception while recovering queue " + oldName + ": " + cause.getMessage();
         TopologyRecoveryException e = new TopologyRecoveryException(message, cause, q);
         this.getExceptionHandler().handleTopologyRecoveryException(this.delegate, q.getDelegateChannel(), e);
      }
   }

   void recoverQueue(String oldName, RecordedQueue q) throws Exception {
      this.internalRecoverQueue(oldName, q, false);
   }

   private void internalRecoverQueue(String oldName, RecordedQueue q, boolean retry) throws Exception {
      if (this.topologyRecoveryFilter.filterQueue(q)) {
         LOGGER.debug("Recovering {}", q);
         if (retry) {
            RecordedQueue entity = q;
            q = (RecordedQueue)this.wrapRetryIfNecessary(q, () -> {
               entity.recover();
               return null;
            }).getRecordedEntity();
         } else {
            q.recover();
         }

         String newName = q.getName();
         if (!oldName.equals(newName)) {
            this.propagateQueueNameChangeToBindings(oldName, newName);
            this.propagateQueueNameChangeToConsumers(oldName, newName);
            synchronized (this.recordedQueues) {
               this.deleteRecordedQueue(oldName);
               this.recordedQueues.put(newName, q);
            }
         }

         for (QueueRecoveryListener qrl : Utility.copy(this.queueRecoveryListeners)) {
            qrl.queueRecovered(oldName, newName);
         }

         LOGGER.debug("{} has recovered", q);
      }
   }

   public void recoverBinding(RecordedBinding b, boolean retry) {
      try {
         if (this.topologyRecoveryFilter.filterBinding(b)) {
            if (retry) {
               RecordedBinding entity = b;
               b = (RecordedBinding)this.wrapRetryIfNecessary(b, () -> {
                  entity.recover();
                  return null;
               }).getRecordedEntity();
            } else {
               b.recover();
            }

            LOGGER.debug("{} has recovered", b);
         }
      } catch (Exception cause) {
         String message = "Caught an exception while recovering binding between " + b.getSource() + " and " + b.getDestination() + ": " + cause.getMessage();
         TopologyRecoveryException e = new TopologyRecoveryException(message, cause, b);
         this.getExceptionHandler().handleTopologyRecoveryException(this.delegate, b.getDelegateChannel(), e);
      }
   }

   public void recoverConsumer(String tag, RecordedConsumer consumer, boolean retry) {
      try {
         this.internalRecoverConsumer(tag, consumer, retry);
      } catch (Exception cause) {
         String message = "Caught an exception while recovering consumer " + tag + ": " + cause.getMessage();
         TopologyRecoveryException e = new TopologyRecoveryException(message, cause, consumer);
         this.getExceptionHandler().handleTopologyRecoveryException(this.delegate, consumer.getDelegateChannel(), e);
      }
   }

   void recoverConsumer(String tag, RecordedConsumer consumer) throws Exception {
      this.internalRecoverConsumer(tag, consumer, false);
   }

   private void internalRecoverConsumer(String tag, RecordedConsumer consumer, boolean retry) throws Exception {
      if (this.topologyRecoveryFilter.filterConsumer(consumer)) {
         LOGGER.debug("Recovering {}", consumer);
         String newTag = null;
         if (retry) {
            RecordedConsumer entity = consumer;
            RetryResult retryResult = this.wrapRetryIfNecessary(consumer, entity::recover);
            consumer = (RecordedConsumer)retryResult.getRecordedEntity();
            newTag = (String)retryResult.getResult();
         } else {
            newTag = consumer.recover();
         }

         if (tag != null && !tag.equals(newTag)) {
            synchronized (this.consumers) {
               this.consumers.remove(tag);
               this.consumers.put(newTag, consumer);
            }

            consumer.getChannel().updateConsumerTag(tag, newTag);
         }

         for (ConsumerRecoveryListener crl : Utility.copy(this.consumerRecoveryListeners)) {
            crl.consumerRecovered(tag, newTag);
         }

         LOGGER.debug("{} has recovered", consumer);
      }
   }

   private <T> RetryResult wrapRetryIfNecessary(RecordedEntity entity, Callable<T> recoveryAction) throws Exception {
      if (this.retryHandler == null) {
         T result = recoveryAction.call();
         return new RetryResult(entity, result);
      }

      try {
         T result = recoveryAction.call();
         return new RetryResult(entity, result);
      } catch (Exception e) {
         RetryContext retryContext = new RetryContext(entity, e, this);
         RetryResult retryResult;
         if (entity instanceof RecordedQueue) {
            retryResult = this.retryHandler.retryQueueRecovery(retryContext);
         } else if (entity instanceof RecordedExchange) {
            retryResult = this.retryHandler.retryExchangeRecovery(retryContext);
         } else if (entity instanceof RecordedBinding) {
            retryResult = this.retryHandler.retryBindingRecovery(retryContext);
         } else {
            if (!(entity instanceof RecordedConsumer)) {
               throw new IllegalArgumentException("Unknown type of recorded entity: " + entity);
            }

            retryResult = this.retryHandler.retryConsumerRecovery(retryContext);
         }

         return retryResult;
      }
   }

   private void propagateQueueNameChangeToBindings(String oldName, String newName) {
      for (RecordedBinding b : Utility.copy(this.recordedBindings)) {
         if (b.getDestination().equals(oldName)) {
            b.setDestination(newName);
         }
      }
   }

   private void propagateQueueNameChangeToConsumers(String oldName, String newName) {
      for (RecordedConsumer c : Utility.copy(this.consumers).values()) {
         if (c.getQueue().equals(oldName)) {
            c.setQueue(newName);
         }
      }
   }

   private void recoverEntitiesAsynchronously(ExecutorService executor, Collection<? extends RecordedEntity> recordedEntities) throws InterruptedException {
      for (Future<Object> task : executor.invokeAll(this.groupEntitiesByChannel(recordedEntities))) {
         if (!task.isDone()) {
            LOGGER.warn("Recovery task should be done {}", task);
         } else {
            try {
               task.get(1L, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
               LOGGER.warn("Recovery task is done but returned an exception", e);
            }
         }
      }
   }

   private <E extends RecordedEntity> List<Callable<Object>> groupEntitiesByChannel(Collection<E> entities) {
      Map<AutorecoveringChannel, List<E>> map = new LinkedHashMap<>();

      for (E entity : entities) {
         AutorecoveringChannel channel = entity.getChannel();
         map.computeIfAbsent(channel, c -> new ArrayList<>()).add(entity);
      }

      List<Callable<Object>> callables = new ArrayList<>();

      for (List<E> entityList : map.values()) {
         callables.add(Executors.callable(() -> {
            for (E entityx : entityList) {
               if (entityx instanceof RecordedExchange) {
                  this.recoverExchange((RecordedExchange)entityx, true);
               } else if (entityx instanceof RecordedQueue) {
                  RecordedQueue q = (RecordedQueue)entityx;
                  this.recoverQueue(q.getName(), q, true);
               } else if (entityx instanceof RecordedBinding) {
                  this.recoverBinding((RecordedBinding)entityx, true);
               } else if (entityx instanceof RecordedConsumer) {
                  RecordedConsumer c = (RecordedConsumer)entityx;
                  this.recoverConsumer(c.getConsumerTag(), c, true);
               }
            }
         }));
      }

      return callables;
   }

   void recordQueueBinding(AutorecoveringChannel ch, String queue, String exchange, String routingKey, Map<String, Object> arguments) {
      RecordedBinding binding = new RecordedQueueBinding(ch).source(exchange).destination(queue).routingKey(routingKey).arguments(arguments);
      this.recordedBindings.remove(binding);
      this.recordedBindings.add(binding);
   }

   boolean deleteRecordedQueueBinding(AutorecoveringChannel ch, String queue, String exchange, String routingKey, Map<String, Object> arguments) {
      RecordedBinding b = new RecordedQueueBinding(ch).source(exchange).destination(queue).routingKey(routingKey).arguments(arguments);
      return this.recordedBindings.remove(b);
   }

   void recordExchangeBinding(AutorecoveringChannel ch, String destination, String source, String routingKey, Map<String, Object> arguments) {
      RecordedBinding binding = new RecordedExchangeBinding(ch).source(source).destination(destination).routingKey(routingKey).arguments(arguments);
      this.recordedBindings.remove(binding);
      this.recordedBindings.add(binding);
   }

   boolean deleteRecordedExchangeBinding(AutorecoveringChannel ch, String destination, String source, String routingKey, Map<String, Object> arguments) {
      RecordedBinding b = new RecordedExchangeBinding(ch).source(source).destination(destination).routingKey(routingKey).arguments(arguments);
      return this.recordedBindings.remove(b);
   }

   void recordQueue(AMQP.Queue.DeclareOk ok, RecordedQueue q) {
      this.recordedQueues.put(ok.getQueue(), q);
   }

   void recordQueue(String queue, RecordedQueue meta) {
      this.recordedQueues.put(queue, meta);
   }

   void deleteRecordedQueue(String queue) {
      this.recordedQueues.remove(queue);

      for (RecordedBinding b : this.removeBindingsWithDestination(queue)) {
         this.maybeDeleteRecordedAutoDeleteExchange(b.getSource());
      }
   }

   public void excludeQueueFromRecovery(String queue, boolean ifUnused) {
      if (ifUnused) {
         synchronized (this.consumers) {
            synchronized (this.recordedQueues) {
               if (!this.hasMoreConsumersOnQueue(this.consumers.values(), queue)) {
                  this.deleteRecordedQueue(queue);
               }
            }
         }
      } else {
         this.deleteRecordedQueue(queue);
      }
   }

   void recordExchange(String exchange, RecordedExchange x) {
      this.recordedExchanges.put(exchange, x);
   }

   void deleteRecordedExchange(String exchange) {
      this.recordedExchanges.remove(exchange);

      for (RecordedBinding b : this.removeBindingsWithDestination(exchange)) {
         this.maybeDeleteRecordedAutoDeleteExchange(b.getSource());
      }
   }

   void recordConsumer(String result, RecordedConsumer consumer) {
      this.consumers.put(result, consumer);
   }

   RecordedConsumer deleteRecordedConsumer(String consumerTag) {
      return this.consumers.remove(consumerTag);
   }

   void maybeDeleteRecordedAutoDeleteQueue(String queue) {
      synchronized (this.consumers) {
         synchronized (this.recordedQueues) {
            if (!this.hasMoreConsumersOnQueue(this.consumers.values(), queue)) {
               RecordedQueue q = this.recordedQueues.get(queue);
               if (q != null && q.isAutoDelete()) {
                  this.deleteRecordedQueue(queue);
               }
            }
         }
      }
   }

   void maybeDeleteRecordedAutoDeleteExchange(String exchange) {
      synchronized (this.recordedExchanges) {
         if (!this.hasMoreDestinationsBoundToExchange(Utility.copy(this.recordedBindings), exchange)) {
            RecordedExchange x = this.recordedExchanges.get(exchange);
            if (x != null && x.isAutoDelete()) {
               this.deleteRecordedExchange(exchange);
            }
         }
      }
   }

   boolean hasMoreDestinationsBoundToExchange(List<RecordedBinding> bindings, String exchange) {
      boolean result = false;

      for (RecordedBinding b : bindings) {
         if (exchange.equals(b.getSource())) {
            result = true;
            break;
         }
      }

      return result;
   }

   boolean hasMoreConsumersOnQueue(Collection<RecordedConsumer> consumers, String queue) {
      boolean result = false;

      for (RecordedConsumer c : consumers) {
         if (queue.equals(c.getQueue())) {
            result = true;
            break;
         }
      }

      return result;
   }

   Set<RecordedBinding> removeBindingsWithDestination(String s) {
      Set<RecordedBinding> result = new LinkedHashSet<>();
      synchronized (this.recordedBindings) {
         Iterator<RecordedBinding> it = this.recordedBindings.iterator();

         while (it.hasNext()) {
            RecordedBinding b = it.next();
            if (b.getDestination().equals(s)) {
               it.remove();
               result.add(b);
            }
         }

         return result;
      }
   }

   public Map<String, RecordedQueue> getRecordedQueues() {
      return this.recordedQueues;
   }

   public Map<String, RecordedExchange> getRecordedExchanges() {
      return this.recordedExchanges;
   }

   public List<RecordedBinding> getRecordedBindings() {
      return this.recordedBindings;
   }

   public Map<String, RecordedConsumer> getRecordedConsumers() {
      return this.consumers;
   }

   @Override
   public String toString() {
      return this.delegate.toString();
   }

   @Override
   public String getId() {
      return this.delegate.getId();
   }

   @Override
   public void setId(String id) {
      this.delegate.setId(id);
   }
}
