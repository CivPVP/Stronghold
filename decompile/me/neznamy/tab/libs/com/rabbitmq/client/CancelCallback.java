package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

@FunctionalInterface
public interface CancelCallback {
   void handle(String var1) throws IOException;
}
