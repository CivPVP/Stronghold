package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public interface BlockedListener {
   void handleBlocked(String var1) throws IOException;

   void handleUnblocked() throws IOException;
}
