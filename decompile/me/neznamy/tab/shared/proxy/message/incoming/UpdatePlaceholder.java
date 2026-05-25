package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class UpdatePlaceholder implements IncomingMessage {
   private String identifier;
   private String target;
   private String value;

   @Override
   public void read(@NotNull ByteArrayDataInput in) {
      this.identifier = in.readUTF();
      if (this.identifier.startsWith("%rel_")) {
         this.target = in.readUTF();
      }

      this.value = in.readUTF();
   }

   @Override
   public void process(@NotNull ProxyTabPlayer player) {
      if (TAB.getInstance().getPlaceholderManager().getBridgePlaceholders().containsKey(this.identifier)) {
         Placeholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholderRaw(this.identifier);
         if (placeholder != null) {
            if (placeholder instanceof RelationalPlaceholder) {
               TabPlayer other = TAB.getInstance().getPlayer(this.target);
               if (other != null) {
                  ((RelationalPlaceholder)placeholder).updateValue(player, other, this.value);
               }
            } else if (placeholder instanceof PlayerPlaceholder) {
               ((PlayerPlaceholder)placeholder).updateValue(player, this.value);
            }
         }
      }
   }
}
