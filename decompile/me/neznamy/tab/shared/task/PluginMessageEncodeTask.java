package me.neznamy.tab.shared.task;

import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.OutgoingMessage;

public class PluginMessageEncodeTask implements Runnable {
   private final ProxyTabPlayer player;
   private final OutgoingMessage message;

   @Override
   public void run() {
      long time = System.nanoTime();
      byte[] msg = this.message.write().toByteArray();
      TAB.getInstance().getCpu().addTime("Plugin message handling", "Encoding message", System.nanoTime() - time);
      time = System.nanoTime();
      this.player.sendPluginMessage(msg);
      TAB.getInstance().getCpu().addTime("Plugin message handling", "Sending message", System.nanoTime() - time);
   }

   @Generated
   public PluginMessageEncodeTask(ProxyTabPlayer player, OutgoingMessage message) {
      this.player = player;
      this.message = message;
   }
}
