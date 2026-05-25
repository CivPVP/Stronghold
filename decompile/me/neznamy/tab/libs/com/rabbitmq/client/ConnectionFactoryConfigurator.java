package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AMQConnection;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.nio.NioParams;

public class ConnectionFactoryConfigurator {
   public static final String DEFAULT_PREFIX = "rabbitmq.";
   public static final String USERNAME = "username";
   public static final String PASSWORD = "password";
   public static final String VIRTUAL_HOST = "virtual.host";
   public static final String HOST = "host";
   public static final String PORT = "port";
   public static final String CONNECTION_CHANNEL_MAX = "connection.channel.max";
   public static final String CONNECTION_FRAME_MAX = "connection.frame.max";
   public static final String CONNECTION_HEARTBEAT = "connection.heartbeat";
   public static final String CONNECTION_TIMEOUT = "connection.timeout";
   public static final String HANDSHAKE_TIMEOUT = "handshake.timeout";
   public static final String SHUTDOWN_TIMEOUT = "shutdown.timeout";
   public static final String CLIENT_PROPERTIES_PREFIX = "client.properties.";
   public static final String CONNECTION_RECOVERY_ENABLED = "connection.recovery.enabled";
   public static final String TOPOLOGY_RECOVERY_ENABLED = "topology.recovery.enabled";
   public static final String CONNECTION_RECOVERY_INTERVAL = "connection.recovery.interval";
   public static final String CHANNEL_RPC_TIMEOUT = "channel.rpc.timeout";
   public static final String CHANNEL_SHOULD_CHECK_RPC_RESPONSE_TYPE = "channel.should.check.rpc.response.type";
   public static final String USE_NIO = "use.nio";
   public static final String NIO_READ_BYTE_BUFFER_SIZE = "nio.read.byte.buffer.size";
   public static final String NIO_WRITE_BYTE_BUFFER_SIZE = "nio.write.byte.buffer.size";
   public static final String NIO_NB_IO_THREADS = "nio.nb.io.threads";
   public static final String NIO_WRITE_ENQUEUING_TIMEOUT_IN_MS = "nio.write.enqueuing.timeout.in.ms";
   public static final String NIO_WRITE_QUEUE_CAPACITY = "nio.write.queue.capacity";
   public static final String SSL_ALGORITHM = "ssl.algorithm";
   public static final String SSL_ENABLED = "ssl.enabled";
   public static final String SSL_KEY_STORE = "ssl.key.store";
   public static final String SSL_KEY_STORE_PASSWORD = "ssl.key.store.password";
   public static final String SSL_KEY_STORE_TYPE = "ssl.key.store.type";
   public static final String SSL_KEY_STORE_ALGORITHM = "ssl.key.store.algorithm";
   public static final String SSL_TRUST_STORE = "ssl.trust.store";
   public static final String SSL_TRUST_STORE_PASSWORD = "ssl.trust.store.password";
   public static final String SSL_TRUST_STORE_TYPE = "ssl.trust.store.type";
   public static final String SSL_TRUST_STORE_ALGORITHM = "ssl.trust.store.algorithm";
   public static final String SSL_VALIDATE_SERVER_CERTIFICATE = "ssl.validate.server.certificate";
   public static final String SSL_VERIFY_HOSTNAME = "ssl.verify.hostname";
   private static final Map<String, List<String>> ALIASES = new ConcurrentHashMap<String, List<String>>() {
      {
         this.put("ssl.key.store", Arrays.asList("ssl.key-store"));
         this.put("ssl.key.store.password", Arrays.asList("ssl.key-store-password"));
         this.put("ssl.key.store.type", Arrays.asList("ssl.key-store-type"));
         this.put("ssl.key.store.algorithm", Arrays.asList("ssl.key-store-algorithm"));
         this.put("ssl.trust.store", Arrays.asList("ssl.trust-store"));
         this.put("ssl.trust.store.password", Arrays.asList("ssl.trust-store-password"));
         this.put("ssl.trust.store.type", Arrays.asList("ssl.trust-store-type"));
         this.put("ssl.trust.store.algorithm", Arrays.asList("ssl.trust-store-algorithm"));
         this.put("ssl.validate.server.certificate", Arrays.asList("ssl.validate-server-certificate"));
         this.put("ssl.verify.hostname", Arrays.asList("ssl.verify-hostname"));
      }
   };

