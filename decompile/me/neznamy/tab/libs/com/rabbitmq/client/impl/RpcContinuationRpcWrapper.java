package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;

public class RpcContinuationRpcWrapper implements RpcWrapper {
   private final AMQChannel.RpcContinuation continuation;

   public RpcContinuationRpcWrapper(AMQChannel.RpcContinuation continuation) {
      this.continuation = continuation;
   }

   @Override
   public boolean canHandleReply(AMQCommand command) {
      return this.continuation.canHandleReply(command);
   }

   @Override
   public void complete(AMQCommand command) {
      this.continuation.handleCommand(command);
   }

   @Override
   public void shutdown(ShutdownSignalException signal) {
      this.continuation.handleShutdownSignal(signal);
   }
}
