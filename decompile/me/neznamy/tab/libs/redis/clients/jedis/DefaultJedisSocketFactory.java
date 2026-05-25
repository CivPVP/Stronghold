package me.neznamy.tab.libs.redis.clients.jedis;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;

public class DefaultJedisSocketFactory implements JedisSocketFactory {
   protected static final HostAndPort DEFAULT_HOST_AND_PORT = new HostAndPort("127.0.0.1", 6379);
   private volatile HostAndPort hostAndPort = DEFAULT_HOST_AND_PORT;
   private int connectionTimeout = 2000;
   private int socketTimeout = 2000;
   private boolean ssl = false;
   private SSLSocketFactory sslSocketFactory = null;
   private SSLParameters sslParameters = null;
   private HostnameVerifier hostnameVerifier = null;
   private HostAndPortMapper hostAndPortMapper = null;

   public DefaultJedisSocketFactory() {
   }

   public DefaultJedisSocketFactory(HostAndPort hostAndPort) {
      this(hostAndPort, null);
   }

   public DefaultJedisSocketFactory(JedisClientConfig config) {
      this(null, config);
   }

   public DefaultJedisSocketFactory(HostAndPort hostAndPort, JedisClientConfig config) {
      if (hostAndPort != null) {
         this.hostAndPort = hostAndPort;
      }

      if (config != null) {
         this.connectionTimeout = config.getConnectionTimeoutMillis();
         this.socketTimeout = config.getSocketTimeoutMillis();
         this.ssl = config.isSsl();
         this.sslSocketFactory = config.getSslSocketFactory();
         this.sslParameters = config.getSslParameters();
         this.hostnameVerifier = config.getHostnameVerifier();
         this.hostAndPortMapper = config.getHostAndPortMapper();
      }
   }

   private Socket connectToFirstSuccessfulHost(HostAndPort hostAndPort) throws Exception {
      List<InetAddress> hosts = Arrays.asList(InetAddress.getAllByName(hostAndPort.getHost()));
      if (hosts.size() > 1) {
         Collections.shuffle(hosts);
      }

      JedisConnectionException jce = new JedisConnectionException("Failed to connect to " + hostAndPort + ".");

      for (InetAddress host : hosts) {
         try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(host, hostAndPort.getPort()), this.connectionTimeout);
            return socket;
         } catch (Exception e) {
            jce.addSuppressed(e);
         }
      }

      throw jce;
   }

   @Override
   public Socket createSocket() throws JedisConnectionException {
      Socket socket = null;

      try {
         HostAndPort _hostAndPort = this.getSocketHostAndPort();
         socket = this.connectToFirstSuccessfulHost(_hostAndPort);
         socket.setSoTimeout(this.socketTimeout);
         if (this.ssl) {
            SSLSocketFactory _sslSocketFactory = this.sslSocketFactory;
            if (null == _sslSocketFactory) {
               _sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            }

            Socket plainSocket = socket;
            socket = _sslSocketFactory.createSocket(socket, _hostAndPort.getHost(), _hostAndPort.getPort(), true);
            if (null != this.sslParameters) {
               ((SSLSocket)socket).setSSLParameters(this.sslParameters);
            }

            socket = new SSLSocketWrapper((SSLSocket)socket, plainSocket);
            if (null != this.hostnameVerifier && !this.hostnameVerifier.verify(_hostAndPort.getHost(), ((SSLSocket)socket).getSession())) {
               String message = String.format("The connection to '%s' failed ssl/tls hostname verification.", _hostAndPort.getHost());
               throw new JedisConnectionException(message);
            }
         }

         return socket;
      } catch (Exception ex) {
         IOUtils.closeQuietly(socket);
         if (ex instanceof JedisConnectionException) {
            throw (JedisConnectionException)ex;
         } else {
            throw new JedisConnectionException("Failed to create socket.", ex);
         }
      }
   }

   public void updateHostAndPort(HostAndPort hostAndPort) {
      this.hostAndPort = hostAndPort;
   }

   public HostAndPort getHostAndPort() {
      return this.hostAndPort;
   }

   protected HostAndPort getSocketHostAndPort() {
      HostAndPortMapper mapper = this.hostAndPortMapper;
      HostAndPort hap = this.hostAndPort;
      if (mapper != null) {
         HostAndPort mapped = mapper.getHostAndPort(hap);
         if (mapped != null) {
            return mapped;
         }
      }

      return hap;
   }

   @Override
   public String toString() {
      return "DefaultJedisSocketFactory{" + this.hostAndPort.toString() + "}";
   }
}
