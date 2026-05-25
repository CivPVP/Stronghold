package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.nio.ByteBuffer;

public interface ByteBufferFactory {
   ByteBuffer createReadBuffer(NioContext var1);

   ByteBuffer createWriteBuffer(NioContext var1);

   ByteBuffer createEncryptedReadBuffer(NioContext var1);

   ByteBuffer createEncryptedWriteBuffer(NioContext var1);
}
