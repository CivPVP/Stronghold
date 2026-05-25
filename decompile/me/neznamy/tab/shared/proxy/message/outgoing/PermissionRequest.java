package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class PermissionRequest implements OutgoingMessage {
   private String permission;

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("Permission");
      out.writeUTF(this.permission);
      return out;
   }

   @Generated
   public PermissionRequest(String permission) {
      this.permission = permission;
   }
}