   public static void load(ConnectionFactory cf, String propertyFileLocation, String prefix) throws IOException {
      if (propertyFileLocation != null && !propertyFileLocation.isEmpty()) {
         Properties properties = new Properties();

         try (InputStream in = loadResource(propertyFileLocation)) {
            properties.load(in);
         }

         load(cf, (Map<String, String>)properties, prefix);
      } else {
         throw new IllegalArgumentException("Property file argument cannot be null or empty");
      }
   }

   private static InputStream loadResource(String location) throws FileNotFoundException {
      return location.startsWith("classpath:")
         ? ConnectionFactoryConfigurator.class.getResourceAsStream(location.substring("classpath:".length()))
         : new FileInputStream(location);
   }

   public static void load(ConnectionFactory cf, Map<String, String> properties, String prefix) {
      prefix = prefix == null ? "" : prefix;
      String uri = properties.get(prefix + "uri");
      if (uri != null) {
         try {
            cf.setUri(uri);
         } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Error while setting AMQP URI: " + uri, e);
         } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Error while setting AMQP URI: " + uri, e);
         } catch (KeyManagementException e) {
            throw new IllegalArgumentException("Error while setting AMQP URI: " + uri, e);
         }
      }

      String username = lookUp("username", properties, prefix);
      if (username != null) {
         cf.setUsername(username);
      }

      String password = lookUp("password", properties, prefix);
      if (password != null) {
         cf.setPassword(password);
      }

      String vhost = lookUp("virtual.host", properties, prefix);
      if (vhost != null) {
         cf.setVirtualHost(vhost);
      }

      String host = lookUp("host", properties, prefix);
      if (host != null) {
         cf.setHost(host);
      }

      String port = lookUp("port", properties, prefix);
      if (port != null) {
         cf.setPort(Integer.valueOf(port));
      }

      String requestedChannelMax = lookUp("connection.channel.max", properties, prefix);
      if (requestedChannelMax != null) {
         cf.setRequestedChannelMax(Integer.valueOf(requestedChannelMax));
      }

      String requestedFrameMax = lookUp("connection.frame.max", properties, prefix);
      if (requestedFrameMax != null) {
         cf.setRequestedFrameMax(Integer.valueOf(requestedFrameMax));
      }

      String requestedHeartbeat = lookUp("connection.heartbeat", properties, prefix);
      if (requestedHeartbeat != null) {
         cf.setRequestedHeartbeat(Integer.valueOf(requestedHeartbeat));
      }

      String connectionTimeout = lookUp("connection.timeout", properties, prefix);
      if (connectionTimeout != null) {
         cf.setConnectionTimeout(Integer.valueOf(connectionTimeout));
      }

      String handshakeTimeout = lookUp("handshake.timeout", properties, prefix);
      if (handshakeTimeout != null) {
         cf.setHandshakeTimeout(Integer.valueOf(handshakeTimeout));
      }

      String shutdownTimeout = lookUp("shutdown.timeout", properties, prefix);
      if (shutdownTimeout != null) {
         cf.setShutdownTimeout(Integer.valueOf(shutdownTimeout));
      }

      Map<String, Object> clientProperties = new HashMap<>();
      Map<String, Object> defaultClientProperties = AMQConnection.defaultClientProperties();
      clientProperties.putAll(defaultClientProperties);

      for (Entry<String, String> entry : properties.entrySet()) {
         if (entry.getKey().startsWith(prefix + "client.properties.")) {
            String clientPropertyKey = entry.getKey().substring((prefix + "client.properties.").length());
            if (!defaultClientProperties.containsKey(clientPropertyKey) || entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
               clientProperties.put(clientPropertyKey, entry.getValue());
            } else {
               clientProperties.remove(clientPropertyKey);
            }
         }
      }

      cf.setClientProperties(clientProperties);
      String automaticRecovery = lookUp("connection.recovery.enabled", properties, prefix);
      if (automaticRecovery != null) {
         cf.setAutomaticRecoveryEnabled(Boolean.valueOf(automaticRecovery));
      }

      String topologyRecovery = lookUp("topology.recovery.enabled", properties, prefix);
      if (topologyRecovery != null) {
         cf.setTopologyRecoveryEnabled(Boolean.valueOf(topologyRecovery));
      }

      String networkRecoveryInterval = lookUp("connection.recovery.interval", properties, prefix);
      if (networkRecoveryInterval != null) {
         cf.setNetworkRecoveryInterval(Long.valueOf(networkRecoveryInterval));
      }

      String channelRpcTimeout = lookUp("channel.rpc.timeout", properties, prefix);
      if (channelRpcTimeout != null) {
         cf.setChannelRpcTimeout(Integer.valueOf(channelRpcTimeout));
      }

      String channelShouldCheckRpcResponseType = lookUp("channel.should.check.rpc.response.type", properties, prefix);
      if (channelShouldCheckRpcResponseType != null) {
         cf.setChannelShouldCheckRpcResponseType(Boolean.valueOf(channelShouldCheckRpcResponseType));
      }

      String useNio = lookUp("use.nio", properties, prefix);
      if (useNio != null && Boolean.valueOf(useNio)) {
         cf.useNio();
         NioParams nioParams = new NioParams();
         String readByteBufferSize = lookUp("nio.read.byte.buffer.size", properties, prefix);
         if (readByteBufferSize != null) {
            nioParams.setReadByteBufferSize(Integer.valueOf(readByteBufferSize));
         }

         String writeByteBufferSize = lookUp("nio.write.byte.buffer.size", properties, prefix);
         if (writeByteBufferSize != null) {
            nioParams.setWriteByteBufferSize(Integer.valueOf(writeByteBufferSize));
         }

         String nbIoThreads = lookUp("nio.nb.io.threads", properties, prefix);
         if (nbIoThreads != null) {
            nioParams.setNbIoThreads(Integer.valueOf(nbIoThreads));
         }

         String writeEnqueuingTime = lookUp("nio.write.enqueuing.timeout.in.ms", properties, prefix);
         if (writeEnqueuingTime != null) {
            nioParams.setWriteEnqueuingTimeoutInMs(Integer.valueOf(writeEnqueuingTime));
         }

         String writeQueueCapacity = lookUp("nio.write.queue.capacity", properties, prefix);
         if (writeQueueCapacity != null) {
            nioParams.setWriteQueueCapacity(Integer.valueOf(writeQueueCapacity));
         }

         cf.setNioParams(nioParams);
      }

      String useSsl = lookUp("ssl.enabled", properties, prefix);
      if (useSsl != null && Boolean.valueOf(useSsl)) {
         setUpSsl(cf, properties, prefix);
      }
   }

   private static void setUpSsl(ConnectionFactory cf, Map<String, String> properties, String prefix) {
      String algorithm = lookUp("ssl.algorithm", properties, prefix);
      String keyStoreLocation = lookUp("ssl.key.store", properties, prefix);
      String keyStorePassword = lookUp("ssl.key.store.password", properties, prefix);
      String keyStoreType = lookUp("ssl.key.store.type", properties, prefix, "PKCS12");
      String keyStoreAlgorithm = lookUp("ssl.key.store.algorithm", properties, prefix, "SunX509");
      String trustStoreLocation = lookUp("ssl.trust.store", properties, prefix);
      String trustStorePassword = lookUp("ssl.trust.store.password", properties, prefix);
      String trustStoreType = lookUp("ssl.trust.store.type", properties, prefix, "JKS");
      String trustStoreAlgorithm = lookUp("ssl.trust.store.algorithm", properties, prefix, "SunX509");
      String validateServerCertificate = lookUp("ssl.validate.server.certificate", properties, prefix);
      String verifyHostname = lookUp("ssl.verify.hostname", properties, prefix);

      try {
         algorithm = algorithm == null
            ? ConnectionFactory.computeDefaultTlsProtocol(SSLContext.getDefault().getSupportedSSLParameters().getProtocols())
            : algorithm;
         boolean enableHostnameVerification = verifyHostname == null ? Boolean.FALSE : Boolean.valueOf(verifyHostname);
         if (keyStoreLocation == null && trustStoreLocation == null) {
            setUpBasicSsl(
               cf, validateServerCertificate == null ? Boolean.FALSE : Boolean.valueOf(validateServerCertificate), enableHostnameVerification, algorithm
            );
         } else {
            KeyManager[] keyManagers = configureKeyManagers(keyStoreLocation, keyStorePassword, keyStoreType, keyStoreAlgorithm);
            TrustManager[] trustManagers = configureTrustManagers(trustStoreLocation, trustStorePassword, trustStoreType, trustStoreAlgorithm);
            SSLContext sslContext = SSLContext.getInstance(algorithm);
            sslContext.init(keyManagers, trustManagers, null);
            cf.useSslProtocol(sslContext);
            if (enableHostnameVerification) {
               cf.enableHostnameVerification();
            }
         }
      } catch (NoSuchAlgorithmException | IOException | CertificateException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
         throw new IllegalStateException("Error while configuring TLS", e);
      }
   }

   private static KeyManager[] configureKeyManagers(String keystore, String keystorePassword, String keystoreType, String keystoreAlgorithm) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
      char[] keyPassphrase = null;
      if (keystorePassword != null) {
         keyPassphrase = keystorePassword.toCharArray();
      }

      KeyManager[] keyManagers = null;
      if (keystore != null) {
         KeyStore ks = KeyStore.getInstance(keystoreType);

         try (InputStream in = loadResource(keystore)) {
            ks.load(in, keyPassphrase);
         }

         KeyManagerFactory kmf = KeyManagerFactory.getInstance(keystoreAlgorithm);
         kmf.init(ks, keyPassphrase);
         keyManagers = kmf.getKeyManagers();
      }

      return keyManagers;
   }

   private static TrustManager[] configureTrustManagers(String truststore, String truststorePassword, String truststoreType, String truststoreAlgorithm) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
      char[] trustPassphrase = null;
      if (truststorePassword != null) {
         trustPassphrase = truststorePassword.toCharArray();
      }

      TrustManager[] trustManagers = null;
      if (truststore != null) {
         KeyStore tks = KeyStore.getInstance(truststoreType);

         try (InputStream in = loadResource(truststore)) {
            tks.load(in, trustPassphrase);
         }

         TrustManagerFactory tmf = TrustManagerFactory.getInstance(truststoreAlgorithm);
         tmf.init(tks);
         trustManagers = tmf.getTrustManagers();
      }

      return trustManagers;
   }

   private static void setUpBasicSsl(ConnectionFactory cf, boolean validateServerCertificate, boolean verifyHostname, String sslAlgorithm) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
      if (validateServerCertificate) {
         useDefaultTrustStore(cf, sslAlgorithm, verifyHostname);
      } else if (sslAlgorithm == null) {
         cf.useSslProtocol();
      } else {
         cf.useSslProtocol(sslAlgorithm);
      }
   }

   private static void useDefaultTrustStore(ConnectionFactory cf, String sslAlgorithm, boolean verifyHostname) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
      SSLContext sslContext = SSLContext.getInstance(sslAlgorithm);
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore)null);
      sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
      cf.useSslProtocol(sslContext);
      if (verifyHostname) {
         cf.enableHostnameVerification();
      }
   }

   public static void load(ConnectionFactory connectionFactory, String propertyFileLocation) throws IOException {
      load(connectionFactory, propertyFileLocation, "rabbitmq.");
   }

   public static void load(ConnectionFactory connectionFactory, Properties properties) {
      load(connectionFactory, (Map<String, String>)properties, "rabbitmq.");
   }

   public static void load(ConnectionFactory connectionFactory, Properties properties, String prefix) {
      load(connectionFactory, (Map<String, String>)properties, prefix);
   }

   public static void load(ConnectionFactory connectionFactory, Map<String, String> properties) {
      load(connectionFactory, properties, "rabbitmq.");
   }

   public static String lookUp(String key, Map<String, String> properties, String prefix) {
      return lookUp(key, properties, prefix, null);
   }

   public static String lookUp(String key, Map<String, String> properties, String prefix, String defaultValue) {
      String value = properties.get(prefix + key);
      if (value == null) {
         value = ALIASES.getOrDefault(key, Collections.emptyList())
            .stream()
            .map(alias -> properties.get(prefix + alias))
            .filter(v -> v != null)
            .findFirst()
            .orElse(defaultValue);
      }

      return value;
   }
}
