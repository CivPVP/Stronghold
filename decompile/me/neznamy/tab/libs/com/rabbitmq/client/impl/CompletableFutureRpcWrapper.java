package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.concurrent.CompletableFuture;
import me.neznamy.tab.libs.com.rabbitmq.client.Command;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;

public class CompletableFutureRpcWrapper implements RpcWrapper {
   private final me.neznamy.tab.libs.com.rabbitmq.client.Method request;
   private final CompletableFuture<Command> completableFuture;

   public CompletableFutureRpcWrapper(me.neznamy.tab.libs.com.rabbitmq.client.Method method, CompletableFuture<Command> completableFuture) {
      this.request = method;
      this.completableFuture = completableFuture;
   }

   @Override
   public boolean canHandleReply(AMQCommand command) {
      return AMQChannel.SimpleBlockingRpcContinuation.isResponseCompatibleWithRequest(this.request, command.getMethod());
   }

   @Override
   public void complete(AMQCommand command) {
      this.completableFuture.complete(command);
   }

   @Override
   public void shutdown(ShutdownSignalException signal) {
      this.completableFuture.completeExceptionally(signal);
   }
}
