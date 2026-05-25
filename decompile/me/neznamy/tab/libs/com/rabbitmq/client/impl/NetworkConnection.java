package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.net.InetAddress;

public interface NetworkConnection {
   InetAddress getLocalAddress();

   int getLocalPort();

   InetAddress getAddress();

   int getPort();
}
