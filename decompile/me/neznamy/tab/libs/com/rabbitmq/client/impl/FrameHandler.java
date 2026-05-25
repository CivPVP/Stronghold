package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.net.SocketException;

public interface FrameHandler extends NetworkConnection {
   void setTimeout(int var1) throws SocketException;

   int getTimeout() throws SocketException;

   void sendHeader() throws IOException;

   void initialize(AMQConnection var1);

   Frame readFrame() throws IOException;

   void writeFrame(Frame var1) throws IOException;

   void flush() throws IOException;

   void close();
}
