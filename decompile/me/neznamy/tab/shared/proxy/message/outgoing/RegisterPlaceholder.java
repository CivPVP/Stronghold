package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class RegisterPlaceholder implements OutgoingMessage {
   private String identifier;
   private int refresh;

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("Placeholder");
      out.writeUTF(this.identifier);
      out.writeInt(this.refresh);
      return out;
   }

   @Generated
   public RegisterPlaceholder(String identifier, int refresh) {
      this.identifier = identifier;
      this.refresh = refresh;
   }
}
