package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.args.ClientAttributeOption;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisValidationException;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;
import me.neznamy.tab.libs.redis.clients.jedis.util.RedisInputStream;
import me.neznamy.tab.libs.redis.clients.jedis.util.RedisOutputStream;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class Connection implements Closeable {
   private ConnectionPool memberOf;
   protected RedisProtocol protocol;
   private final JedisSocketFactory socketFactory;
   private Socket socket;
   private RedisOutputStream outputStream;
   private RedisInputStream inputStream;
   private int soTimeout = 0;
   private int infiniteSoTimeout = 0;
   private boolean broken = false;
   private boolean strValActive;
   private String strVal;
   protected String server;
   protected String version;

   public Connection() {
      this("127.0.0.1", 6379);
   }

   public Connection(String host, int port) {
      this(new HostAndPort(host, port));
   }

   public Connection(HostAndPort hostAndPort) {
      this(new DefaultJedisSocketFactory(hostAndPort));
   }

   public Connection(HostAndPort hostAndPort, JedisClientConfig clientConfig) {
      this(new DefaultJedisSocketFactory(hostAndPort, clientConfig), clientConfig);
   }

   public Connection(JedisSocketFactory socketFactory) {
      this.socketFactory = socketFactory;
   }

   public Connection(JedisSocketFactory socketFactory, JedisClientConfig clientConfig) {
      this.socketFactory = socketFactory;
      this.soTimeout = clientConfig.getSocketTimeoutMillis();
      this.infiniteSoTimeout = clientConfig.getBlockingSocketTimeoutMillis();
      this.initializeFromClientConfig(clientConfig);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{" + this.socketFactory + "}";
   }

   @Experimental
   public String toIdentityString() {
      if (this.strValActive == this.broken && this.strVal != null) {
         return this.strVal;
      }

      String className = this.getClass().getSimpleName();
      int id = this.hashCode();
      if (this.socket == null) {
         return String.format("%s{id: 0x%X}", className, id);
      }

      SocketAddress remoteAddr = this.socket.getRemoteSocketAddress();
      SocketAddress localAddr = this.socket.getLocalSocketAddress();
      if (remoteAddr != null) {
         this.strVal = String.format("%s{id: 0x%X, L:%s %c R:%s}", className, id, localAddr, Character.valueOf((char)(this.broken ? '!' : '-')), remoteAddr);
      } else if (localAddr != null) {
         this.strVal = String.format("%s{id: 0x%X, L:%s}", className, id, localAddr);
      } else {
         this.strVal = String.format("%s{id: 0x%X}", className, id);
      }

      this.strValActive = this.broken;
      return this.strVal;
   }

   public final RedisProtocol getRedisProtocol() {
      return this.protocol;
   }

   public final void setHandlingPool(ConnectionPool pool) {
      this.memberOf = pool;
   }

   final HostAndPort getHostAndPort() {
      return ((DefaultJedisSocketFactory)this.socketFactory).getHostAndPort();
   }

   public int getSoTimeout() {
      return this.soTimeout;
   }

   public void setSoTimeout(int soTimeout) {
      this.soTimeout = soTimeout;
      if (this.socket != null) {
         try {
            this.socket.setSoTimeout(soTimeout);
         } catch (SocketException ex) {
            this.setBroken();
            throw new JedisConnectionException(ex);
         }
      }
   }

   public void setTimeoutInfinite() {
      try {
         if (!this.isConnected()) {
            this.connect();
         }

         this.socket.setSoTimeout(this.infiniteSoTimeout);
      } catch (SocketException ex) {
         this.setBroken();
         throw new JedisConnectionException(ex);
      }
   }

   public void rollbackTimeout() {
      try {
         this.socket.setSoTimeout(this.soTimeout);
      } catch (SocketException ex) {
         this.setBroken();
         throw new JedisConnectionException(ex);
      }
   }

   public Object executeCommand(ProtocolCommand cmd) {
      return this.executeCommand(new CommandArguments(cmd));
   }

   public Object executeCommand(CommandArguments args) {
      this.sendCommand(args);
      return this.getOne();
   }

   public <T> T executeCommand(CommandObject<T> commandObject) {
      CommandArguments args = commandObject.getArguments();
      this.sendCommand(args);
      if (!args.isBlocking()) {
         return commandObject.getBuilder().build(this.getOne());
      }

      try {
         this.setTimeoutInfinite();
         return commandObject.getBuilder().build(this.getOne());
      } finally {
         this.rollbackTimeout();
      }
   }

   public void sendCommand(ProtocolCommand cmd) {
      this.sendCommand(new CommandArguments(cmd));
   }

   public void sendCommand(ProtocolCommand cmd, Rawable keyword) {
      this.sendCommand(new CommandArguments(cmd).add(keyword));
   }

   public void sendCommand(ProtocolCommand cmd, String... args) {
      this.sendCommand(new CommandArguments(cmd).addObjects(args));
   }

   public void sendCommand(ProtocolCommand cmd, byte[]... args) {
      this.sendCommand(new CommandArguments(cmd).addObjects((Object[])args));
   }

   public void sendCommand(CommandArguments args) {
      try {
         this.connect();
         Protocol.sendCommand(this.outputStream, args);
      } catch (JedisConnectionException var5) {
         JedisConnectionException ex = var5;

         try {
            String errorMessage = Protocol.readErrorLineIfPossible(this.inputStream);
            if (errorMessage != null && errorMessage.length() > 0) {
               ex = new JedisConnectionException(errorMessage, ex.getCause());
            }
         } catch (Exception var4) {
         }

         this.setBroken();
         throw ex;
      }
   }

   public void connect() throws JedisConnectionException {
      if (!this.isConnected()) {
         try {
            this.socket = this.socketFactory.createSocket();
            this.soTimeout = this.socket.getSoTimeout();
            this.outputStream = new RedisOutputStream(this.socket.getOutputStream());
            this.inputStream = new RedisInputStream(this.socket.getInputStream());
            this.broken = false;
         } catch (JedisConnectionException jce) {
            this.setBroken();
            throw jce;
         } catch (IOException ioe) {
            this.setBroken();
            throw new JedisConnectionException("Failed to create input/output stream", ioe);
         } finally {
            if (this.broken) {
               IOUtils.closeQuietly(this.socket);
            }
         }
      }
   }

   @Override
   public void close() {
      if (this.memberOf != null) {
         ConnectionPool pool = this.memberOf;
         this.memberOf = null;
         if (this.isBroken()) {
            pool.returnBrokenResource(this);
         } else {
            pool.returnResource(this);
         }
      } else {
         this.disconnect();
      }
   }

   public void disconnect() {
      if (this.isConnected()) {
         try {
            this.outputStream.flush();
            this.socket.close();
         } catch (IOException ex) {
            throw new JedisConnectionException(ex);
         } finally {
            IOUtils.closeQuietly(this.socket);
            this.setBroken();
         }
      }
   }

   public boolean isConnected() {
      return this.socket != null
         && this.socket.isBound()
         && !this.socket.isClosed()
         && this.socket.isConnected()
         && !this.socket.isInputShutdown()
         && !this.socket.isOutputShutdown();
   }

   public boolean isBroken() {
      return this.broken;
   }

   public void setBroken() {
      this.broken = true;
   }

   public String getStatusCodeReply() {
      this.flush();
      byte[] resp = (byte[])this.readProtocolWithCheckingBroken();
      return null == resp ? null : SafeEncoder.encode(resp);
   }

   public String getBulkReply() {
      byte[] result = this.getBinaryBulkReply();
      return null != result ? SafeEncoder.encode(result) : null;
   }

   public byte[] getBinaryBulkReply() {
      this.flush();
      return (byte[])this.readProtocolWithCheckingBroken();
   }

   public Long getIntegerReply() {
      this.flush();
      return (Long)this.readProtocolWithCheckingBroken();
   }

   public List<String> getMultiBulkReply() {
      return BuilderFactory.STRING_LIST.build(this.getBinaryMultiBulkReply());
   }

   public List<byte[]> getBinaryMultiBulkReply() {
      this.flush();
      return (List<byte[]>)this.readProtocolWithCheckingBroken();
   }

   @Deprecated
   public List<Object> getUnflushedObjectMultiBulkReply() {
      return (List<Object>)this.readProtocolWithCheckingBroken();
   }

   public Object getUnflushedObject() {
      return this.readProtocolWithCheckingBroken();
   }

   public List<Object> getObjectMultiBulkReply() {
      this.flush();
      return (List<Object>)this.readProtocolWithCheckingBroken();
   }

   public List<Long> getIntegerMultiBulkReply() {
      this.flush();
      return (List<Long>)this.readProtocolWithCheckingBroken();
   }

   public Object getOne() {
      this.flush();
      return this.readProtocolWithCheckingBroken();
   }

   protected void flush() {
      try {
         this.outputStream.flush();
      } catch (IOException ex) {
         this.setBroken();
         throw new JedisConnectionException(ex);
      }
   }

   @Experimental
   protected Object protocolRead(RedisInputStream is) {
      return Protocol.read(is);
   }

   @Experimental
   protected void protocolReadPushes(RedisInputStream is) {
   }

   protected Object readProtocolWithCheckingBroken() {
      if (this.broken) {
         throw new JedisConnectionException("Attempting to read from a broken connection.");
      }

      try {
         return this.protocolRead(this.inputStream);
      } catch (JedisConnectionException exc) {
         this.broken = true;
         throw exc;
      }
   }

   protected void readPushesWithCheckingBroken() {
      if (this.broken) {
         throw new JedisConnectionException("Attempting to read from a broken connection.");
      }

      try {
         if (this.inputStream.available() > 0) {
            this.protocolReadPushes(this.inputStream);
         }
      } catch (IOException e) {
         this.broken = true;
         throw new JedisConnectionException("Failed to check buffer on connection.", e);
      } catch (JedisConnectionException exc) {
         this.setBroken();
         throw exc;
      }
   }

   public List<Object> getMany(int count) {
      this.flush();
      List<Object> responses = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         try {
            responses.add(this.readProtocolWithCheckingBroken());
         } catch (JedisDataException e) {
            responses.add(e);
         }
      }

      return responses;
   }

   private static boolean validateClientInfo(String info) {
      for (int i = 0; i < info.length(); i++) {
         char c = info.charAt(i);
         if (c < '!' || c > '~') {
            throw new JedisValidationException("client info cannot contain spaces, newlines or special characters.");
         }
      }

      return true;
   }

   protected void initializeFromClientConfig(JedisClientConfig config) {
      try {
         this.connect();
         this.protocol = config.getRedisProtocol();
         Supplier<RedisCredentials> credentialsProvider = config.getCredentialsProvider();
         if (credentialsProvider instanceof RedisCredentialsProvider) {
            RedisCredentialsProvider redisCredentialsProvider = (RedisCredentialsProvider)credentialsProvider;

            try {
               redisCredentialsProvider.prepare();
               this.helloAndAuth(this.protocol, redisCredentialsProvider.get());
            } finally {
               redisCredentialsProvider.cleanUp();
            }
         } else {
            this.helloAndAuth(
               this.protocol, credentialsProvider != null ? credentialsProvider.get() : new DefaultRedisCredentials(config.getUser(), config.getPassword())
            );
         }

         List<CommandArguments> fireAndForgetMsg = new ArrayList<>();
         String clientName = config.getClientName();
         if (clientName != null && validateClientInfo(clientName)) {
            fireAndForgetMsg.add(new CommandArguments(Protocol.Command.CLIENT).add(Protocol.Keyword.SETNAME).add(clientName));
         }

         ClientSetInfoConfig setInfoConfig = config.getClientSetInfoConfig();
         if (setInfoConfig == null) {
            setInfoConfig = ClientSetInfoConfig.DEFAULT;
         }

         if (!setInfoConfig.isDisabled()) {
            String libName = JedisMetaInfo.getArtifactId();
            if (libName != null && validateClientInfo(libName)) {
               String libNameSuffix = setInfoConfig.getLibNameSuffix();
               if (libNameSuffix != null) {
                  libName = libName + '(' + libNameSuffix + ')';
               }

               fireAndForgetMsg.add(
                  new CommandArguments(Protocol.Command.CLIENT).add(Protocol.Keyword.SETINFO).add(ClientAttributeOption.LIB_NAME.getRaw()).add(libName)
               );
            }

            String libVersion = JedisMetaInfo.getVersion();
            if (libVersion != null && validateClientInfo(libVersion)) {
               fireAndForgetMsg.add(
                  new CommandArguments(Protocol.Command.CLIENT).add(Protocol.Keyword.SETINFO).add(ClientAttributeOption.LIB_VER.getRaw()).add(libVersion)
               );
            }
         }

         if (config.isReadOnlyForRedisClusterReplicas()) {
            fireAndForgetMsg.add(new CommandArguments(Protocol.Command.READONLY));
         }

         for (CommandArguments arg : fireAndForgetMsg) {
            this.sendCommand(arg);
         }

         this.getMany(fireAndForgetMsg.size());
         int dbIndex = config.getDatabase();
         if (dbIndex > 0) {
            this.select(dbIndex);
         }
      } catch (JedisException je) {
         try {
            this.disconnect();
         } catch (Exception var11) {
         }

         throw je;
      }
   }

   private void helloAndAuth(RedisProtocol protocol, RedisCredentials credentials) {
      Map<String, Object> helloResult = null;
      if (protocol != null && credentials != null && credentials.getUser() != null) {
         byte[] rawPass = this.encodeToBytes(credentials.getPassword());

         try {
            helloResult = this.hello(SafeEncoder.encode(protocol.version()), Protocol.Keyword.AUTH.getRaw(), SafeEncoder.encode(credentials.getUser()), rawPass);
         } finally {
            Arrays.fill(rawPass, (byte)0);
         }
      } else {
         this.auth(credentials);
         helloResult = protocol == null ? null : this.hello(SafeEncoder.encode(protocol.version()));
      }

      if (helloResult != null) {
         this.server = (String)helloResult.get("server");
         this.version = (String)helloResult.get("version");
      }
   }

   private void auth(RedisCredentials credentials) {
      if (credentials != null && credentials.getPassword() != null) {
         byte[] rawPass = this.encodeToBytes(credentials.getPassword());

         try {
            if (credentials.getUser() == null) {
               this.sendCommand(Protocol.Command.AUTH, rawPass);
            } else {
               this.sendCommand(Protocol.Command.AUTH, SafeEncoder.encode(credentials.getUser()), rawPass);
            }
         } finally {
            Arrays.fill(rawPass, (byte)0);
         }

         this.getStatusCodeReply();
      }
   }

   protected Map<String, Object> hello(byte[]... args) {
      this.sendCommand(Protocol.Command.HELLO, args);
      return BuilderFactory.ENCODED_OBJECT_MAP.build(this.getOne());
   }

   protected byte[] encodeToBytes(char[] chars) {
      ByteBuffer passBuf = Protocol.CHARSET.encode(CharBuffer.wrap(chars));
      byte[] rawPass = Arrays.copyOfRange(passBuf.array(), passBuf.position(), passBuf.limit());
      Arrays.fill(passBuf.array(), (byte)0);
      return rawPass;
   }

   public String select(int index) {
      this.sendCommand(Protocol.Command.SELECT, Protocol.toByteArray(index));
      return this.getStatusCodeReply();
   }

   public boolean ping() {
      this.sendCommand(Protocol.Command.PING);
      String status = this.getStatusCodeReply();
      if (!"PONG".equals(status)) {
         throw new JedisException(status);
      } else {
         return true;
      }
   }
}
