package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

public class LoadRequest extends ProxyMessage {
   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      proxySupport.sendMessage(new Load(TAB.getInstance().getOnlinePlayers()));
      TAB.getInstance().getFeatureManager().onProxyLoadRequest();
   }

   @Generated
   @Override
   public String toString() {
      return "LoadRequest()";
   }
}
