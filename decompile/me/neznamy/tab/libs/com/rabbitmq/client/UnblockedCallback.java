package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

@FunctionalInterface
public interface UnblockedCallback {
   void handle() throws IOException;
}
