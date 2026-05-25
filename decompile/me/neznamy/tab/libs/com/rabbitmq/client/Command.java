package me.neznamy.tab.libs.com.rabbitmq.client;

public interface Command {
   Method getMethod();

   ContentHeader getContentHeader();

   byte[] getContentBody();
}
