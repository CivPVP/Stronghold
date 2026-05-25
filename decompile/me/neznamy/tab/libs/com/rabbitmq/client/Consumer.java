package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public interface Consumer {
   void handleConsumeOk(String var1);

   void handleCancelOk(String var1);

   void handleCancel(String var1) throws IOException;

   void handleShutdownSignal(String var1, ShutdownSignalException var2);

   void handleRecoverOk(String var1);

   void handleDelivery(String var1, Envelope var2, AMQP.BasicProperties var3, byte[] var4) throws IOException;
}
