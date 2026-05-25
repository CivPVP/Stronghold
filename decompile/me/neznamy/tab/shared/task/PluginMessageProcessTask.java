package me.neznamy.tab.shared.task;

import lombok.Generated;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.incoming.IncomingMessage;

public class PluginMessageProcessTask implements Runnable {
   private final IncomingMessage message;
   private final ProxyTabPlayer player;

   @Override
   public void run() {
      this.message.process(this.player);
   }

   @Generated
   public PluginMessageProcessTask(IncomingMessage message, ProxyTabPlayer player) {
      this.message = message;
      this.player = player;
   }
}
