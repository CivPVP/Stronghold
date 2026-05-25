package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.nio.channels.Selector;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SelectorHolder {
   final Selector selector;
   final Set<SocketChannelRegistration> registrations = Collections.newSetFromMap(new ConcurrentHashMap<>());

   SelectorHolder(Selector selector) {
      this.selector = selector;
   }

   public void registerFrameHandlerState(SocketChannelFrameHandlerState state, int operations) {
      this.registrations.add(new SocketChannelRegistration(state, operations));
      this.selector.wakeup();
   }
}
