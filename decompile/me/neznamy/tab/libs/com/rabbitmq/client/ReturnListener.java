package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public interface ReturnListener {
   void handleReturn(int var1, String var2, String var3, String var4, AMQP.BasicProperties var5, byte[] var6) throws IOException;
}
