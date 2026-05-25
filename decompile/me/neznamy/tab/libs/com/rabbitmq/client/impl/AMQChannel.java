package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.AlreadyClosedException;
import me.neznamy.tab.libs.com.rabbitmq.client.ChannelContinuationTimeoutException;
import me.neznamy.tab.libs.com.rabbitmq.client.Command;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.TrafficListener;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;
import me.neznamy.tab.libs.com.rabbitmq.utility.BlockingValueOrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AMQChannel extends ShutdownNotifierComponent {
   private static final Logger LOGGER = LoggerFactory.getLogger(AMQChannel.class);
   protected static final int NO_RPC_TIMEOUT = 0;
   protected final Lock _channelLock = new ReentrantLock();
   protected final Condition _channelLockCondition = this._channelLock.newCondition();
   private final AMQConnection _connection;
   private final int _channelNumber;
   private AMQCommand _command;
   private RpcWrapper _activeRpc = null;
   protected volatile boolean _blockContent = false;
   protected final int _rpcTimeout;
   private final boolean _checkRpcResponseType;
   private final TrafficListener _trafficListener;
   private final int maxInboundMessageBodySize;
   private final ObservationCollector.ConnectionInfo connectionInfo;

   public AMQChannel(AMQConnection connection, int channelNumber) {
      this._connection = connection;
      this._channelNumber = channelNumber;
      if (connection.getChannelRpcTimeout() < 0) {
         throw new IllegalArgumentException("Continuation timeout on RPC calls cannot be less than 0");
      }

      this._rpcTimeout = connection.getChannelRpcTimeout();
      this._checkRpcResponseType = connection.willCheckRpcResponseType();
      this._trafficListener = connection.getTrafficListener();
      this.maxInboundMessageBodySize = connection.getMaxInboundMessageBodySize();
      this._command = new AMQCommand(this.maxInboundMessageBodySize);
      this.connectionInfo = connection.connectionInfo();
   }

   public int getChannelNumber() {
      return this._channelNumber;
   }

   public void handleFrame(Frame frame) throws IOException {
      AMQCommand command = this._command;
      if (command.handleFrame(frame)) {
         this._command = new AMQCommand(this.maxInboundMessageBodySize);
         this.handleCompleteInboundCommand(command);
      }
   }

   public static IOException wrap(ShutdownSignalException ex) {
      return wrap(ex, null);
   }

   public static IOException wrap(ShutdownSignalException ex, String message) {
      IOException ioe = new IOException(message);
      ioe.initCause(ex);
      return ioe;
   }

   public AMQCommand exnWrappingRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException {
      try {
         return this.privateRpc(m);
      } catch (AlreadyClosedException ace) {
         throw ace;
      } catch (ShutdownSignalException ex) {
         throw wrap(ex);
      }
   }

   public CompletableFuture<Command> exnWrappingAsyncRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException {
      try {
         return this.privateAsyncRpc(m);
      } catch (AlreadyClosedException ace) {
         throw ace;
      } catch (ShutdownSignalException ex) {
         throw wrap(ex);
      }
   }

   public void handleCompleteInboundCommand(AMQCommand command) throws IOException {
      this._trafficListener.read(command);
      if (!this.processAsync(command)) {
         if (this._checkRpcResponseType) {
            this._channelLock.lock();

            try {
               if (this._activeRpc != null && !this._activeRpc.canHandleReply(command)) {
                  return;
               }
            } finally {
               this._channelLock.unlock();
            }
         }

         RpcWrapper nextOutstandingRpc = this.nextOutstandingRpc();
         if (nextOutstandingRpc != null) {
            nextOutstandingRpc.complete(command);
            this.markRpcFinished();
         }
      }
   }

   public void enqueueRpc(AMQChannel.RpcContinuation k) {
      this.doEnqueueRpc(() -> new RpcContinuationRpcWrapper(k));
   }

   public void enqueueAsyncRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method method, CompletableFuture<Command> future) {
      this.doEnqueueRpc(() -> new CompletableFutureRpcWrapper(method, future));
   }

   private void doEnqueueRpc(Supplier<RpcWrapper> rpcWrapperSupplier) {
      this._channelLock.lock();

      try {
         boolean waitClearedInterruptStatus = false;

         while (this._activeRpc != null) {
            try {
               this._channelLockCondition.await();
            } catch (InterruptedException e) {
               waitClearedInterruptStatus = true;
            }
         }

         if (waitClearedInterruptStatus) {
            Thread.currentThread().interrupt();
         }

         this._activeRpc = rpcWrapperSupplier.get();
      } finally {
         this._channelLock.unlock();
      }
   }

   public boolean isOutstandingRpc() {
      this._channelLock.lock();

      try {
         return this._activeRpc != null;
      } finally {
         this._channelLock.unlock();
      }
   }

   public RpcWrapper nextOutstandingRpc() {
      this._channelLock.lock();

      try {
         RpcWrapper result = this._activeRpc;
         this._activeRpc = null;
         this._channelLockCondition.signalAll();
         return result;
      } finally {
         this._channelLock.unlock();
      }
   }

   protected void markRpcFinished() {
   }

   public void ensureIsOpen() throws AlreadyClosedException {
      if (!this.isOpen()) {
         throw new AlreadyClosedException(this.getCloseReason());
      }
   }

   public AMQCommand rpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException, ShutdownSignalException {
      return this.privateRpc(m);
   }

   public AMQCommand rpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m, int timeout) throws IOException, ShutdownSignalException, TimeoutException {
      return this.privateRpc(m, timeout);
   }

   private AMQCommand privateRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException, ShutdownSignalException {
      AMQChannel.SimpleBlockingRpcContinuation k = new AMQChannel.SimpleBlockingRpcContinuation(m);
      this.rpc(m, k);
      if (this._rpcTimeout == 0) {
         return k.getReply();
      }

      try {
         return k.getReply(this._rpcTimeout);
      } catch (TimeoutException e) {
         throw this.wrapTimeoutException(m, e);
      }
   }

   private void cleanRpcChannelState() {
      try {
         this.nextOutstandingRpc();
         this.markRpcFinished();
      } catch (Exception ex) {
         LOGGER.warn("Error while cleaning timed out channel RPC: {}", ex.getMessage());
      }
   }

   protected ChannelContinuationTimeoutException wrapTimeoutException(me.neznamy.tab.libs.com.rabbitmq.client.Method m, TimeoutException e) {
      this.cleanRpcChannelState();
      return new ChannelContinuationTimeoutException(e, this, this._channelNumber, m);
   }

   private CompletableFuture<Command> privateAsyncRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException, ShutdownSignalException {
      CompletableFuture<Command> future = new CompletableFuture<>();
      this.asyncRpc(m, future);
      return future;
   }

   private AMQCommand privateRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m, int timeout) throws IOException, ShutdownSignalException, TimeoutException {
      AMQChannel.SimpleBlockingRpcContinuation k = new AMQChannel.SimpleBlockingRpcContinuation(m);
      this.rpc(m, k);

      try {
         return k.getReply(timeout);
      } catch (TimeoutException e) {
         this.cleanRpcChannelState();
         throw e;
      }
   }

   public void rpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m, AMQChannel.RpcContinuation k) throws IOException {
      this._channelLock.lock();

      try {
         this.ensureIsOpen();
         this.quiescingRpc(m, k);
      } finally {
         this._channelLock.unlock();
      }
   }

   public void quiescingRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m, AMQChannel.RpcContinuation k) throws IOException {
      this._channelLock.lock();

      try {
         this.enqueueRpc(k);
         this.quiescingTransmit(m);
      } finally {
         this._channelLock.unlock();
      }
   }

   public void asyncRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m, CompletableFuture<Command> future) throws IOException {
      this._channelLock.lock();

      try {
         this.ensureIsOpen();
         this.quiescingAsyncRpc(m, future);
      } finally {
         this._channelLock.unlock();
      }
   }

   public void quiescingAsyncRpc(me.neznamy.tab.libs.com.rabbitmq.client.Method m, CompletableFuture<Command> future) throws IOException {
      this._channelLock.lock();

      try {
         this.enqueueAsyncRpc(m, future);
         this.quiescingTransmit(m);
      } finally {
         this._channelLock.unlock();
      }
   }

   public abstract boolean processAsync(Command var1) throws IOException;

   @Override
   public String toString() {
      return "AMQChannel(" + this._connection + "," + this._channelNumber + ")";
   }

   public void processShutdownSignal(ShutdownSignalException signal, boolean ignoreClosed, boolean notifyRpc) {
      try {
         this._channelLock.lock();

         try {
            if (!this.setShutdownCauseIfOpen(signal) && !ignoreClosed) {
               throw new AlreadyClosedException(this.getCloseReason());
            }

            this._channelLockCondition.signalAll();
         } finally {
            this._channelLock.unlock();
         }
      } finally {
         if (notifyRpc) {
            this.notifyOutstandingRpc(signal);
         }
      }
   }

   public void notifyOutstandingRpc(ShutdownSignalException signal) {
      RpcWrapper k = this.nextOutstandingRpc();
      if (k != null) {
         k.shutdown(signal);
      }
   }

   public void transmit(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException {
      this._channelLock.lock();

      try {
         this.transmit(new AMQCommand(m));
      } finally {
         this._channelLock.unlock();
      }
   }

   public void transmit(AMQCommand c) throws IOException {
      this._channelLock.lock();

      try {
         this.ensureIsOpen();
         this.quiescingTransmit(c);
      } finally {
         this._channelLock.unlock();
      }
   }

   public void quiescingTransmit(me.neznamy.tab.libs.com.rabbitmq.client.Method m) throws IOException {
      this._channelLock.lock();

      try {
         this.quiescingTransmit(new AMQCommand(m));
      } finally {
         this._channelLock.unlock();
      }
   }

   public void quiescingTransmit(AMQCommand c) throws IOException {
      this._channelLock.lock();

      try {
         if (c.getMethod().hasContent()) {
            for (; this._blockContent; this.ensureIsOpen()) {
               try {
                  this._channelLockCondition.await();
               } catch (InterruptedException ignored) {
                  Thread.currentThread().interrupt();
               }
            }
         }

         this._trafficListener.write(c);
         c.transmit(this);
      } finally {
         this._channelLock.unlock();
      }
   }

   public AMQConnection getConnection() {
      return this._connection;
   }

   protected ObservationCollector.ConnectionInfo connectionInfo() {
      return this.connectionInfo;
   }

   public abstract static class BlockingRpcContinuation<T> implements AMQChannel.RpcContinuation {
      public final BlockingValueOrException<T, ShutdownSignalException> _blocker = new BlockingValueOrException<>();
      protected final me.neznamy.tab.libs.com.rabbitmq.client.Method request;

      public BlockingRpcContinuation() {
         this.request = null;
      }

      public BlockingRpcContinuation(me.neznamy.tab.libs.com.rabbitmq.client.Method request) {
         this.request = request;
      }

      @Override
      public void handleCommand(AMQCommand command) {
         this._blocker.setValue(this.transformReply(command));
      }

      @Override
      public void handleShutdownSignal(ShutdownSignalException signal) {
         this._blocker.setException(signal);
      }

      public T getReply() throws ShutdownSignalException {
         return this._blocker.uninterruptibleGetValue();
      }

      public T getReply(int timeout) throws ShutdownSignalException, TimeoutException {
         return this._blocker.uninterruptibleGetValue(timeout);
      }

      @Override
      public boolean canHandleReply(AMQCommand command) {
         return isResponseCompatibleWithRequest(this.request, command.getMethod());
      }

      public abstract T transformReply(AMQCommand var1);

      public static boolean isResponseCompatibleWithRequest(
         me.neznamy.tab.libs.com.rabbitmq.client.Method request, me.neznamy.tab.libs.com.rabbitmq.client.Method response
      ) {
         if (request != null) {
            if (request instanceof AMQP.Basic.Qos) {
               return response instanceof AMQP.Basic.QosOk;
            }

            if (request instanceof AMQP.Basic.Get) {
               return response instanceof AMQP.Basic.GetOk || response instanceof AMQP.Basic.GetEmpty;
            }

            if (request instanceof AMQP.Basic.Consume) {
               if (!(response instanceof AMQP.Basic.ConsumeOk)) {
                  return false;
               }

               String consumerTag = ((AMQP.Basic.Consume)request).getConsumerTag();
               return consumerTag == null || consumerTag.equals("") || consumerTag.equals(((AMQP.Basic.ConsumeOk)response).getConsumerTag());
            }

            if (request instanceof AMQP.Basic.Cancel) {
               if (!(response instanceof AMQP.Basic.CancelOk)) {
                  return false;
               }

               return ((AMQP.Basic.Cancel)request).getConsumerTag().equals(((AMQP.Basic.CancelOk)response).getConsumerTag());
            }

            if (request instanceof AMQP.Basic.Recover) {
               return response instanceof AMQP.Basic.RecoverOk;
            }

            if (request instanceof AMQP.Exchange.Declare) {
               return response instanceof AMQP.Exchange.DeclareOk;
            }

            if (request instanceof AMQP.Exchange.Delete) {
               return response instanceof AMQP.Exchange.DeleteOk;
            }

            if (request instanceof AMQP.Exchange.Bind) {
               return response instanceof AMQP.Exchange.BindOk;
            }

            if (request instanceof AMQP.Exchange.Unbind) {
               return response instanceof AMQP.Exchange.UnbindOk;
            }

            if (request instanceof AMQP.Queue.Declare) {
               return response instanceof AMQP.Queue.DeclareOk;
            }

            if (request instanceof AMQP.Queue.Delete) {
               return response instanceof AMQP.Queue.DeleteOk;
            }

            if (request instanceof AMQP.Queue.Bind) {
               return response instanceof AMQP.Queue.BindOk;
            }

            if (request instanceof AMQP.Queue.Unbind) {
               return response instanceof AMQP.Queue.UnbindOk;
            }

            if (request instanceof AMQP.Queue.Purge) {
               return response instanceof AMQP.Queue.PurgeOk;
            }

            if (request instanceof AMQP.Tx.Select) {
               return response instanceof AMQP.Tx.SelectOk;
            }

            if (request instanceof AMQP.Tx.Commit) {
               return response instanceof AMQP.Tx.CommitOk;
            }

            if (request instanceof AMQP.Tx.Rollback) {
               return response instanceof AMQP.Tx.RollbackOk;
            }

            if (request instanceof AMQP.Confirm.Select) {
               return response instanceof AMQP.Confirm.SelectOk;
            }
         }

         return true;
      }
   }

   public interface RpcContinuation {
      void handleCommand(AMQCommand var1);

      boolean canHandleReply(AMQCommand var1);

      void handleShutdownSignal(ShutdownSignalException var1);
   }

   public static class SimpleBlockingRpcContinuation extends AMQChannel.BlockingRpcContinuation<AMQCommand> {
      public SimpleBlockingRpcContinuation() {
      }

      public SimpleBlockingRpcContinuation(me.neznamy.tab.libs.com.rabbitmq.client.Method method) {
         super(method);
      }

      public AMQCommand transformReply(AMQCommand command) {
         return command;
      }
   }
}
