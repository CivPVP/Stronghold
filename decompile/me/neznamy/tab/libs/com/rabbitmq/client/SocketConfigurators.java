package me.neznamy.tab.libs.com.rabbitmq.client;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

public abstract class SocketConfigurators {
   public static final SocketConfigurator DISABLE_NAGLE_ALGORITHM = socket -> socket.setTcpNoDelay(true);
   public static final SocketConfigurator DEFAULT = DISABLE_NAGLE_ALGORITHM;
   public static final SocketConfigurator ENABLE_HOSTNAME_VERIFICATION = socket -> {
      if (socket instanceof SSLSocket) {
         SSLSocket sslSocket = (SSLSocket)socket;
         SSLParameters sslParameters = enableHostnameVerification(sslSocket.getSSLParameters());
         sslSocket.setSSLParameters(sslParameters);
      }
   };

   static final SSLParameters enableHostnameVerification(SSLParameters sslParameters) {
      if (sslParameters == null) {
         sslParameters = new SSLParameters();
      }

      sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
      return sslParameters;
   }

   public static SocketConfigurator defaultConfigurator() {
      return DEFAULT;
   }

   public static SocketConfigurator disableNagleAlgorithm() {
      return DISABLE_NAGLE_ALGORITHM;
   }

   public static SocketConfigurator enableHostnameVerification() {
      return ENABLE_HOSTNAME_VERIFICATION;
   }

   public static SocketConfigurators.Builder builder() {
      return new SocketConfigurators.Builder();
   }

   public static class Builder {
      private SocketConfigurator configurator = socket -> {};

      public SocketConfigurators.Builder defaultConfigurator() {
         this.configurator = this.configurator.andThen(SocketConfigurators.DEFAULT);
         return this;
      }

      public SocketConfigurators.Builder disableNagleAlgorithm() {
         this.configurator = this.configurator.andThen(SocketConfigurators.DISABLE_NAGLE_ALGORITHM);
         return this;
      }

      public SocketConfigurators.Builder enableHostnameVerification() {
         this.configurator = this.configurator.andThen(SocketConfigurators.ENABLE_HOSTNAME_VERIFICATION);
         return this;
      }

      public SocketConfigurators.Builder add(SocketConfigurator extraConfiguration) {
         this.configurator = this.configurator.andThen(extraConfiguration);
         return this;
      }

      public SocketConfigurator build() {
         return this.configurator;
      }
   }
}
