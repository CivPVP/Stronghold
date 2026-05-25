package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;

public interface Connection extends ShutdownNotifier, Closeable {
   InetAddress getAddress();

   int getPort();

   int getChannelMax();

   int getFrameMax();

   int getHeartbeat();

   Map<String, Object> getClientProperties();

   String getClientProvidedName();

   Map<String, Object> getServerProperties();

   Channel createChannel() throws IOException;

   Channel createChannel(int var1) throws IOException;

   default Optional<Channel> openChannel() throws IOException {
      return Optional.ofNullable(this.createChannel());
   }

   default Optional<Channel> openChannel(int channelNumber) throws IOException {
      return Optional.ofNullable(this.createChannel(channelNumber));
   }

   @Override
   void close() throws IOException;

   void close(int var1, String var2) throws IOException;

   void close(int var1) throws IOException;

   void close(int var1, String var2, int var3) throws IOException;

   void abort();

   void abort(int var1, String var2);

   void abort(int var1);

   void abort(int var1, String var2, int var3);

   void addBlockedListener(BlockedListener var1);

   BlockedListener addBlockedListener(BlockedCallback var1, UnblockedCallback var2);

   boolean removeBlockedListener(BlockedListener var1);

   void clearBlockedListeners();

   ExceptionHandler getExceptionHandler();

   String getId();

   void setId(String var1);
}
