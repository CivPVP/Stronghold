package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.DataOutputStream;
import java.io.IOException;

public interface WriteRequest {
   void handle(DataOutputStream var1) throws IOException;
}
