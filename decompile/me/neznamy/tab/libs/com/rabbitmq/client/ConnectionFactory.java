package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ConnectionParams;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.CredentialsProvider;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.CredentialsRefreshService;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.DefaultCredentialsProvider;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.DefaultExceptionHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ErrorOnWriteListener;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.FrameHandlerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.SocketFrameHandlerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.nio.NioParams;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.nio.SocketChannelFrameHandlerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.AutorecoveringConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.RecoveredQueueNameSupplier;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.RetryHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.TopologyRecoveryFilter;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionFactory implements Cloneable {
   private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFactory.class);
   private static final int MAX_UNSIGNED_SHORT = 65535;
   public static final String DEFAULT_USER = "guest";
   public static final String DEFAULT_PASS = "guest";
   public static final String DEFAULT_VHOST = "/";
   public static final int DEFAULT_CHANNEL_MAX = 2047;
   public static final int DEFAULT_FRAME_MAX = 0;
   public static final int DEFAULT_HEARTBEAT = 60;
   public static final String DEFAULT_HOST = "localhost";
   public static final int USE_DEFAULT_PORT = -1;
   public static final int DEFAULT_AMQP_PORT = 5672;
   public static final int DEFAULT_AMQP_OVER_SSL_PORT = 5671;
   public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
   public static final int DEFAULT_HANDSHAKE_TIMEOUT = 10000;
   public static final int DEFAULT_SHUTDOWN_TIMEOUT = 10000;
   public static final int DEFAULT_CHANNEL_RPC_TIMEOUT = (int)TimeUnit.MINUTES.toMillis(10L);
   public static final long DEFAULT_NETWORK_RECOVERY_INTERVAL = 5000L;
   public static final int DEFAULT_WORK_POOL_TIMEOUT = -1;
   private static final String PREFERRED_TLS_PROTOCOL = "TLSv1.2";
   private static final String FALLBACK_TLS_PROTOCOL = "TLSv1";
   private String virtualHost = "/";
   private String host = "localhost";
   private int port = -1;
   private int requestedChannelMax = 2047;
   private int requestedFrameMax = 0;
   private int requestedHeartbeat = 60;
   private int connectionTimeout = 60000;
   private int handshakeTimeout = 10000;
   private int shutdownTimeout = 10000;
   private Map<String, Object> _clientProperties = AMQConnection.defaultClientProperties();
   private SocketFactory socketFactory = null;
   private SaslConfig saslConfig = DefaultSaslConfig.PLAIN;
   private ExecutorService sharedExecutor;
   private ThreadFactory threadFactory = Executors.defaultThreadFactory();
   private ExecutorService shutdownExecutor;
   private ScheduledExecutorService heartbeatExecutor;
   private SocketConfigurator socketConf = SocketConfigurators.defaultConfigurator();
   private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();
   private CredentialsProvider credentialsProvider = new DefaultCredentialsProvider("guest", "guest");
   private boolean automaticRecovery = true;
   private boolean topologyRecovery = true;
   private ExecutorService topologyRecoveryExecutor;
   private long networkRecoveryInterval = 5000L;
   private RecoveryDelayHandler recoveryDelayHandler;
   private MetricsCollector metricsCollector;
   private ObservationCollector observationCollector = ObservationCollector.NO_OP;
   private boolean nio = false;
   private FrameHandlerFactory frameHandlerFactory;
   private NioParams nioParams = new NioParams();
   private SslContextFactory sslContextFactory;
   private int channelRpcTimeout = DEFAULT_CHANNEL_RPC_TIMEOUT;
   private boolean channelShouldCheckRpcResponseType = false;
   private ErrorOnWriteListener errorOnWriteListener;
   private int workPoolTimeout = -1;
   private TopologyRecoveryFilter topologyRecoveryFilter;
   private Predicate<ShutdownSignalException> connectionRecoveryTriggeringCondition;
   private RetryHandler topologyRecoveryRetryHandler;
   private RecoveredQueueNameSupplier recoveredQueueNameSupplier;
   private TrafficListener trafficListener = TrafficListener.NO_OP;
   private CredentialsRefreshService credentialsRefreshService;
   private int maxInboundMessageBodySize = 67108864;
   private static final Map<String, BiConsumer<String, ConnectionFactory>> URI_QUERY_PARAMETER_HANDLERS = new HashMap<String, BiConsumer<String, ConnectionFactory>>() {
      {
         this.put("heartbeat", (value, cf) -> {
            try {
               int heartbeatInt = Integer.parseInt(value);
               cf.setRequestedHeartbeat(heartbeatInt);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Requested heartbeat must an integer");
            }
         });
         this.put("connection_timeout", (value, cf) -> {
            try {
               int connectionTimeoutInt = Integer.parseInt(value);
               cf.setConnectionTimeout(connectionTimeoutInt);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("TCP connection timeout must an integer");
            }
         });
         this.put("channel_max", (value, cf) -> {
            try {
               int channelMaxInt = Integer.parseInt(value);
               cf.setRequestedChannelMax(channelMaxInt);
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Requested channel max must an integer");
            }
         });
      }
   };

   public String getHost() {
      return this.host;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public static int portOrDefault(int port, boolean ssl) {
      if (port != -1) {
         return port;
      } else {
         return ssl ? 5671 : 5672;
      }
   }

   public int getPort() {
      return portOrDefault(this.port, this.isSSL());
   }

   public void setPort(int port) {
      this.port = port;
   }

   public String getUsername() {
      return this.credentialsProvider.getUsername();
   }

   public void setUsername(String username) {
      this.credentialsProvider = new DefaultCredentialsProvider(username, this.credentialsProvider.getPassword());
   }

   public String getPassword() {
      return this.credentialsProvider.getPassword();
   }

   public void setPassword(String password) {
      this.credentialsProvider = new DefaultCredentialsProvider(this.credentialsProvider.getUsername(), password);
   }

   public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
      this.credentialsProvider = credentialsProvider;
   }

   public String getVirtualHost() {
      return this.virtualHost;
   }

   public void setVirtualHost(String virtualHost) {
      this.virtualHost = virtualHost;
   }

   public void setUri(URI uri) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
      if (!"amqp".equals(uri.getScheme().toLowerCase())) {
         if (!"amqps".equals(uri.getScheme().toLowerCase())) {
            throw new IllegalArgumentException("Wrong scheme in AMQP URI: " + uri.getScheme());
         }

         this.setPort(5671);
         if (this.sslContextFactory == null) {
            this.useSslProtocol();
         }
      }

      String host = uri.getHost();
      if (host != null) {
         this.setHost(host);
      }

      int port = uri.getPort();
      if (port != -1) {
         this.setPort(port);
      }

      String userInfo = uri.getRawUserInfo();
      if (userInfo != null) {
         String[] userPass = userInfo.split(":");
         if (userPass.length > 2) {
            throw new IllegalArgumentException("Bad user info in AMQP URI: " + userInfo);
         }

         this.setUsername(uriDecode(userPass[0]));
         if (userPass.length == 2) {
            this.setPassword(uriDecode(userPass[1]));
         }
      }

      String path = uri.getRawPath();
      if (path != null && path.length() > 0) {
         if (path.indexOf(47, 1) != -1) {
            throw new IllegalArgumentException("Multiple segments in path of AMQP URI: " + path);
         }

         this.setVirtualHost(uriDecode(uri.getPath().substring(1)));
      }

      String rawQuery = uri.getRawQuery();
      if (rawQuery != null && rawQuery.length() > 0) {
         this.setQuery(rawQuery);
      }
   }

   public void setUri(String uriString) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
      this.setUri(new URI(uriString));
   }

   private static String uriDecode(String s) {
      try {
         return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void setQuery(String rawQuery) {
      Map<String, String> parameters = new HashMap<>();

      try {
         for (String param : rawQuery.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "US-ASCII");
            String value = null;
            if (pair.length > 1) {
               value = URLDecoder.decode(pair[1], "US-ASCII");
            }

            parameters.put(key, value);
         }
      } catch (IOException e) {
         throw new IllegalArgumentException("Cannot parse the query parameters", e);
      }

      for (Entry<String, String> entry : parameters.entrySet()) {
         BiConsumer<String, ConnectionFactory> handler = URI_QUERY_PARAMETER_HANDLERS.get(entry.getKey());
         if (handler != null) {
            handler.accept(entry.getValue(), this);
         } else {
            this.processUriQueryParameter(entry.getKey(), entry.getValue());
         }
      }
   }

   protected void processUriQueryParameter(String key, String value) {
   }

   public int getRequestedChannelMax() {
      return this.requestedChannelMax;
   }

   public void setRequestedChannelMax(int requestedChannelMax) {
      this.requestedChannelMax = ensureUnsignedShort(requestedChannelMax);
      if (this.requestedChannelMax != requestedChannelMax) {
         LOGGER.warn(
            "Requested channel max must be between 0 and {}, value has been set to {} instead of {}",
            new Object[]{65535, this.requestedChannelMax, requestedChannelMax}
         );
      }
   }

   public int getRequestedFrameMax() {
      return this.requestedFrameMax;
   }

   public void setRequestedFrameMax(int requestedFrameMax) {
      this.requestedFrameMax = requestedFrameMax;
   }

   public int getRequestedHeartbeat() {
      return this.requestedHeartbeat;
   }

   public void setConnectionTimeout(int timeout) {
      if (timeout < 0) {
         throw new IllegalArgumentException("TCP connection timeout cannot be negative");
      }

      this.connectionTimeout = timeout;
   }

   public int getConnectionTimeout() {
      return this.connectionTimeout;
   }

   public int getHandshakeTimeout() {
      return this.handshakeTimeout;
   }

   public void setHandshakeTimeout(int timeout) {
      if (timeout < 0) {
         throw new IllegalArgumentException("handshake timeout cannot be negative");
      }

      this.handshakeTimeout = timeout;
   }

   public void setShutdownTimeout(int shutdownTimeout) {
      this.shutdownTimeout = shutdownTimeout;
   }

   public int getShutdownTimeout() {
      return this.shutdownTimeout;
   }

   public void setRequestedHeartbeat(int requestedHeartbeat) {
      this.requestedHeartbeat = ensureUnsignedShort(requestedHeartbeat);
      if (this.requestedHeartbeat != requestedHeartbeat) {
         LOGGER.warn(
            "Requested heartbeat must be between 0 and {}, value has been set to {} instead of {}",
            new Object[]{65535, this.requestedHeartbeat, requestedHeartbeat}
         );
      }
   }

   public Map<String, Object> getClientProperties() {
      return this._clientProperties;
   }

   public void setClientProperties(Map<String, Object> clientProperties) {
      this._clientProperties = clientProperties;
   }

   public SaslConfig getSaslConfig() {
      return this.saslConfig;
   }

   public void setSaslConfig(SaslConfig saslConfig) {
      this.saslConfig = saslConfig;
   }

   public SocketFactory getSocketFactory() {
      return this.socketFactory;
   }

   public void setSocketFactory(SocketFactory factory) {
      this.socketFactory = factory;
   }

   public SocketConfigurator getSocketConfigurator() {
      return this.socketConf;
   }

   public void setSocketConfigurator(SocketConfigurator socketConfigurator) {
      this.socketConf = socketConfigurator;
   }

   public void setSharedExecutor(ExecutorService executor) {
      this.sharedExecutor = executor;
   }

   public void setShutdownExecutor(ExecutorService executor) {
      this.shutdownExecutor = executor;
   }

   public void setHeartbeatExecutor(ScheduledExecutorService executor) {
      this.heartbeatExecutor = executor;
   }

   public ThreadFactory getThreadFactory() {
      return this.threadFactory;
   }

   public void setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
   }

   public ExceptionHandler getExceptionHandler() {
      return this.exceptionHandler;
   }

   public void setExceptionHandler(ExceptionHandler exceptionHandler) {
      if (exceptionHandler == null) {
         throw new IllegalArgumentException("exception handler cannot be null!");
      }

      this.exceptionHandler = exceptionHandler;
   }

   public boolean isSSL() {
      return this.getSocketFactory() instanceof SSLSocketFactory || this.sslContextFactory != null;
   }

   public void useSslProtocol() throws NoSuchAlgorithmException, KeyManagementException {
      this.useSslProtocol(computeDefaultTlsProtocol(SSLContext.getDefault().getSupportedSSLParameters().getProtocols()));
   }

   public void useSslProtocol(String protocol) throws NoSuchAlgorithmException, KeyManagementException {
      this.useSslProtocol(protocol, new TrustEverythingTrustManager());
   }

   public void useSslProtocol(String protocol, TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
      SSLContext c = SSLContext.getInstance(protocol);
      c.init(null, new TrustManager[]{trustManager}, null);
      this.useSslProtocol(c);
   }

   public void useSslProtocol(SSLContext context) {
      this.sslContextFactory = name -> context;
      this.setSocketFactory(context.getSocketFactory());
   }

   public void enableHostnameVerification() {
      this.enableHostnameVerificationForNio();
      this.enableHostnameVerificationForBlockingIo();
   }

   protected void enableHostnameVerificationForNio() {
      if (this.nioParams == null) {
         this.nioParams = new NioParams();
      }

      this.nioParams = this.nioParams.enableHostnameVerification();
   }

   protected void enableHostnameVerificationForBlockingIo() {
      if (this.socketConf == null) {
         this.socketConf = SocketConfigurators.builder().defaultConfigurator().enableHostnameVerification().build();
      } else {
         this.socketConf = this.socketConf.andThen(SocketConfigurators.enableHostnameVerification());
      }
   }

   public static String computeDefaultTlsProtocol(String[] supportedProtocols) {
      if (supportedProtocols != null) {
         for (String supportedProtocol : supportedProtocols) {
            if ("TLSv1.2".equalsIgnoreCase(supportedProtocol)) {
               return supportedProtocol;
            }
         }
      }

      return "TLSv1";
   }

   public boolean isAutomaticRecoveryEnabled() {
      return this.automaticRecovery;
   }

   public void setAutomaticRecoveryEnabled(boolean automaticRecovery) {
      this.automaticRecovery = automaticRecovery;
   }

   public boolean isTopologyRecoveryEnabled() {
      return this.topologyRecovery;
   }

   public void setTopologyRecoveryEnabled(boolean topologyRecovery) {
      this.topologyRecovery = topologyRecovery;
   }

   public ExecutorService getTopologyRecoveryExecutor() {
      return this.topologyRecoveryExecutor;
   }

   public void setTopologyRecoveryExecutor(ExecutorService topologyRecoveryExecutor) {
      this.topologyRecoveryExecutor = topologyRecoveryExecutor;
   }

   public void setMetricsCollector(MetricsCollector metricsCollector) {
      this.metricsCollector = metricsCollector;
   }

   public MetricsCollector getMetricsCollector() {
      return this.metricsCollector;
   }

   public void setObservationCollector(ObservationCollector observationCollector) {
      this.observationCollector = observationCollector;
   }

   public void setCredentialsRefreshService(CredentialsRefreshService credentialsRefreshService) {
      this.credentialsRefreshService = credentialsRefreshService;
   }

   protected synchronized FrameHandlerFactory createFrameHandlerFactory() throws IOException {
      if (this.nio) {
         if (this.frameHandlerFactory == null) {
            if (this.nioParams.getNioExecutor() == null && this.nioParams.getThreadFactory() == null) {
               this.nioParams.setThreadFactory(this.getThreadFactory());
            }

            this.frameHandlerFactory = new SocketChannelFrameHandlerFactory(
               this.connectionTimeout, this.nioParams, this.isSSL(), this.sslContextFactory, this.maxInboundMessageBodySize
            );
         }

         return this.frameHandlerFactory;
      } else {
         return new SocketFrameHandlerFactory(
            this.connectionTimeout,
            this.socketFactory,
            this.socketConf,
            this.isSSL(),
            this.shutdownExecutor,
            this.sslContextFactory,
            this.maxInboundMessageBodySize
         );
      }
   }

   public Connection newConnection(Address[] addrs) throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, Arrays.asList(addrs), null);
   }

   public Connection newConnection(AddressResolver addressResolver) throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, addressResolver, null);
   }

   public Connection newConnection(Address[] addrs, String clientProvidedName) throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, Arrays.asList(addrs), clientProvidedName);
   }

   public Connection newConnection(List<Address> addrs) throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, addrs, null);
   }

   public Connection newConnection(List<Address> addrs, String clientProvidedName) throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, addrs, clientProvidedName);
   }

   public Connection newConnection(ExecutorService executor, Address[] addrs) throws IOException, TimeoutException {
      return this.newConnection(executor, Arrays.asList(addrs), null);
   }

   public Connection newConnection(ExecutorService executor, Address[] addrs, String clientProvidedName) throws IOException, TimeoutException {
      return this.newConnection(executor, Arrays.asList(addrs), clientProvidedName);
   }

   public Connection newConnection(ExecutorService executor, List<Address> addrs) throws IOException, TimeoutException {
      return this.newConnection(executor, addrs, null);
   }

   public Connection newConnection(ExecutorService executor, AddressResolver addressResolver) throws IOException, TimeoutException {
      return this.newConnection(executor, addressResolver, null);
   }

   public Connection newConnection(ExecutorService executor, List<Address> addrs, String clientProvidedName) throws IOException, TimeoutException {
      return this.newConnection(executor, this.createAddressResolver(addrs), clientProvidedName);
   }

   public Connection newConnection(ExecutorService executor, AddressResolver addressResolver, String clientProvidedName) throws IOException, TimeoutException {
      if (this.metricsCollector == null) {
         this.metricsCollector = new NoOpMetricsCollector();
      }

      FrameHandlerFactory fhFactory = this.createFrameHandlerFactory();
      ConnectionParams params = this.params(executor);
      if (clientProvidedName != null) {
         Map<String, Object> properties = new HashMap<>(params.getClientProperties());
         properties.put("connection_name", clientProvidedName);
         params.setClientProperties(properties);
      }

      if (this.isAutomaticRecoveryEnabled()) {
         AutorecoveringConnection conn = new AutorecoveringConnection(params, fhFactory, addressResolver, this.metricsCollector, this.observationCollector);
         conn.init();
         return conn;
      }

      List<Address> addrs = addressResolver.getAddresses();
      Exception lastException = null;

      for (Address addr : addrs) {
         try {
            FrameHandler handler = fhFactory.create(addr, clientProvidedName);
            AMQConnection conn = this.createConnection(params, handler, this.metricsCollector);
            conn.start();
            this.metricsCollector.newConnection(conn);
            return conn;
         } catch (IOException e) {
            lastException = e;
         } catch (TimeoutException te) {
            lastException = te;
         }
      }

      if (lastException != null) {
         if (lastException instanceof IOException) {
            throw (IOException)lastException;
         }

         if (lastException instanceof TimeoutException) {
            throw (TimeoutException)lastException;
         }
      }

      throw new IOException("failed to connect");
   }

   public ConnectionParams params(ExecutorService consumerWorkServiceExecutor) {
      ConnectionParams result = new ConnectionParams();
      result.setCredentialsProvider(this.credentialsProvider);
      result.setConsumerWorkServiceExecutor(consumerWorkServiceExecutor);
      result.setVirtualHost(this.virtualHost);
      result.setClientProperties(this.getClientProperties());
      result.setRequestedFrameMax(this.requestedFrameMax);
      result.setRequestedChannelMax(this.requestedChannelMax);
      result.setShutdownTimeout(this.shutdownTimeout);
      result.setSaslConfig(this.saslConfig);
      result.setNetworkRecoveryInterval(this.networkRecoveryInterval);
      result.setRecoveryDelayHandler(this.recoveryDelayHandler);
      result.setTopologyRecovery(this.topologyRecovery);
      result.setTopologyRecoveryExecutor(this.topologyRecoveryExecutor);
      result.setExceptionHandler(this.exceptionHandler);
      result.setThreadFactory(this.threadFactory);
      result.setHandshakeTimeout(this.handshakeTimeout);
      result.setRequestedHeartbeat(this.requestedHeartbeat);
      result.setShutdownExecutor(this.shutdownExecutor);
      result.setHeartbeatExecutor(this.heartbeatExecutor);
      result.setChannelRpcTimeout(this.channelRpcTimeout);
      result.setChannelShouldCheckRpcResponseType(this.channelShouldCheckRpcResponseType);
      result.setWorkPoolTimeout(this.workPoolTimeout);
      result.setErrorOnWriteListener(this.errorOnWriteListener);
      result.setTopologyRecoveryFilter(this.topologyRecoveryFilter);
      result.setConnectionRecoveryTriggeringCondition(this.connectionRecoveryTriggeringCondition);
      result.setTopologyRecoveryRetryHandler(this.topologyRecoveryRetryHandler);
      result.setRecoveredQueueNameSupplier(this.recoveredQueueNameSupplier);
      result.setTrafficListener(this.trafficListener);
      result.setCredentialsRefreshService(this.credentialsRefreshService);
      result.setMaxInboundMessageBodySize(this.maxInboundMessageBodySize);
      return result;
   }

   protected AMQConnection createConnection(ConnectionParams params, FrameHandler frameHandler, MetricsCollector metricsCollector) {
      return new AMQConnection(params, frameHandler, metricsCollector, this.observationCollector);
   }

   public Connection newConnection() throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, Collections.singletonList(new Address(this.getHost(), this.getPort())));
   }

   public Connection newConnection(String connectionName) throws IOException, TimeoutException {
      return this.newConnection(this.sharedExecutor, Collections.singletonList(new Address(this.getHost(), this.getPort())), connectionName);
   }

   public Connection newConnection(ExecutorService executor) throws IOException, TimeoutException {
      return this.newConnection(executor, Collections.singletonList(new Address(this.getHost(), this.getPort())));
   }

   public Connection newConnection(ExecutorService executor, String connectionName) throws IOException, TimeoutException {
      return this.newConnection(executor, Collections.singletonList(new Address(this.getHost(), this.getPort())), connectionName);
   }

   protected AddressResolver createAddressResolver(List<Address> addresses) {
      if (addresses != null && !addresses.isEmpty()) {
         return addresses.size() > 1 ? new ListAddressResolver(addresses) : new DnsRecordIpAddressResolver(addresses.get(0), this.isSSL());
      } else {
         throw new IllegalArgumentException("Please provide at least one address to connect to");
      }
   }

   public ConnectionFactory clone() {
      try {
         return (ConnectionFactory)super.clone();
      } catch (CloneNotSupportedException e) {
         throw new RuntimeException(e);
      }
   }

   public ConnectionFactory load(String propertyFileLocation) throws IOException {
      ConnectionFactoryConfigurator.load(this, propertyFileLocation);
      return this;
   }

   public ConnectionFactory load(String propertyFileLocation, String prefix) throws IOException {
      ConnectionFactoryConfigurator.load(this, propertyFileLocation, prefix);
      return this;
   }

   public ConnectionFactory load(Properties properties) {
      ConnectionFactoryConfigurator.load(this, properties);
      return this;
   }

   public ConnectionFactory load(Properties properties, String prefix) {
      ConnectionFactoryConfigurator.load(this, (Map<String, String>)properties, prefix);
      return this;
   }

   public ConnectionFactory load(Map<String, String> properties) {
      ConnectionFactoryConfigurator.load(this, properties);
      return this;
   }

   public ConnectionFactory load(Map<String, String> properties, String prefix) {
      ConnectionFactoryConfigurator.load(this, properties, prefix);
      return this;
   }

   public long getNetworkRecoveryInterval() {
      return this.networkRecoveryInterval;
   }

   public void setNetworkRecoveryInterval(int networkRecoveryInterval) {
      this.networkRecoveryInterval = networkRecoveryInterval;
   }

   public void setNetworkRecoveryInterval(long networkRecoveryInterval) {
      this.networkRecoveryInterval = networkRecoveryInterval;
   }

   public RecoveryDelayHandler getRecoveryDelayHandler() {
      return this.recoveryDelayHandler;
   }

   public void setRecoveryDelayHandler(RecoveryDelayHandler recoveryDelayHandler) {
      this.recoveryDelayHandler = recoveryDelayHandler;
   }

   public void setNioParams(NioParams nioParams) {
      this.nioParams = nioParams;
   }

   public NioParams getNioParams() {
      return this.nioParams;
   }

   public void useNio() {
      this.nio = true;
   }

   public void useBlockingIo() {
      this.nio = false;
   }

   public void setChannelRpcTimeout(int channelRpcTimeout) {
      if (channelRpcTimeout < 0) {
         throw new IllegalArgumentException("Timeout cannot be less than 0");
      }

      this.channelRpcTimeout = channelRpcTimeout;
   }

   public int getChannelRpcTimeout() {
      return this.channelRpcTimeout;
   }

   public void setMaxInboundMessageBodySize(int maxInboundMessageBodySize) {
      if (maxInboundMessageBodySize <= 0) {
         throw new IllegalArgumentException("Max inbound message body size must be greater than 0: " + maxInboundMessageBodySize);
      }

      this.maxInboundMessageBodySize = maxInboundMessageBodySize;
   }

   public void setSslContextFactory(SslContextFactory sslContextFactory) {
      this.sslContextFactory = sslContextFactory;
   }

   public void setChannelShouldCheckRpcResponseType(boolean channelShouldCheckRpcResponseType) {
      this.channelShouldCheckRpcResponseType = channelShouldCheckRpcResponseType;
   }

   public boolean isChannelShouldCheckRpcResponseType() {
      return this.channelShouldCheckRpcResponseType;
   }

   public void setWorkPoolTimeout(int workPoolTimeout) {
      this.workPoolTimeout = workPoolTimeout;
   }

   public int getWorkPoolTimeout() {
      return this.workPoolTimeout;
   }

   public void setErrorOnWriteListener(ErrorOnWriteListener errorOnWriteListener) {
      this.errorOnWriteListener = errorOnWriteListener;
   }

   public void setTopologyRecoveryFilter(TopologyRecoveryFilter topologyRecoveryFilter) {
      this.topologyRecoveryFilter = topologyRecoveryFilter;
   }

   public void setConnectionRecoveryTriggeringCondition(Predicate<ShutdownSignalException> connectionRecoveryTriggeringCondition) {
      this.connectionRecoveryTriggeringCondition = connectionRecoveryTriggeringCondition;
   }

   public void setTopologyRecoveryRetryHandler(RetryHandler topologyRecoveryRetryHandler) {
      this.topologyRecoveryRetryHandler = topologyRecoveryRetryHandler;
   }

   public void setRecoveredQueueNameSupplier(RecoveredQueueNameSupplier recoveredQueueNameSupplier) {
      this.recoveredQueueNameSupplier = recoveredQueueNameSupplier;
   }

   public void setTrafficListener(TrafficListener trafficListener) {
      this.trafficListener = trafficListener;
   }

   public static int ensureUnsignedShort(int value) {
      if (value < 0) {
         return 0;
      } else {
         return value > 65535 ? 65535 : value;
      }
   }
}
