package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

@FunctionalInterface
public interface BlockedCallback {
   void handle(String var1) throws IOException;
}
