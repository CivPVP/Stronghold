package me.neznamy.tab.shared.proxy;

import java.util.Map.Entry;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.ExpansionPlaceholder;
import org.jetbrains.annotations.NotNull;

public class ProxyTabExpansion implements TabExpansion {
   @Override
   public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
      player.expansionValues.put(key, value);
      ((ProxyTabPlayer)player).sendPluginMessage(new ExpansionPlaceholder(key, value));
   }

   @Override
   public void unregisterExpansion() {
   }

   public void resendAllValues(@NotNull ProxyTabPlayer player) {
      for (Entry<String, String> entry : player.expansionValues.entrySet()) {
         player.sendPluginMessage(new ExpansionPlaceholder(entry.getKey(), entry.getValue()));
      }
   }
}
