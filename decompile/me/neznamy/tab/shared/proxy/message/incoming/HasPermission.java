package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class HasPermission implements IncomingMessage {
   private String permission;
   private boolean value;

   @Override
   public void read(@NotNull ByteArrayDataInput in) {
      this.permission = in.readUTF();
      this.value = in.readBoolean();
   }

   @Override
   public void process(@NotNull ProxyTabPlayer player) {
      player.setHasPermission(this.permission, this.value);
   }
}
