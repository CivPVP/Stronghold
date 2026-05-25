package me.neznamy.tab.libs.com.saicone.delivery4j;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ChannelConsumer<T> {
   void accept(@NotNull String var1, @NotNull T var2) throws IOException;

   default ChannelConsumer<T> andThen(@NotNull ChannelConsumer<T> after) {
      return (channel, src) -> {
         this.accept(channel, src);
         after.accept(channel, src);
      };
   }
}
