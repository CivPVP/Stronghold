package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

@FunctionalInterface
public interface ConfirmCallback {
   void handle(long var1, boolean var3) throws IOException;
}
