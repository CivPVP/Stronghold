package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.function.Supplier;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

public final class DefaultJedisClientConfig implements JedisClientConfig {
   private final RedisProtocol redisProtocol;
   private final int connectionTimeoutMillis;
   private final int socketTimeoutMillis;
   private final int blockingSocketTimeoutMillis;
   private volatile Supplier<RedisCredentials> credentialsProvider;
   private final int database;
   private final String clientName;
   private final boolean ssl;
   private final SSLSocketFactory sslSocketFactory;
   private final SSLParameters sslParameters;
   private final HostnameVerifier hostnameVerifier;
   private final HostAndPortMapper hostAndPortMapper;
   private final ClientSetInfoConfig clientSetInfoConfig;
   private final boolean readOnlyForRedisClusterReplicas;

   private DefaultJedisClientConfig(
      RedisProtocol protocol,
      int connectionTimeoutMillis,
      int soTimeoutMillis,
      int blockingSocketTimeoutMillis,
      Supplier<RedisCredentials> credentialsProvider,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier,
      HostAndPortMapper hostAndPortMapper,
      ClientSetInfoConfig clientSetInfoConfig,
      boolean readOnlyForRedisClusterReplicas
   ) {
      this.redisProtocol = protocol;
      this.connectionTimeoutMillis = connectionTimeoutMillis;
      this.socketTimeoutMillis = soTimeoutMillis;
      this.blockingSocketTimeoutMillis = blockingSocketTimeoutMillis;
      this.credentialsProvider = credentialsProvider;
      this.database = database;
      this.clientName = clientName;
      this.ssl = ssl;
      this.sslSocketFactory = sslSocketFactory;
      this.sslParameters = sslParameters;
      this.hostnameVerifier = hostnameVerifier;
      this.hostAndPortMapper = hostAndPortMapper;
      this.clientSetInfoConfig = clientSetInfoConfig;
      this.readOnlyForRedisClusterReplicas = readOnlyForRedisClusterReplicas;
   }

   @Override
   public RedisProtocol getRedisProtocol() {
      return this.redisProtocol;
   }

   @Override
   public int getConnectionTimeoutMillis() {
      return this.connectionTimeoutMillis;
   }

   @Override
   public int getSocketTimeoutMillis() {
      return this.socketTimeoutMillis;
   }

   @Override
   public int getBlockingSocketTimeoutMillis() {
      return this.blockingSocketTimeoutMillis;
   }

   @Override
   public String getUser() {
      return this.credentialsProvider.get().getUser();
   }

   @Override
   public String getPassword() {
      char[] password = this.credentialsProvider.get().getPassword();
      return password == null ? null : new String(password);
   }

   @Override
   public Supplier<RedisCredentials> getCredentialsProvider() {
      return this.credentialsProvider;
   }

   @Override
   public int getDatabase() {
      return this.database;
   }

   @Override
   public String getClientName() {
      return this.clientName;
   }

   @Override
   public boolean isSsl() {
      return this.ssl;
   }

   @Override
   public SSLSocketFactory getSslSocketFactory() {
      return this.sslSocketFactory;
   }

   @Override
   public SSLParameters getSslParameters() {
      return this.sslParameters;
   }

   @Override
   public HostnameVerifier getHostnameVerifier() {
      return this.hostnameVerifier;
   }

   @Override
   public HostAndPortMapper getHostAndPortMapper() {
      return this.hostAndPortMapper;
   }

   @Override
   public ClientSetInfoConfig getClientSetInfoConfig() {
      return this.clientSetInfoConfig;
   }

   @Override
   public boolean isReadOnlyForRedisClusterReplicas() {
      return this.readOnlyForRedisClusterReplicas;
   }

   public static DefaultJedisClientConfig.Builder builder() {
      return new DefaultJedisClientConfig.Builder();
   }

   public static DefaultJedisClientConfig create(
      int connectionTimeoutMillis,
      int soTimeoutMillis,
      int blockingSocketTimeoutMillis,
      String user,
      String password,
      int database,
      String clientName,
      boolean ssl,
      SSLSocketFactory sslSocketFactory,
      SSLParameters sslParameters,
      HostnameVerifier hostnameVerifier,
      HostAndPortMapper hostAndPortMapper
   ) {
      return new DefaultJedisClientConfig(
         null,
         connectionTimeoutMillis,
         soTimeoutMillis,
         blockingSocketTimeoutMillis,
         new DefaultRedisCredentialsProvider(new DefaultRedisCredentials(user, password)),
         database,
         clientName,
         ssl,
         sslSocketFactory,
         sslParameters,
         hostnameVerifier,
         hostAndPortMapper,
         null,
         false
      );
   }

