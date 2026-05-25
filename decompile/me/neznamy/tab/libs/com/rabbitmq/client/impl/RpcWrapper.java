package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;

public interface RpcWrapper {
   boolean canHandleReply(AMQCommand var1);

   void complete(AMQCommand var1);

   void shutdown(ShutdownSignalException var1);
}
