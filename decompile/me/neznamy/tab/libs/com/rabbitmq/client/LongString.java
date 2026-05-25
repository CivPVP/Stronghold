package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.DataInputStream;
import java.io.IOException;

public interface LongString {
   long MAX_LENGTH = 4294967295L;

   long length();

   DataInputStream getStream() throws IOException;

   byte[] getBytes();

   @Override
   String toString();
}
