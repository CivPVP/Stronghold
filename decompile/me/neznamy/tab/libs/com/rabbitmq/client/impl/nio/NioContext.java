package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import javax.net.ssl.SSLEngine;

public class NioContext {
   private final NioParams nioParams;
   private final SSLEngine sslEngine;

   NioContext(NioParams nioParams, SSLEngine sslEngine) {
      this.nioParams = nioParams;
      this.sslEngine = sslEngine;
   }

   public NioParams getNioParams() {
      return this.nioParams;
   }

   public SSLEngine getSslEngine() {
      return this.sslEngine;
   }
}
