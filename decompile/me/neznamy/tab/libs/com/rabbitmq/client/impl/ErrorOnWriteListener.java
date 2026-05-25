package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;

public interface ErrorOnWriteListener {
   void handle(Connection var1, IOException var2) throws IOException;
}
