package me.neznamy.tab.libs.com.rabbitmq.client;

import javax.net.ssl.SSLParameters;

public abstract class SslEngineConfigurators {
   public static final SslEngineConfigurator DEFAULT = sslEngine -> {};
   public static final SslEngineConfigurator ENABLE_HOSTNAME_VERIFICATION = sslEngine -> {
      SSLParameters sslParameters = SocketConfigurators.enableHostnameVerification(sslEngine.getSSLParameters());
      sslEngine.setSSLParameters(sslParameters);
   };

   public static SslEngineConfigurator defaultConfigurator() {
      return DEFAULT;
   }

   public static SslEngineConfigurator enableHostnameVerification() {
      return ENABLE_HOSTNAME_VERIFICATION;
   }

   public static SslEngineConfigurators.Builder builder() {
      return new SslEngineConfigurators.Builder();
   }

   public static class Builder {
      private SslEngineConfigurator configurator = channel -> {};

      public SslEngineConfigurators.Builder defaultConfigurator() {
         this.configurator = this.configurator.andThen(SslEngineConfigurators.DEFAULT);
         return this;
      }

      public SslEngineConfigurators.Builder enableHostnameVerification() {
         this.configurator = this.configurator.andThen(SslEngineConfigurators.ENABLE_HOSTNAME_VERIFICATION);
         return this;
      }

      public SslEngineConfigurators.Builder add(SslEngineConfigurator extraConfiguration) {
         this.configurator = this.configurator.andThen(extraConfiguration);
         return this;
      }

      public SslEngineConfigurator build() {
         return this.configurator;
      }
   }
}