   public static DefaultJedisClientConfig copyConfig(JedisClientConfig copy) {
      return new DefaultJedisClientConfig(
         copy.getRedisProtocol(),
         copy.getConnectionTimeoutMillis(),
         copy.getSocketTimeoutMillis(),
         copy.getBlockingSocketTimeoutMillis(),
         copy.getCredentialsProvider(),
         copy.getDatabase(),
         copy.getClientName(),
         copy.isSsl(),
         copy.getSslSocketFactory(),
         copy.getSslParameters(),
         copy.getHostnameVerifier(),
         copy.getHostAndPortMapper(),
         copy.getClientSetInfoConfig(),
         copy.isReadOnlyForRedisClusterReplicas()
      );
   }

   public static class Builder {
      private RedisProtocol redisProtocol = null;
      private int connectionTimeoutMillis = 2000;
      private int socketTimeoutMillis = 2000;
      private int blockingSocketTimeoutMillis = 0;
      private String user = null;
      private String password = null;
      private Supplier<RedisCredentials> credentialsProvider;
      private int database = 0;
      private String clientName = null;
      private boolean ssl = false;
      private SSLSocketFactory sslSocketFactory = null;
      private SSLParameters sslParameters = null;
      private HostnameVerifier hostnameVerifier = null;
      private HostAndPortMapper hostAndPortMapper = null;
      private ClientSetInfoConfig clientSetInfoConfig = ClientSetInfoConfig.DEFAULT;
      private boolean readOnlyForRedisClusterReplicas = false;

      private Builder() {
      }

      public DefaultJedisClientConfig build() {
         if (this.credentialsProvider == null) {
            this.credentialsProvider = new DefaultRedisCredentialsProvider(new DefaultRedisCredentials(this.user, this.password));
         }

         return new DefaultJedisClientConfig(
            this.redisProtocol,
            this.connectionTimeoutMillis,
            this.socketTimeoutMillis,
            this.blockingSocketTimeoutMillis,
            this.credentialsProvider,
            this.database,
            this.clientName,
            this.ssl,
            this.sslSocketFactory,
            this.sslParameters,
            this.hostnameVerifier,
            this.hostAndPortMapper,
            this.clientSetInfoConfig,
            this.readOnlyForRedisClusterReplicas
         );
      }

      public DefaultJedisClientConfig.Builder resp3() {
         return this.protocol(RedisProtocol.RESP3);
      }

      public DefaultJedisClientConfig.Builder protocol(RedisProtocol protocol) {
         this.redisProtocol = protocol;
         return this;
      }

      public DefaultJedisClientConfig.Builder timeoutMillis(int timeoutMillis) {
         this.connectionTimeoutMillis = timeoutMillis;
         this.socketTimeoutMillis = timeoutMillis;
         return this;
      }

      public DefaultJedisClientConfig.Builder connectionTimeoutMillis(int connectionTimeoutMillis) {
         this.connectionTimeoutMillis = connectionTimeoutMillis;
         return this;
      }

      public DefaultJedisClientConfig.Builder socketTimeoutMillis(int socketTimeoutMillis) {
         this.socketTimeoutMillis = socketTimeoutMillis;
         return this;
      }

      public DefaultJedisClientConfig.Builder blockingSocketTimeoutMillis(int blockingSocketTimeoutMillis) {
         this.blockingSocketTimeoutMillis = blockingSocketTimeoutMillis;
         return this;
      }

      public DefaultJedisClientConfig.Builder user(String user) {
         this.user = user;
         return this;
      }

      public DefaultJedisClientConfig.Builder password(String password) {
         this.password = password;
         return this;
      }

      public DefaultJedisClientConfig.Builder credentials(RedisCredentials credentials) {
         this.credentialsProvider = new DefaultRedisCredentialsProvider(credentials);
         return this;
      }

      public DefaultJedisClientConfig.Builder credentialsProvider(Supplier<RedisCredentials> credentials) {
         this.credentialsProvider = credentials;
         return this;
      }

      public DefaultJedisClientConfig.Builder database(int database) {
         this.database = database;
         return this;
      }

      public DefaultJedisClientConfig.Builder clientName(String clientName) {
         this.clientName = clientName;
         return this;
      }

      public DefaultJedisClientConfig.Builder ssl(boolean ssl) {
         this.ssl = ssl;
         return this;
      }

      public DefaultJedisClientConfig.Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
         this.sslSocketFactory = sslSocketFactory;
         return this;
      }

      public DefaultJedisClientConfig.Builder sslParameters(SSLParameters sslParameters) {
         this.sslParameters = sslParameters;
         return this;
      }

      public DefaultJedisClientConfig.Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
         this.hostnameVerifier = hostnameVerifier;
         return this;
      }

      public DefaultJedisClientConfig.Builder hostAndPortMapper(HostAndPortMapper hostAndPortMapper) {
         this.hostAndPortMapper = hostAndPortMapper;
         return this;
      }

      public DefaultJedisClientConfig.Builder clientSetInfoConfig(ClientSetInfoConfig setInfoConfig) {
         this.clientSetInfoConfig = setInfoConfig;
         return this;
      }

      public DefaultJedisClientConfig.Builder readOnlyForRedisClusterReplicas() {
         this.readOnlyForRedisClusterReplicas = true;
         return this;
      }
   }
}
