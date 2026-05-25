package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class ExpansionPlaceholder implements OutgoingMessage {
   private String placeholder;
   private String value;

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("Expansion");
      out.writeUTF(this.placeholder);
      out.writeUTF(this.value);
      return out;
   }

   @Generated
   public ExpansionPlaceholder(String placeholder, String value) {
      this.placeholder = placeholder;
      this.value = value;
   }
}
