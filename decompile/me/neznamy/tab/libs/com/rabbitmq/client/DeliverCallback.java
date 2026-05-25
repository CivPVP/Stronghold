package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

@FunctionalInterface
public interface DeliverCallback {
   void handle(String var1, Delivery var2) throws IOException;
}
