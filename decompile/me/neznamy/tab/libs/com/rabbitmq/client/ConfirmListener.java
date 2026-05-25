package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public interface ConfirmListener {
   void handleAck(long var1, boolean var3) throws IOException;

   void handleNack(long var1, boolean var3) throws IOException;
}
