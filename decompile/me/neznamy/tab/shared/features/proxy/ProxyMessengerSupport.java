package me.neznamy.tab.shared.features.proxy;

import java.util.function.Supplier;
import lombok.Generated;
import me.neznamy.tab.libs.com.saicone.delivery4j.AbstractMessenger;
import me.neznamy.tab.libs.com.saicone.delivery4j.Broker;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProxyMessengerSupport extends ProxySupport {
   @NotNull
   private final String messengerName;
   @NotNull
   private final Supplier<Broker> brokerSupplier;
   @Nullable
   private AbstractMessenger messenger;

   @Override
   public void sendMessage(@NotNull String message) {
      if (this.messenger != null && this.messenger.isEnabled()) {
         this.messenger.send("TAB-2", message);
      }
   }

   @Override
   public void register() {
      try {
         final Broker broker = this.brokerSupplier.get();
         this.messenger = new AbstractMessenger() {
            @NotNull
            @Override
            protected Broker loadBroker() {
               return broker;
            }
         };
         this.messenger.subscribe("TAB-2").consume((channel, lines) -> this.processMessage(lines[0])).cache(true);
         this.messenger.start();
         TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Successfully connected to " + this.messengerName, TabTextColor.GREEN));
      } catch (Exception e) {
         TAB.getInstance()
            .getErrorManager()
            .criticalError("Failed to connect to " + this.messengerName + ": " + e.getClass().getName() + ": " + e.getMessage(), null);
      }
   }

   @Override
   public void unregister() {
      if (this.messenger != null) {
         this.messenger.close();
         this.messenger.clear();
      }
   }

   @Generated
   public ProxyMessengerSupport(@NotNull String messengerName, @NotNull Supplier<Broker> brokerSupplier) {
      if (messengerName == null) {
         throw new NullPointerException("messengerName is marked non-null but is null");
      }

      if (brokerSupplier == null) {
         throw new NullPointerException("brokerSupplier is marked non-null but is null");
      }

      this.messengerName = messengerName;
      this.brokerSupplier = brokerSupplier;
   }
}
