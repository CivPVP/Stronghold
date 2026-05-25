package me.neznamy.tab.libs.com.rabbitmq.client;

public abstract class SocketChannelConfigurators {
   public static final SocketChannelConfigurator DISABLE_NAGLE_ALGORITHM = socketChannel -> SocketConfigurators.DISABLE_NAGLE_ALGORITHM
      .configure(socketChannel.socket());
   public static final SocketChannelConfigurator DEFAULT = DISABLE_NAGLE_ALGORITHM;

   public static SocketChannelConfigurator defaultConfigurator() {
      return DEFAULT;
   }

   public static SocketChannelConfigurator disableNagleAlgorithm() {
      return DISABLE_NAGLE_ALGORITHM;
   }

   public static SocketChannelConfigurators.Builder builder() {
      return new SocketChannelConfigurators.Builder();
   }

   public static class Builder {
      private SocketChannelConfigurator configurator = channel -> {};

      public SocketChannelConfigurators.Builder defaultConfigurator() {
         this.configurator = this.configurator.andThen(SocketChannelConfigurators.DEFAULT);
         return this;
      }

      public SocketChannelConfigurators.Builder disableNagleAlgorithm() {
         this.configurator = this.configurator.andThen(SocketChannelConfigurators.DISABLE_NAGLE_ALGORITHM);
         return this;
      }

      public SocketChannelConfigurators.Builder add(SocketChannelConfigurator extraConfiguration) {
         this.configurator = this.configurator.andThen(extraConfiguration);
         return this;
      }

      public SocketChannelConfigurator build() {
         return this.configurator;
      }
   }
}
