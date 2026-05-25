package me.neznamy.tab.shared.task;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.incoming.Disguised;
import me.neznamy.tab.shared.proxy.message.incoming.HasPermission;
import me.neznamy.tab.shared.proxy.message.incoming.IncomingMessage;
import me.neznamy.tab.shared.proxy.message.incoming.Invisible;
import me.neznamy.tab.shared.proxy.message.incoming.PlaceholderError;
import me.neznamy.tab.shared.proxy.message.incoming.PlayerJoinResponse;
import me.neznamy.tab.shared.proxy.message.incoming.RegisterPlaceholder;
import me.neznamy.tab.shared.proxy.message.incoming.SetGroup;
import me.neznamy.tab.shared.proxy.message.incoming.SetWorld;
import me.neznamy.tab.shared.proxy.message.incoming.UpdateGameMode;
import me.neznamy.tab.shared.proxy.message.incoming.UpdatePlaceholder;
import me.neznamy.tab.shared.proxy.message.incoming.Vanished;

public class PluginMessageDecodeTask implements Runnable {
   private static final Supplier<IncomingMessage>[] registeredMessages = new Supplier[]{
      PlaceholderError::new,
      UpdateGameMode::new,
      HasPermission::new,
      Invisible::new,
      Disguised::new,
      SetWorld::new,
      SetGroup::new,
      Vanished::new,
      UpdatePlaceholder::new,
      PlayerJoinResponse::new,
      RegisterPlaceholder::new
   };
   private final UUID playerId;
   private final byte[] bytes;

   @Override
   public void run() {
      ProxyTabPlayer player = (ProxyTabPlayer)TAB.getInstance().getPlayer(this.playerId);
      if (player != null) {
         ByteArrayDataInput in = ByteStreams.newDataInput(this.bytes);
         Supplier<IncomingMessage> supplier = registeredMessages[in.readByte()];
         IncomingMessage msg = supplier.get();
         msg.read(in);
         TAB.getInstance().getCpu().runMeasuredTask("Plugin message handling", "Processing message", new PluginMessageProcessTask(msg, player));
      }
   }

   @Generated
   public PluginMessageDecodeTask(UUID playerId, byte[] bytes) {
      this.playerId = playerId;
      this.bytes = bytes;
   }
}
