package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.AlreadyClosedException;
import me.neznamy.tab.libs.com.rabbitmq.client.BuiltinExchangeType;
import me.neznamy.tab.libs.com.rabbitmq.client.CancelCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Command;
import me.neznamy.tab.libs.com.rabbitmq.client.ConfirmCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.ConfirmListener;
import me.neznamy.tab.libs.com.rabbitmq.client.ConnectionFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.Consumer;
import me.neznamy.tab.libs.com.rabbitmq.client.ConsumerShutdownSignalCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.DeliverCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.Delivery;
import me.neznamy.tab.libs.com.rabbitmq.client.Envelope;
import me.neznamy.tab.libs.com.rabbitmq.client.GetResponse;
import me.neznamy.tab.libs.com.rabbitmq.client.MessageProperties;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.Return;
import me.neznamy.tab.libs.com.rabbitmq.client.ReturnCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.ReturnListener;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.UnexpectedMethodError;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;
import me.neznamy.tab.libs.com.rabbitmq.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelN extends AMQChannel implements Channel {
   private static final int MAX_UNSIGNED_SHORT = 65535;
   private static final String UNSPECIFIED_OUT_OF_BAND = "";
   private static final Logger LOGGER = LoggerFactory.getLogger(ChannelN.class);
   private final Map<String, Consumer> _consumers = Collections.synchronizedMap(new HashMap<>());
   private final Collection<ReturnListener> returnListeners = new CopyOnWriteArrayList<>();
   private final Collection<ConfirmListener> confirmListeners = new CopyOnWriteArrayList<>();
   private long nextPublishSeqNo = 0L;
   private volatile Consumer defaultConsumer = null;
   private final ConsumerDispatcher dispatcher;
   private volatile CountDownLatch finishedShutdownFlag = null;
   private final SortedSet<Long> unconfirmedSet = Collections.synchronizedSortedSet(new TreeSet<>());
   private boolean confirmSelectActivated = false;
   private volatile boolean onlyAcksReceived = true;
   protected final MetricsCollector metricsCollector;
   private final ObservationCollector observationCollector;

   public ChannelN(AMQConnection connection, int channelNumber, ConsumerWorkService workService) {
      this(connection, channelNumber, workService, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public ChannelN(
      AMQConnection connection,
      int channelNumber,
      ConsumerWorkService workService,
      MetricsCollector metricsCollector,
      ObservationCollector observationCollector
   ) {
      super(connection, channelNumber);
      this.dispatcher = new ConsumerDispatcher(connection, this, workService);
      this.metricsCollector = metricsCollector;
      this.observationCollector = observationCollector;
   }

   public void open() throws IOException {
      this.exnWrappingRpc(new AMQImpl.Channel.Open(""));
   }

   @Override
   public void addReturnListener(ReturnListener listener) {
      this.returnListeners.add(listener);
   }

   @Override
   public ReturnListener addReturnListener(ReturnCallback returnCallback) {
      ReturnListener returnListener = (replyCode, replyText, exchange, routingKey, properties, body) -> returnCallback.handle(
         new Return(replyCode, replyText, exchange, routingKey, properties, body)
      );
      this.addReturnListener(returnListener);
      return returnListener;
   }

   @Override
   public boolean removeReturnListener(ReturnListener listener) {
      return this.returnListeners.remove(listener);
   }

   @Override
   public void clearReturnListeners() {
      this.returnListeners.clear();
   }

   @Override
   public void addConfirmListener(ConfirmListener listener) {
      this.confirmListeners.add(listener);
   }

   @Override
   public ConfirmListener addConfirmListener(final ConfirmCallback ackCallback, final ConfirmCallback nackCallback) {
      ConfirmListener confirmListener = new ConfirmListener() {
         @Override
         public void handleAck(long deliveryTag, boolean multiple) throws IOException {
            ackCallback.handle(deliveryTag, multiple);
         }

         @Override
         public void handleNack(long deliveryTag, boolean multiple) throws IOException {
            nackCallback.handle(deliveryTag, multiple);
         }
      };
      this.addConfirmListener(confirmListener);
      return confirmListener;
   }

   @Override
   public boolean removeConfirmListener(ConfirmListener listener) {
      return this.confirmListeners.remove(listener);
   }

   @Override
   public void clearConfirmListeners() {
      this.confirmListeners.clear();
   }

   @Override
   public boolean waitForConfirms() throws InterruptedException {
      boolean confirms = false;

      try {
         confirms = this.waitForConfirms(0L);
      } catch (TimeoutException var3) {
      }

      return confirms;
   }

   @Override
   public boolean waitForConfirms(long timeout) throws InterruptedException, TimeoutException {
      if (this.nextPublishSeqNo == 0L) {
         throw new IllegalStateException("Confirms not selected");
      }

      long startTime = System.currentTimeMillis();
      synchronized (this.unconfirmedSet) {
         while (this.getCloseReason() == null) {
            if (this.unconfirmedSet.isEmpty()) {
               boolean aux = this.onlyAcksReceived;
               this.onlyAcksReceived = true;
               return aux;
            }

            if (timeout == 0L) {
               this.unconfirmedSet.wait();
            } else {
               long elapsed = System.currentTimeMillis() - startTime;
               if (timeout <= elapsed) {
                  throw new TimeoutException();
               }

               this.unconfirmedSet.wait(timeout - elapsed);
            }
         }

         throw (ShutdownSignalException)Utility.fixStackTrace(this.getCloseReason());
      }
   }

   @Override
   public void waitForConfirmsOrDie() throws IOException, InterruptedException {
      try {
         this.waitForConfirmsOrDie(0L);
      } catch (TimeoutException var2) {
      }
   }

   @Override
   public void waitForConfirmsOrDie(long timeout) throws IOException, InterruptedException, TimeoutException {
      try {
         if (!this.waitForConfirms(timeout)) {
            this.close(200, "NACKS RECEIVED", true, null, false);
            throw new IOException("nacks received");
         }
      } catch (TimeoutException e) {
         this.close(406, "TIMEOUT WAITING FOR ACK");
         throw e;
      }
   }

   @Override
   public Consumer getDefaultConsumer() {
      return this.defaultConsumer;
   }

   @Override
   public void setDefaultConsumer(Consumer consumer) {
      this.defaultConsumer = consumer;
   }

   private void broadcastShutdownSignal(ShutdownSignalException signal) {
      this.finishedShutdownFlag = this.dispatcher.handleShutdownSignal(Utility.copy(this._consumers), signal);
   }

   private void startProcessShutdownSignal(ShutdownSignalException signal, boolean ignoreClosed, boolean notifyRpc) {
      super.processShutdownSignal(signal, ignoreClosed, notifyRpc);
   }

   private void finishProcessShutdownSignal() {
      this.dispatcher.quiesce();
      this.broadcastShutdownSignal(this.getCloseReason());
      synchronized (this.unconfirmedSet) {
         this.unconfirmedSet.notifyAll();
      }
   }

   @Override
   public void processShutdownSignal(ShutdownSignalException signal, boolean ignoreClosed, boolean notifyRpc) {
      this.startProcessShutdownSignal(signal, ignoreClosed, notifyRpc);
      this.finishProcessShutdownSignal();
   }

   CountDownLatch getShutdownLatch() {
      return this.finishedShutdownFlag;
   }

   private void releaseChannel() {
      this.getConnection().disconnectChannel(this);
   }

   @Override
   public boolean processAsync(Command command) throws IOException {
      me.neznamy.tab.libs.com.rabbitmq.client.Method method = command.getMethod();
      if (method instanceof AMQImpl.Channel.Close) {
         this.asyncShutdown(command);
         return true;
      }

      if (!this.isOpen()) {
         return !(method instanceof AMQImpl.Channel.CloseOk);
      }

      if (method instanceof AMQImpl.Basic.Deliver) {
         this.processDelivery(command, (AMQImpl.Basic.Deliver)method);
         return true;
      }

      if (method instanceof AMQImpl.Basic.Return) {
         this.callReturnListeners(command, (AMQImpl.Basic.Return)method);
         return true;
      }

      if (method instanceof AMQImpl.Channel.Flow) {
         AMQImpl.Channel.Flow channelFlow = (AMQImpl.Channel.Flow)method;
         this._channelLock.lock();

         try {
            this._blockContent = !channelFlow.getActive();
            this.transmit(new AMQImpl.Channel.FlowOk(!this._blockContent));
            this._channelLockCondition.signalAll();
         } finally {
            this._channelLock.unlock();
         }

         return true;
      } else {
         if (method instanceof AMQImpl.Basic.Ack) {
            AMQImpl.Basic.Ack ack = (AMQImpl.Basic.Ack)method;
            this.callConfirmListeners(command, ack);
            this.handleAckNack(ack.getDeliveryTag(), ack.getMultiple(), false);
            return true;
         }

         if (method instanceof AMQImpl.Basic.Nack) {
            AMQImpl.Basic.Nack nack = (AMQImpl.Basic.Nack)method;
            this.callConfirmListeners(command, nack);
            this.handleAckNack(nack.getDeliveryTag(), nack.getMultiple(), true);
            return true;
         }

         if (!(method instanceof AMQImpl.Basic.RecoverOk)) {
            if (method instanceof AMQImpl.Basic.Cancel) {
               AMQImpl.Basic.Cancel m = (AMQImpl.Basic.Cancel)method;
               String consumerTag = m.getConsumerTag();
               Consumer callback = this._consumers.remove(consumerTag);
               if (callback == null) {
                  callback = this.defaultConsumer;
               }

               if (callback != null) {
                  try {
                     this.dispatcher.handleCancel(callback, consumerTag);
                  } catch (WorkPoolFullException e) {
                     throw e;
                  } catch (Throwable ex) {
                     this.getConnection().getExceptionHandler().handleConsumerException(this, ex, callback, consumerTag, "handleCancel");
                  }
               } else {
                  LOGGER.warn("Could not cancel consumer with unknown tag {}", consumerTag);
               }

               return true;
            } else {
               return false;
            }
         } else {
            for (Entry<String, Consumer> entry : Utility.copy(this._consumers).entrySet()) {
               this.dispatcher.handleRecoverOk(entry.getValue(), entry.getKey());
            }

            return false;
         }
      }
   }

   protected void processDelivery(Command command, AMQImpl.Basic.Deliver method) {
      AMQImpl.Basic.Deliver m = method;
      Consumer callback = this._consumers.get(m.getConsumerTag());
      if (callback == null) {
         if (this.defaultConsumer == null) {
            throw new IllegalStateException("Unsolicited delivery - see Channel.setDefaultConsumer to handle this case.");
         }

         callback = this.defaultConsumer;
      }

      Envelope envelope = new Envelope(m.getDeliveryTag(), m.getRedelivered(), m.getExchange(), m.getRoutingKey());

      try {
         this.metricsCollector.consumedMessage(this, m.getDeliveryTag(), m.getConsumerTag());
         this.dispatcher.handleDelivery(callback, m.getConsumerTag(), envelope, (AMQP.BasicProperties)command.getContentHeader(), command.getContentBody());
      } catch (WorkPoolFullException e) {
         throw e;
      } catch (Throwable ex) {
         this.getConnection().getExceptionHandler().handleConsumerException(this, ex, callback, m.getConsumerTag(), "handleDelivery");
      }
   }

   private void callReturnListeners(Command command, AMQImpl.Basic.Return basicReturn) {
      try {
         for (ReturnListener l : this.returnListeners) {
            l.handleReturn(
               basicReturn.getReplyCode(),
               basicReturn.getReplyText(),
               basicReturn.getExchange(),
               basicReturn.getRoutingKey(),
               (AMQP.BasicProperties)command.getContentHeader(),
               command.getContentBody()
            );
         }
      } catch (Throwable ex) {
         this.getConnection().getExceptionHandler().handleReturnListenerException(this, ex);
      } finally {
         this.metricsCollector.basicPublishUnrouted(this);
      }
   }

   private void callConfirmListeners(Command command, AMQImpl.Basic.Ack ack) {
      try {
         for (ConfirmListener l : this.confirmListeners) {
            l.handleAck(ack.getDeliveryTag(), ack.getMultiple());
         }
      } catch (Throwable ex) {
         this.getConnection().getExceptionHandler().handleConfirmListenerException(this, ex);
      } finally {
         this.metricsCollector.basicPublishAck(this, ack.getDeliveryTag(), ack.getMultiple());
      }
   }

   private void callConfirmListeners(Command command, AMQImpl.Basic.Nack nack) {
      try {
         for (ConfirmListener l : this.confirmListeners) {
            l.handleNack(nack.getDeliveryTag(), nack.getMultiple());
         }
      } catch (Throwable ex) {
         this.getConnection().getExceptionHandler().handleConfirmListenerException(this, ex);
      } finally {
         this.metricsCollector.basicPublishNack(this, nack.getDeliveryTag(), nack.getMultiple());
      }
   }

   private void asyncShutdown(Command command) throws IOException {
      ShutdownSignalException signal = new ShutdownSignalException(false, false, command.getMethod(), this);
      this._channelLock.lock();

      try {
         try {
            this.processShutdownSignal(signal, true, false);
            this.quiescingTransmit(new AMQImpl.Channel.CloseOk());
         } finally {
            this.releaseChannel();
            this.notifyOutstandingRpc(signal);
         }
      } finally {
         this._channelLock.unlock();
      }

      this.notifyListeners();
   }

   @Override
   public void close() throws IOException, TimeoutException {
      this.close(200, "OK");
   }

   @Override
   public void close(int closeCode, String closeMessage) throws IOException, TimeoutException {
      this.close(closeCode, closeMessage, true, null, false);
   }

   @Override
   public void abort() throws IOException {
      this.abort(200, "OK");
   }

   @Override
   public void abort(int closeCode, String closeMessage) throws IOException {
      try {
         this.close(closeCode, closeMessage, true, null, true);
      } catch (IOException var4) {
      } catch (TimeoutException var5) {
      }
   }

   protected void close(int closeCode, String closeMessage, boolean initiatedByApplication, Throwable cause, boolean abort) throws IOException, TimeoutException {
      AMQImpl.Channel.Close reason = new AMQImpl.Channel.Close(closeCode, closeMessage, 0, 0);
      ShutdownSignalException signal = new ShutdownSignalException(false, initiatedByApplication, reason, this);
      if (cause != null) {
         signal.initCause(cause);
      }

      AMQChannel.BlockingRpcContinuation<AMQCommand> k = new AMQChannel.BlockingRpcContinuation<AMQCommand>() {
         public AMQCommand transformReply(AMQCommand command) {
            ChannelN.this.finishProcessShutdownSignal();
            return command;
         }
      };
      boolean notify = false;

      try {
         this._channelLock.lock();

         try {
            this.startProcessShutdownSignal(signal, !initiatedByApplication, true);
            this.quiescingRpc(reason, k);
         } finally {
            this._channelLock.unlock();
         }

         notify = true;
         k.getReply(10000);
      } catch (TimeoutException ise) {
         if (!abort) {
            throw ise;
         }
      } catch (ShutdownSignalException sse) {
         if (!abort) {
            throw sse;
         }
      } catch (IOException ioe) {
         if (!abort) {
            throw ioe;
         }
      } finally {
         if (abort || notify) {
            this.releaseChannel();
            this.notifyListeners();
         }
      }
   }

   @Override
   public void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException {
      int unsignedShortPrefetchCount = ConnectionFactory.ensureUnsignedShort(prefetchCount);
      if (unsignedShortPrefetchCount != prefetchCount) {
         LOGGER.warn(
            "Prefetch count must be between 0 and {}, value has been set to {} instead of {}", new Object[]{65535, unsignedShortPrefetchCount, prefetchCount}
         );
      }

      this.exnWrappingRpc(new AMQImpl.Basic.Qos(prefetchSize, unsignedShortPrefetchCount, global));
   }

   @Override
   public void basicQos(int prefetchCount, boolean global) throws IOException {
      this.basicQos(0, prefetchCount, global);
   }

   @Override
   public void basicQos(int prefetchCount) throws IOException {
      this.basicQos(0, prefetchCount, false);
   }

   @Override
   public void basicPublish(String exchange, String routingKey, AMQP.BasicProperties props, byte[] body) throws IOException {
      this.basicPublish(exchange, routingKey, false, props, body);
   }

   @Override
   public void basicPublish(String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, byte[] body) throws IOException {
      this.basicPublish(exchange, routingKey, mandatory, false, props, body);
   }

   @Override
   public void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, AMQP.BasicProperties props, byte[] body) throws IOException {
      if (this.nextPublishSeqNo > 0L) {
         this.unconfirmedSet.add(this.getNextPublishSeqNo());
         this.nextPublishSeqNo++;
      }

      if (props == null) {
         props = MessageProperties.MINIMAL_BASIC;
      }

      AMQP.Basic.Publish publish = new AMQP.Basic.Publish.Builder().exchange(exchange).routingKey(routingKey).mandatory(mandatory).immediate(immediate).build();

      try {
         ObservationCollector.PublishCall publishCall = properties -> {
            AMQCommand command = new AMQCommand(publish, properties, body);
            this.transmit(command);
         };
         this.observationCollector.publish(publishCall, publish, props, body, this.connectionInfo());
      } catch (IOException | AlreadyClosedException e) {
         this.metricsCollector.basicPublishFailure(this, e);
         throw e;
      }

      this.metricsCollector.basicPublish(this);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
      return this.exchangeDeclare(exchange, type, durable, autoDelete, false, arguments);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(
      String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, Map<String, Object> arguments
   ) throws IOException {
      return this.exchangeDeclare(exchange, type.getType(), durable, autoDelete, arguments);
   }

   @Override
   public void exchangeDeclareNoWait(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
      this.transmit(
         new AMQCommand(
            new AMQP.Exchange.Declare.Builder()
               .exchange(exchange)
               .type(type)
               .durable(durable)
               .autoDelete(autoDelete)
               .internal(internal)
               .arguments(arguments)
               .passive(false)
               .nowait(true)
               .build()
         )
      );
   }

   @Override
   public void exchangeDeclareNoWait(
      String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments
   ) throws IOException {
      this.exchangeDeclareNoWait(exchange, type.getType(), durable, autoDelete, internal, arguments);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(
      String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments
   ) throws IOException {
      return (AMQImpl.Exchange.DeclareOk)this.exnWrappingRpc(
            new AMQP.Exchange.Declare.Builder()
               .exchange(exchange)
               .type(type)
               .durable(durable)
               .autoDelete(autoDelete)
               .internal(internal)
               .arguments(arguments)
               .build()
         )
         .getMethod();
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(
      String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments
   ) throws IOException {
      return this.exchangeDeclare(exchange, type.getType(), durable, autoDelete, internal, arguments);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException {
      return this.exchangeDeclare(exchange, type, durable, false, null);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable) throws IOException {
      return this.exchangeDeclare(exchange, type.getType(), durable);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(String exchange, String type) throws IOException {
      return this.exchangeDeclare(exchange, type, false, false, null);
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type) throws IOException {
      return this.exchangeDeclare(exchange, type.getType());
   }

   public AMQImpl.Exchange.DeclareOk exchangeDeclarePassive(String exchange) throws IOException {
      return (AMQImpl.Exchange.DeclareOk)this.exnWrappingRpc(new AMQP.Exchange.Declare.Builder().exchange(exchange).type("").passive().build()).getMethod();
   }

   public AMQImpl.Exchange.DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException {
      return (AMQImpl.Exchange.DeleteOk)this.exnWrappingRpc(new AMQP.Exchange.Delete.Builder().exchange(exchange).ifUnused(ifUnused).build()).getMethod();
   }

   @Override
   public void exchangeDeleteNoWait(String exchange, boolean ifUnused) throws IOException {
      this.transmit(new AMQCommand(new AMQP.Exchange.Delete.Builder().exchange(exchange).ifUnused(ifUnused).nowait(true).build()));
   }

   public AMQImpl.Exchange.DeleteOk exchangeDelete(String exchange) throws IOException {
      return this.exchangeDelete(exchange, false);
   }

   public AMQImpl.Exchange.BindOk exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
      return (AMQImpl.Exchange.BindOk)this.exnWrappingRpc(
            new AMQP.Exchange.Bind.Builder().destination(destination).source(source).routingKey(routingKey).arguments(arguments).build()
         )
         .getMethod();
   }

   @Override
   public void exchangeBindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
      this.transmit(
         new AMQCommand(
            new AMQP.Exchange.Bind.Builder().destination(destination).source(source).routingKey(routingKey).arguments(arguments).nowait(true).build()
         )
      );
   }

   public AMQImpl.Exchange.BindOk exchangeBind(String destination, String source, String routingKey) throws IOException {
      return this.exchangeBind(destination, source, routingKey, null);
   }

   public AMQImpl.Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
      return (AMQImpl.Exchange.UnbindOk)this.exnWrappingRpc(
            new AMQP.Exchange.Unbind.Builder().destination(destination).source(source).routingKey(routingKey).arguments(arguments).build()
         )
         .getMethod();
   }

   public AMQImpl.Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey) throws IOException {
      return this.exchangeUnbind(destination, source, routingKey, null);
   }

   @Override
   public void exchangeUnbindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
      this.transmit(
         new AMQCommand(
            new AMQP.Exchange.Unbind.Builder().destination(destination).source(source).routingKey(routingKey).arguments(arguments).nowait(true).build()
         )
      );
   }

   public AMQImpl.Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
      validateQueueNameLength(queue);
      return (AMQImpl.Queue.DeclareOk)this.exnWrappingRpc(
            new AMQP.Queue.Declare.Builder().queue(queue).durable(durable).exclusive(exclusive).autoDelete(autoDelete).arguments(arguments).build()
         )
         .getMethod();
   }

   @Override
   public AMQP.Queue.DeclareOk queueDeclare() throws IOException {
      return this.queueDeclare("", false, true, true, null);
   }

   @Override
   public void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
      validateQueueNameLength(queue);
      this.transmit(
         new AMQCommand(
            new AMQP.Queue.Declare.Builder()
               .queue(queue)
               .durable(durable)
               .exclusive(exclusive)
               .autoDelete(autoDelete)
               .arguments(arguments)
               .passive(false)
               .nowait(true)
               .build()
         )
      );
   }

   public AMQImpl.Queue.DeclareOk queueDeclarePassive(String queue) throws IOException {
      validateQueueNameLength(queue);
      return (AMQImpl.Queue.DeclareOk)this.exnWrappingRpc(new AMQP.Queue.Declare.Builder().queue(queue).passive().exclusive().autoDelete().build()).getMethod();
   }

   @Override
   public long messageCount(String queue) throws IOException {
      AMQImpl.Queue.DeclareOk ok = this.queueDeclarePassive(queue);
      return ok.getMessageCount();
   }

   @Override
   public long consumerCount(String queue) throws IOException {
      AMQImpl.Queue.DeclareOk ok = this.queueDeclarePassive(queue);
      return ok.getConsumerCount();
   }

   public AMQImpl.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
      validateQueueNameLength(queue);
      return (AMQImpl.Queue.DeleteOk)this.exnWrappingRpc(new AMQP.Queue.Delete.Builder().queue(queue).ifUnused(ifUnused).ifEmpty(ifEmpty).build()).getMethod();
   }

   @Override
   public void queueDeleteNoWait(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
      validateQueueNameLength(queue);
      this.transmit(new AMQCommand(new AMQP.Queue.Delete.Builder().queue(queue).ifUnused(ifUnused).ifEmpty(ifEmpty).nowait(true).build()));
   }

   public AMQImpl.Queue.DeleteOk queueDelete(String queue) throws IOException {
      return this.queueDelete(queue, false, false);
   }

   public AMQImpl.Queue.BindOk queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
      validateQueueNameLength(queue);
      return (AMQImpl.Queue.BindOk)this.exnWrappingRpc(
            new AMQP.Queue.Bind.Builder().queue(queue).exchange(exchange).routingKey(routingKey).arguments(arguments).build()
         )
         .getMethod();
   }

   public AMQImpl.Queue.BindOk queueBind(String queue, String exchange, String routingKey) throws IOException {
      return this.queueBind(queue, exchange, routingKey, null);
   }

   @Override
   public void queueBindNoWait(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
      validateQueueNameLength(queue);
      this.transmit(
         new AMQCommand(new AMQP.Queue.Bind.Builder().queue(queue).exchange(exchange).routingKey(routingKey).arguments(arguments).nowait(true).build())
      );
   }

   public AMQImpl.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
      validateQueueNameLength(queue);
      return (AMQImpl.Queue.UnbindOk)this.exnWrappingRpc(
            new AMQP.Queue.Unbind.Builder().queue(queue).exchange(exchange).routingKey(routingKey).arguments(arguments).build()
         )
         .getMethod();
   }

   public AMQImpl.Queue.PurgeOk queuePurge(String queue) throws IOException {
      validateQueueNameLength(queue);
      return (AMQImpl.Queue.PurgeOk)this.exnWrappingRpc(new AMQP.Queue.Purge.Builder().queue(queue).build()).getMethod();
   }

   public AMQImpl.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey) throws IOException {
      return this.queueUnbind(queue, exchange, routingKey, null);
   }

   @Override
   public GetResponse basicGet(String queue, boolean autoAck) throws IOException {
      validateQueueNameLength(queue);
      AMQCommand replyCommand = this.exnWrappingRpc(new AMQP.Basic.Get.Builder().queue(queue).noAck(autoAck).build());
      return this.observationCollector.basicGet(() -> {
         me.neznamy.tab.libs.com.rabbitmq.client.Method method = replyCommand.getMethod();
         if (method instanceof AMQImpl.Basic.GetOk) {
            AMQImpl.Basic.GetOk getOk = (AMQImpl.Basic.GetOk)method;
            Envelope envelope = new Envelope(getOk.getDeliveryTag(), getOk.getRedelivered(), getOk.getExchange(), getOk.getRoutingKey());
            AMQP.BasicProperties props = (AMQP.BasicProperties)replyCommand.getContentHeader();
            byte[] body = replyCommand.getContentBody();
            int messageCount = getOk.getMessageCount();
            this.metricsCollector.consumedMessage(this, getOk.getDeliveryTag(), autoAck);
            return new GetResponse(envelope, props, body, messageCount);
         } else if (method instanceof AMQImpl.Basic.GetEmpty) {
            return null;
         } else {
            throw new UnexpectedMethodError(method);
         }
      }, queue);
   }

   @Override
   public void basicAck(long deliveryTag, boolean multiple) throws IOException {
      this.transmit(new AMQImpl.Basic.Ack(deliveryTag, multiple));
      this.metricsCollector.basicAck(this, deliveryTag, multiple);
   }

   @Override
   public void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException {
      this.transmit(new AMQImpl.Basic.Nack(deliveryTag, multiple, requeue));
      this.metricsCollector.basicNack(this, deliveryTag, requeue);
   }

   @Override
   public void basicReject(long deliveryTag, boolean requeue) throws IOException {
      this.transmit(new AMQImpl.Basic.Reject(deliveryTag, requeue));
      this.metricsCollector.basicReject(this, deliveryTag, requeue);
   }

   @Override
   public String basicConsume(String queue, Consumer callback) throws IOException {
      return this.basicConsume(queue, false, callback);
   }

   @Override
   public String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
      return this.basicConsume(queue, this.consumerFromDeliverCancelCallbacks(deliverCallback, cancelCallback));
   }

   @Override
   public String basicConsume(String queue, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
      return this.basicConsume(queue, this.consumerFromDeliverShutdownCallbacks(deliverCallback, shutdownSignalCallback));
   }

   @Override
   public String basicConsume(
      String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(queue, this.consumerFromDeliverCancelShutdownCallbacks(deliverCallback, cancelCallback, shutdownSignalCallback));
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException {
      return this.basicConsume(queue, autoAck, "", callback);
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException {
      return this.basicConsume(queue, autoAck, "", this.consumerFromDeliverShutdownCallbacks(deliverCallback, shutdownSignalCallback));
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
      return this.basicConsume(queue, autoAck, "", this.consumerFromDeliverCancelCallbacks(deliverCallback, cancelCallback));
   }

   @Override
   public String basicConsume(
      String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(queue, autoAck, "", this.consumerFromDeliverCancelShutdownCallbacks(deliverCallback, cancelCallback, shutdownSignalCallback));
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, Consumer callback) throws IOException {
      return this.basicConsume(queue, autoAck, "", false, false, arguments, callback);
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
      return this.basicConsume(queue, autoAck, "", false, false, arguments, this.consumerFromDeliverCancelCallbacks(deliverCallback, cancelCallback));
   }

   @Override
   public String basicConsume(
      String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(queue, autoAck, "", false, false, arguments, this.consumerFromDeliverShutdownCallbacks(deliverCallback, shutdownSignalCallback));
   }

   @Override
   public String basicConsume(
      String queue,
      boolean autoAck,
      Map<String, Object> arguments,
      DeliverCallback deliverCallback,
      CancelCallback cancelCallback,
      ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(
         queue, autoAck, "", false, false, arguments, this.consumerFromDeliverCancelShutdownCallbacks(deliverCallback, cancelCallback, shutdownSignalCallback)
      );
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, String consumerTag, Consumer callback) throws IOException {
      return this.basicConsume(queue, autoAck, consumerTag, false, false, null, callback);
   }

   @Override
   public String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException {
      return this.basicConsume(queue, autoAck, consumerTag, false, false, null, this.consumerFromDeliverCancelCallbacks(deliverCallback, cancelCallback));
   }

   @Override
   public String basicConsume(
      String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(
         queue, autoAck, consumerTag, false, false, null, this.consumerFromDeliverShutdownCallbacks(deliverCallback, shutdownSignalCallback)
      );
   }

   @Override
   public String basicConsume(
      String queue,
      boolean autoAck,
      String consumerTag,
      DeliverCallback deliverCallback,
      CancelCallback cancelCallback,
      ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(
         queue,
         autoAck,
         consumerTag,
         false,
         false,
         null,
         this.consumerFromDeliverCancelShutdownCallbacks(deliverCallback, cancelCallback, shutdownSignalCallback)
      );
   }

   @Override
   public String basicConsume(
      String queue,
      boolean autoAck,
      String consumerTag,
      boolean noLocal,
      boolean exclusive,
      Map<String, Object> arguments,
      DeliverCallback deliverCallback,
      CancelCallback cancelCallback
   ) throws IOException {
      return this.basicConsume(
         queue, autoAck, consumerTag, noLocal, exclusive, arguments, this.consumerFromDeliverCancelCallbacks(deliverCallback, cancelCallback)
      );
   }

   @Override
   public String basicConsume(
      String queue,
      boolean autoAck,
      String consumerTag,
      boolean noLocal,
      boolean exclusive,
      Map<String, Object> arguments,
      DeliverCallback deliverCallback,
      ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(
         queue, autoAck, consumerTag, noLocal, exclusive, arguments, this.consumerFromDeliverShutdownCallbacks(deliverCallback, shutdownSignalCallback)
      );
   }

   @Override
   public String basicConsume(
      String queue,
      boolean autoAck,
      String consumerTag,
      boolean noLocal,
      boolean exclusive,
      Map<String, Object> arguments,
      DeliverCallback deliverCallback,
      CancelCallback cancelCallback,
      ConsumerShutdownSignalCallback shutdownSignalCallback
   ) throws IOException {
      return this.basicConsume(
         queue,
         autoAck,
         consumerTag,
         noLocal,
         exclusive,
         arguments,
         this.consumerFromDeliverCancelShutdownCallbacks(deliverCallback, cancelCallback, shutdownSignalCallback)
      );
   }

   @Override
   public String basicConsume(
      final String queue,
      final boolean autoAck,
      final String consumerTag,
      boolean noLocal,
      boolean exclusive,
      Map<String, Object> arguments,
      final Consumer callback
   ) throws IOException {
      me.neznamy.tab.libs.com.rabbitmq.client.Method m = new AMQP.Basic.Consume.Builder()
         .queue(queue)
         .consumerTag(consumerTag)
         .noLocal(noLocal)
         .noAck(autoAck)
         .exclusive(exclusive)
         .arguments(arguments)
         .build();
      AMQChannel.BlockingRpcContinuation<String> k = new AMQChannel.BlockingRpcContinuation<String>(m) {
         public String transformReply(AMQCommand replyCommand) {
            String actualConsumerTag = ((AMQImpl.Basic.ConsumeOk)replyCommand.getMethod()).getConsumerTag();
            Consumer wrappedCallback = ChannelN.this.observationCollector.basicConsume(queue, consumerTag, callback);
            ChannelN.this._consumers.put(actualConsumerTag, wrappedCallback);
            ChannelN.this.metricsCollector.basicConsume(ChannelN.this, actualConsumerTag, autoAck);
            ChannelN.this.dispatcher.handleConsumeOk(wrappedCallback, actualConsumerTag);
            return actualConsumerTag;
         }
      };
      this.rpc(m, k);

      try {
         if (this._rpcTimeout == 0) {
            return k.getReply();
         }

         try {
            return k.getReply(this._rpcTimeout);
         } catch (TimeoutException e) {
            throw this.wrapTimeoutException(m, e);
         }
      } catch (ShutdownSignalException ex) {
         throw wrap(ex);
      }
   }

   private Consumer consumerFromDeliverCancelCallbacks(final DeliverCallback deliverCallback, final CancelCallback cancelCallback) {
      return new Consumer() {
         @Override
         public void handleConsumeOk(String consumerTag) {
         }

         @Override
         public void handleCancelOk(String consumerTag) {
         }

         @Override
         public void handleCancel(String consumerTag) throws IOException {
            cancelCallback.handle(consumerTag);
         }

         @Override
         public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
         }

         @Override
         public void handleRecoverOk(String consumerTag) {
         }

         @Override
         public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            deliverCallback.handle(consumerTag, new Delivery(envelope, properties, body));
         }
      };
   }

   private Consumer consumerFromDeliverShutdownCallbacks(final DeliverCallback deliverCallback, final ConsumerShutdownSignalCallback shutdownSignalCallback) {
      return new Consumer() {
         @Override
         public void handleConsumeOk(String consumerTag) {
         }

         @Override
         public void handleCancelOk(String consumerTag) {
         }

         @Override
         public void handleCancel(String consumerTag) throws IOException {
         }

         @Override
         public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            shutdownSignalCallback.handleShutdownSignal(consumerTag, sig);
         }

         @Override
         public void handleRecoverOk(String consumerTag) {
         }

         @Override
         public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            deliverCallback.handle(consumerTag, new Delivery(envelope, properties, body));
         }
      };
   }

   private Consumer consumerFromDeliverCancelShutdownCallbacks(
      final DeliverCallback deliverCallback, final CancelCallback cancelCallback, final ConsumerShutdownSignalCallback shutdownSignalCallback
   ) {
      return new Consumer() {
         @Override
         public void handleConsumeOk(String consumerTag) {
         }

         @Override
         public void handleCancelOk(String consumerTag) {
         }

         @Override
         public void handleCancel(String consumerTag) throws IOException {
            cancelCallback.handle(consumerTag);
         }

         @Override
         public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            shutdownSignalCallback.handleShutdownSignal(consumerTag, sig);
         }

         @Override
         public void handleRecoverOk(String consumerTag) {
         }

         @Override
         public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            deliverCallback.handle(consumerTag, new Delivery(envelope, properties, body));
         }
      };
   }

   @Override
   public void basicCancel(final String consumerTag) throws IOException {
      final Consumer originalConsumer = this._consumers.get(consumerTag);
      if (originalConsumer == null) {
         throw new IOException("Unknown consumerTag");
      }

      me.neznamy.tab.libs.com.rabbitmq.client.Method m = new AMQImpl.Basic.Cancel(consumerTag, false);
      AMQChannel.BlockingRpcContinuation<Consumer> k = new AMQChannel.BlockingRpcContinuation<Consumer>(m) {
         public Consumer transformReply(AMQCommand replyCommand) {
            if (!(replyCommand.getMethod() instanceof AMQImpl.Basic.CancelOk)) {
               ChannelN.LOGGER.warn("Received reply {} was not of expected method Basic.CancelOk", replyCommand.getMethod());
            }

            ChannelN.this._consumers.remove(consumerTag);
            ChannelN.this.dispatcher.handleCancelOk(originalConsumer, consumerTag);
            return originalConsumer;
         }
      };
      this.rpc(m, k);

      try {
         if (this._rpcTimeout == 0) {
            k.getReply();
         } else {
            try {
               k.getReply(this._rpcTimeout);
            } catch (TimeoutException e) {
               throw this.wrapTimeoutException(m, e);
            }
         }
      } catch (ShutdownSignalException ex) {
         throw wrap(ex);
      }

      this.metricsCollector.basicCancel(this, consumerTag);
   }

   public AMQImpl.Basic.RecoverOk basicRecover() throws IOException {
      return this.basicRecover(true);
   }

   public AMQImpl.Basic.RecoverOk basicRecover(boolean requeue) throws IOException {
      return (AMQImpl.Basic.RecoverOk)this.exnWrappingRpc(new AMQImpl.Basic.Recover(requeue)).getMethod();
   }

   public AMQImpl.Tx.SelectOk txSelect() throws IOException {
      return (AMQImpl.Tx.SelectOk)this.exnWrappingRpc(new AMQImpl.Tx.Select()).getMethod();
   }

   public AMQImpl.Tx.CommitOk txCommit() throws IOException {
      return (AMQImpl.Tx.CommitOk)this.exnWrappingRpc(new AMQImpl.Tx.Commit()).getMethod();
   }

   public AMQImpl.Tx.RollbackOk txRollback() throws IOException {
      return (AMQImpl.Tx.RollbackOk)this.exnWrappingRpc(new AMQImpl.Tx.Rollback()).getMethod();
   }

   public AMQImpl.Confirm.SelectOk confirmSelect() throws IOException {
      if (this.confirmSelectActivated) {
         return new AMQImpl.Confirm.SelectOk();
      }

      if (this.nextPublishSeqNo == 0L) {
         this.nextPublishSeqNo = 1L;
      }

      AMQImpl.Confirm.SelectOk result = (AMQImpl.Confirm.SelectOk)this.exnWrappingRpc(new AMQImpl.Confirm.Select(false)).getMethod();
      this.confirmSelectActivated = true;
      return result;
   }

   @Override
   public long getNextPublishSeqNo() {
      return this.nextPublishSeqNo;
   }

   @Override
   public void asyncRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method method) throws IOException {
      this.transmit(method);
   }

   @Override
   public AMQCommand rpc(me.neznamy.tab.libs.com.rabbitmq.client.Method method) throws IOException {
      return this.exnWrappingRpc(method);
   }

   @Override
   public CompletableFuture<Command> asyncCompletableRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method method) throws IOException {
      return this.exnWrappingAsyncRpc(method);
   }

   @Override
   public void enqueueRpc(AMQChannel.RpcContinuation k) {
      this._channelLock.lock();

      try {
         super.enqueueRpc(k);
         this.dispatcher.setUnlimited(true);
      } finally {
         this._channelLock.unlock();
      }
   }

   @Override
   protected void markRpcFinished() {
      this._channelLock.lock();

      try {
         this.dispatcher.setUnlimited(false);
      } finally {
         this._channelLock.unlock();
      }
   }

   private void handleAckNack(long seqNo, boolean multiple, boolean nack) {
      if (multiple) {
         this.unconfirmedSet.headSet(seqNo + 1L).clear();
      } else {
         this.unconfirmedSet.remove(seqNo);
      }

      synchronized (this.unconfirmedSet) {
         this.onlyAcksReceived = this.onlyAcksReceived && !nack;
         if (this.unconfirmedSet.isEmpty()) {
            this.unconfirmedSet.notifyAll();
         }
      }
   }

   private static void validateQueueNameLength(String queue) {
      if (queue.length() > 255) {
         throw new IllegalArgumentException("queue name must be no more than 255 characters long");
      }
   }
}
