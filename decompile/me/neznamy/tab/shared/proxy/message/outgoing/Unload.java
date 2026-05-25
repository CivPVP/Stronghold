package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;

public class Unload implements OutgoingMessage {
   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("Unload");
      return out;
   }
}
