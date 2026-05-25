package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import me.neznamy.tab.libs.com.rabbitmq.client.SocketConfigurator;

public abstract class AbstractFrameHandlerFactory implements FrameHandlerFactory {
   protected final int connectionTimeout;
   protected final SocketConfigurator configurator;
   protected final boolean ssl;
   protected final int maxInboundMessageBodySize;

   protected AbstractFrameHandlerFactory(int connectionTimeout, SocketConfigurator configurator, boolean ssl, int maxInboundMessageBodySize) {
      this.connectionTimeout = connectionTimeout;
      this.configurator = configurator;
      this.ssl = ssl;
      this.maxInboundMessageBodySize = maxInboundMessageBodySize;
   }
}
