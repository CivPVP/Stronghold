package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.function.Supplier;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

public interface JedisClientConfig {
   default RedisProtocol getRedisProtocol() {
      return null;
   }

   default int getConnectionTimeoutMillis() {
      return 2000;
   }

   default int getSocketTimeoutMillis() {
      return 2000;
   }

   default int getBlockingSocketTimeoutMillis() {
      return 0;
   }

   default String getUser() {
      return null;
   }

   default String getPassword() {
      return null;
   }

   default Supplier<RedisCredentials> getCredentialsProvider() {
      return new DefaultRedisCredentialsProvider(new DefaultRedisCredentials(this.getUser(), this.getPassword()));
   }

   default int getDatabase() {
      return 0;
   }

   default String getClientName() {
      return null;
   }

   default boolean isSsl() {
      return false;
   }

   default SSLSocketFactory getSslSocketFactory() {
      return null;
   }

   default SSLParameters getSslParameters() {
      return null;
   }

   default HostnameVerifier getHostnameVerifier() {
      return null;
   }

   default HostAndPortMapper getHostAndPortMapper() {
      return null;
   }

   default boolean isReadOnlyForRedisClusterReplicas() {
      return false;
   }

   default ClientSetInfoConfig getClientSetInfoConfig() {
      return ClientSetInfoConfig.DEFAULT;
   }
}
