package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.Address;

public interface FrameHandlerFactory {
   FrameHandler create(Address var1, String var2) throws IOException;
}
