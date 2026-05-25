package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.AlreadyClosedException;
import me.neznamy.tab.libs.com.rabbitmq.client.AuthenticationFailureException;
import me.neznamy.tab.libs.com.rabbitmq.client.BlockedCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.BlockedListener;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Command;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;
import me.neznamy.tab.libs.com.rabbitmq.client.ConnectionFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.ExceptionHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;
import me.neznamy.tab.libs.com.rabbitmq.client.MetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.MissedHeartbeatException;
import me.neznamy.tab.libs.com.rabbitmq.client.NoOpMetricsCollector;
import me.neznamy.tab.libs.com.rabbitmq.client.PossibleAuthenticationFailureException;
import me.neznamy.tab.libs.com.rabbitmq.client.ProtocolVersionMismatchException;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslConfig;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslMechanism;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.TrafficListener;
import me.neznamy.tab.libs.com.rabbitmq.client.UnblockedCallback;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.RecoveryCanBeginListener;
import me.neznamy.tab.libs.com.rabbitmq.client.observation.ObservationCollector;
import me.neznamy.tab.libs.com.rabbitmq.utility.BlockingCell;
import me.neznamy.tab.libs.com.rabbitmq.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMQConnection extends ShutdownNotifierComponent implements Connection, NetworkConnection {
   private static final int MAX_UNSIGNED_SHORT = 65535;
   private static final Logger LOGGER = LoggerFactory.getLogger(AMQConnection.class);
   public static final double CHANNEL_SHUTDOWN_TIMEOUT_MULTIPLIER = 1.05;
   private final ExecutorService consumerWorkServiceExecutor;
   private final ScheduledExecutorService heartbeatExecutor;
   private final ExecutorService shutdownExecutor;
   private Thread mainLoopThread;
   private final AtomicBoolean ioLoopThreadSet = new AtomicBoolean(false);
   private volatile Thread ioLoopThread;
   private ThreadFactory threadFactory = Executors.defaultThreadFactory();
   private String id;
   private final List<RecoveryCanBeginListener> recoveryCanBeginListeners = Collections.synchronizedList(new ArrayList<>());
   private final ErrorOnWriteListener errorOnWriteListener;
   private final int workPoolTimeout;
   private final AtomicBoolean finalShutdownStarted = new AtomicBoolean(false);
   private volatile ObservationCollector.ConnectionInfo connectionInfo;
   private static final Version clientVersion = new Version(0, 9);
   private final AMQChannel _channel0;
   protected ConsumerWorkService _workService = null;
   private final FrameHandler _frameHandler;
   private volatile boolean _running = false;
   private final ExceptionHandler _exceptionHandler;
   private final BlockingCell<Object> _appContinuation = new BlockingCell<>();
   private volatile boolean _brokerInitiatedShutdown;
   private volatile boolean _inConnectionNegotiation;
   private HeartbeatSender _heartbeatSender;
   private final String _virtualHost;
   private final Map<String, Object> _clientProperties;
   private final SaslConfig saslConfig;
   private final int requestedHeartbeat;
   private final int requestedChannelMax;
   private final int requestedFrameMax;
   private final int handshakeTimeout;
   private final int shutdownTimeout;
   private final CredentialsProvider credentialsProvider;
   private final Collection<BlockedListener> blockedListeners = new CopyOnWriteArrayList<>();
   protected final MetricsCollector metricsCollector;
   protected final ObservationCollector observationCollector;
   private final int channelRpcTimeout;
   private final boolean channelShouldCheckRpcResponseType;
   private final TrafficListener trafficListener;
   private final CredentialsRefreshService credentialsRefreshService;
   private volatile int _frameMax = 0;
   private volatile int _missedHeartbeats = 0;
   private volatile int _heartbeat = 0;
   private volatile ChannelManager _channelManager;
   private volatile Map<String, Object> _serverProperties;
   private final int maxInboundMessageBodySize;
   private static long SOCKET_CLOSE_TIMEOUT = 10000L;

   public static Map<String, Object> defaultClientProperties() {
      Map<String, Object> props = new HashMap<>();
      props.put("product", LongStringHelper.asLongString("RabbitMQ"));
      props.put("version", LongStringHelper.asLongString(ClientVersion.VERSION));
      props.put("platform", LongStringHelper.asLongString("Java"));
      props.put("copyright", LongStringHelper.asLongString("Copyright (c) 2007-2024 Broadcom Inc. and/or its subsidiaries."));
      props.put("information", LongStringHelper.asLongString("Licensed under the MPL. See https://www.rabbitmq.com/"));
      Map<String, Object> capabilities = new HashMap<>();
      capabilities.put("publisher_confirms", true);
      capabilities.put("exchange_exchange_bindings", true);
      capabilities.put("basic.nack", true);
      capabilities.put("consumer_cancel_notify", true);
      capabilities.put("connection.blocked", true);
      capabilities.put("authentication_failure_close", true);
      props.put("capabilities", capabilities);
      return props;
   }

   public final void disconnectChannel(ChannelN channel) {
      ChannelManager cm = this._channelManager;
      if (cm != null) {
         cm.releaseChannelNumber(channel);
      }
   }

   private void ensureIsOpen() throws AlreadyClosedException {
      if (!this.isOpen()) {
         throw new AlreadyClosedException(this.getCloseReason());
      }
   }

   @Override
   public InetAddress getAddress() {
      return this._frameHandler.getAddress();
   }

   @Override
   public InetAddress getLocalAddress() {
      return this._frameHandler.getLocalAddress();
   }

   @Override
   public int getPort() {
      return this._frameHandler.getPort();
   }

   @Override
   public int getLocalPort() {
      return this._frameHandler.getLocalPort();
   }

   public FrameHandler getFrameHandler() {
      return this._frameHandler;
   }

   @Override
   public Map<String, Object> getServerProperties() {
      return this._serverProperties;
   }

   public AMQConnection(ConnectionParams params, FrameHandler frameHandler) {
      this(params, frameHandler, new NoOpMetricsCollector(), ObservationCollector.NO_OP);
   }

   public AMQConnection(ConnectionParams params, FrameHandler frameHandler, MetricsCollector metricsCollector, ObservationCollector observationCollector) {
      checkPreconditions();
      this.credentialsProvider = params.getCredentialsProvider();
      this._frameHandler = frameHandler;
      this._virtualHost = params.getVirtualHost();
      this._exceptionHandler = params.getExceptionHandler();
      this._clientProperties = new HashMap<>(params.getClientProperties());
      this.requestedFrameMax = params.getRequestedFrameMax();
      this.requestedChannelMax = params.getRequestedChannelMax();
      this.requestedHeartbeat = params.getRequestedHeartbeat();
      this.handshakeTimeout = params.getHandshakeTimeout();
      this.shutdownTimeout = params.getShutdownTimeout();
      this.saslConfig = params.getSaslConfig();
      this.consumerWorkServiceExecutor = params.getConsumerWorkServiceExecutor();
      this.heartbeatExecutor = params.getHeartbeatExecutor();
      this.shutdownExecutor = params.getShutdownExecutor();
      this.threadFactory = params.getThreadFactory();
      if (params.getChannelRpcTimeout() < 0) {
         throw new IllegalArgumentException("Continuation timeout on RPC calls cannot be less than 0");
      }

      this.channelRpcTimeout = params.getChannelRpcTimeout();
      this.channelShouldCheckRpcResponseType = params.channelShouldCheckRpcResponseType();
      this.trafficListener = params.getTrafficListener() == null ? TrafficListener.NO_OP : params.getTrafficListener();
      this.credentialsRefreshService = params.getCredentialsRefreshService();
      this._channel0 = this.createChannel0();
      this._channelManager = null;
      this._brokerInitiatedShutdown = false;
      this._inConnectionNegotiation = true;
      this.metricsCollector = metricsCollector;
      this.observationCollector = observationCollector;
      this.errorOnWriteListener = params.getErrorOnWriteListener() != null ? params.getErrorOnWriteListener() : (connection, exception) -> {
         throw exception;
      };
      this.workPoolTimeout = params.getWorkPoolTimeout();
      this.maxInboundMessageBodySize = params.getMaxInboundMessageBodySize();
   }

   AMQChannel createChannel0() {
      return new AMQChannel(this, 0) {
         @Override
         public boolean processAsync(Command c) throws IOException {
            return this.getConnection().processControlCommand(c);
         }
      };
   }

   private void initializeConsumerWorkService() {
      this._workService = new ConsumerWorkService(this.consumerWorkServiceExecutor, this.threadFactory, this.workPoolTimeout, this.shutdownTimeout);
   }

   private void initializeHeartbeatSender() {
      this._heartbeatSender = new HeartbeatSender(this._frameHandler, this.heartbeatExecutor, this.threadFactory);
   }

   public void start() throws IOException, TimeoutException {
      this.initializeConsumerWorkService();
      this.initializeHeartbeatSender();
      this._running = true;
      AMQChannel.SimpleBlockingRpcContinuation connStartBlocker = new AMQChannel.SimpleBlockingRpcContinuation();
      this._channel0.enqueueRpc(connStartBlocker);

      try {
         this._frameHandler.setTimeout(this.handshakeTimeout);
         this._frameHandler.sendHeader();
      } catch (IOException ioe) {
         this._frameHandler.close();
         throw ioe;
      }

      this._frameHandler.initialize(this);
      AMQP.Connection.Tune connTune = null;

      try {
         AMQP.Connection.Start connStart = (AMQP.Connection.Start)connStartBlocker.getReply(this.handshakeTimeout / 2).getMethod();
         this._serverProperties = Collections.unmodifiableMap(connStart.getServerProperties());
         Version serverVersion = new Version(connStart.getVersionMajor(), connStart.getVersionMinor());
         if (!Version.checkVersion(clientVersion, serverVersion)) {
            throw new ProtocolVersionMismatchException(clientVersion, serverVersion);
         }

         String[] mechanisms = connStart.getMechanisms().toString().split(" ");
         SaslMechanism sm = this.saslConfig.getSaslMechanism(mechanisms);
         if (sm == null) {
            throw new IOException("No compatible authentication mechanism found - server offered [" + connStart.getMechanisms() + "]");
         }

         String username = this.credentialsProvider.getUsername();
         String password = this.credentialsProvider.getPassword();
         if (this.credentialsProvider.getTimeBeforeExpiration() != null) {
            if (this.credentialsRefreshService == null) {
               throw new IllegalStateException("Credentials can expire, a credentials refresh service should be set");
            }

            if (this.credentialsRefreshService.isApproachingExpiration(this.credentialsProvider.getTimeBeforeExpiration())) {
               this.credentialsProvider.refresh();
               username = this.credentialsProvider.getUsername();
               password = this.credentialsProvider.getPassword();
            }
         }

         LongString challenge = null;
         LongString response = sm.handleChallenge(null, username, password);

         do {
            me.neznamy.tab.libs.com.rabbitmq.client.Method method = challenge == null
               ? new AMQP.Connection.StartOk.Builder().clientProperties(this._clientProperties).mechanism(sm.getName()).response(response).build()
               : new AMQP.Connection.SecureOk.Builder().response(response).build();

            try {
               me.neznamy.tab.libs.com.rabbitmq.client.Method serverResponse = this._channel0.rpc(method, this.handshakeTimeout / 2).getMethod();
               if (serverResponse instanceof AMQP.Connection.Tune) {
                  connTune = (AMQP.Connection.Tune)serverResponse;
               } else {
                  challenge = ((AMQP.Connection.Secure)serverResponse).getChallenge();
                  response = sm.handleChallenge(challenge, username, password);
               }
            } catch (ShutdownSignalException e) {
               me.neznamy.tab.libs.com.rabbitmq.client.Method shutdownMethod = e.getReason();
               if (shutdownMethod instanceof AMQP.Connection.Close) {
                  AMQP.Connection.Close shutdownClose = (AMQP.Connection.Close)shutdownMethod;
                  if (shutdownClose.getReplyCode() == 403) {
                     throw new AuthenticationFailureException(shutdownClose.getReplyText());
                  }
               }

               throw new PossibleAuthenticationFailureException(e);
            }
         } while (connTune == null);
      } catch (TimeoutException te) {
         this._frameHandler.close();
         throw te;
      } catch (ShutdownSignalException sse) {
         this._frameHandler.close();
         throw AMQChannel.wrap(sse);
      } catch (IOException ioe) {
         this._frameHandler.close();
         throw ioe;
      }

      try {
         int negotiatedChannelMax = this.negotiateChannelMax(this.requestedChannelMax, connTune.getChannelMax());
         int channelMax = ConnectionFactory.ensureUnsignedShort(negotiatedChannelMax);
         if (channelMax != negotiatedChannelMax) {
            LOGGER.warn("Channel max must be between 0 and {}, value has been set to {} instead of {}", new Object[]{65535, channelMax, negotiatedChannelMax});
         }

         this._channelManager = this.instantiateChannelManager(channelMax, this.threadFactory);
         int frameMax = negotiatedMaxValue(this.requestedFrameMax, connTune.getFrameMax());
         this._frameMax = frameMax;
         int negotiatedHeartbeat = negotiatedMaxValue(this.requestedHeartbeat, connTune.getHeartbeat());
         int heartbeat = ConnectionFactory.ensureUnsignedShort(negotiatedHeartbeat);
         if (heartbeat != negotiatedHeartbeat) {
            LOGGER.warn("Heartbeat must be between 0 and {}, value has been set to {} instead of {}", new Object[]{65535, heartbeat, negotiatedHeartbeat});
         }

         this.setHeartbeat(heartbeat);
         this.connectionInfo = new AMQConnection.DefaultConnectionInfo(this.getAddress(), this.getPort());
         this._channel0.transmit(new AMQP.Connection.TuneOk.Builder().channelMax(channelMax).frameMax(frameMax).heartbeat(heartbeat).build());
         this._channel0.exnWrappingRpc(new AMQP.Connection.Open.Builder().virtualHost(this._virtualHost).build());
      } catch (IOException ioe) {
         this._heartbeatSender.shutdown();
         this._frameHandler.close();
         throw ioe;
      } catch (ShutdownSignalException sse) {
         this._heartbeatSender.shutdown();
         this._frameHandler.close();
         throw AMQChannel.wrap(sse);
      }

      if (this.credentialsProvider.getTimeBeforeExpiration() != null) {
         String registrationId = this.credentialsRefreshService
            .register(
               this.credentialsProvider,
               () -> {
                  if (!this.isOpen()) {
                     return false;
                  }

                  if (this._inConnectionNegotiation) {
                     return true;
                  }

                  String refreshedPassword = this.credentialsProvider.getPassword();
                  UpdateSecretExtension.UpdateSecret updateSecret = new UpdateSecretExtension.UpdateSecret(
                     LongStringHelper.asLongString(refreshedPassword), "Refresh scheduled by client"
                  );

                  try {
                     this._channel0.rpc(updateSecret);
                  } catch (ShutdownSignalException e) {
                     LOGGER.warn("Error while trying to update secret: {}. Connection has been closed.", ex.getMessage());
                     return false;
                  }

                  return true;
               }
            );
         this.addShutdownListener(sse -> this.credentialsRefreshService.unregister(this.credentialsProvider, registrationId));
      }

      this._inConnectionNegotiation = false;
   }

   protected ChannelManager instantiateChannelManager(int channelMax, ThreadFactory threadFactory) {
      ChannelManager result = new ChannelManager(this._workService, channelMax, threadFactory, this.metricsCollector, this.observationCollector);
      this.configureChannelManager(result);
      return result;
   }

   protected void configureChannelManager(ChannelManager channelManager) {
      channelManager.setShutdownExecutor(this.shutdownExecutor);
      channelManager.setChannelShutdownTimeout((int)(this.requestedHeartbeat * 1.05 * 1000.0));
   }

   public void startMainLoop() {
      AMQConnection.MainLoop loop = new AMQConnection.MainLoop();
      String name = "AMQP Connection " + this.getHostAddress() + ":" + this.getPort();
      this.mainLoopThread = Environment.newThread(this.threadFactory, loop, name);
      this.ioLoopThread(this.mainLoopThread);
      this.mainLoopThread.start();
   }

   protected int negotiateChannelMax(int requestedChannelMax, int serverMax) {
      return negotiatedMaxValue(requestedChannelMax, serverMax);
   }

   private static void checkPreconditions() {
      AMQCommand.checkPreconditions();
   }

   @Override
   public int getChannelMax() {
      ChannelManager cm = this._channelManager;
      return cm == null ? 0 : cm.getChannelMax();
   }

   @Override
   public int getFrameMax() {
      return this._frameMax;
   }

   @Override
   public int getHeartbeat() {
      return this._heartbeat;
   }

   public void setHeartbeat(int heartbeat) {
      try {
         this._heartbeatSender.setHeartbeat(heartbeat);
         this._heartbeat = heartbeat;
         this._frameHandler.setTimeout(heartbeat * 1000 / 4);
      } catch (SocketException var3) {
      }
   }

   public void setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
   }

   public ThreadFactory getThreadFactory() {
      return this.threadFactory;
   }

   @Override
   public Map<String, Object> getClientProperties() {
      return new HashMap<>(this._clientProperties);
   }

   @Override
   public String getClientProvidedName() {
      return (String)this._clientProperties.get("connection_name");
   }

   @Override
   public ExceptionHandler getExceptionHandler() {
      return this._exceptionHandler;
   }

   public boolean willShutDownConsumerExecutor() {
      return this._workService.usesPrivateExecutor();
   }

   @Override
   public Channel createChannel(int channelNumber) throws IOException {
      this.ensureIsOpen();
      ChannelManager cm = this._channelManager;
      if (cm == null) {
         return null;
      }

      Channel channel = cm.createChannel(this, channelNumber);
      if (channel != null) {
         this.metricsCollector.newChannel(channel);
      }

      return channel;
   }

   @Override
   public Channel createChannel() throws IOException {
      this.ensureIsOpen();
      ChannelManager cm = this._channelManager;
      if (cm == null) {
         return null;
      }

      Channel channel = cm.createChannel(this);
      if (channel != null) {
         this.metricsCollector.newChannel(channel);
      }

      return channel;
   }

   public void writeFrame(Frame f) throws IOException {
      this._frameHandler.writeFrame(f);
      this._heartbeatSender.signalActivity();
   }

   public void flush() throws IOException {
      try {
         this._frameHandler.flush();
      } catch (IOException ioe) {
         this.errorOnWriteListener.handle(this, ioe);
      }
   }

   private static int negotiatedMaxValue(int clientValue, int serverValue) {
      return clientValue != 0 && serverValue != 0 ? Math.min(clientValue, serverValue) : Math.max(clientValue, serverValue);
   }

   private static boolean checkUnsignedShort(int value) {
      return value >= 0 && value <= 65535;
   }

   public boolean handleReadFrame(Frame frame) {
      if (this._running) {
         try {
            this.readFrame(frame);
            return true;
         } catch (WorkPoolFullException e) {
            throw e;
         } catch (Throwable var9) {
            Throwable ex = var9;

            try {
               this.handleFailure(ex);
            } finally {
               this.doFinalShutdown();
            }
         }
      }

      return false;
   }

   public boolean isRunning() {
      return this._running;
   }

   public boolean hasBrokerInitiatedShutdown() {
      return this._brokerInitiatedShutdown;
   }

   private void readFrame(Frame frame) throws IOException {
      if (frame != null) {
         this._missedHeartbeats = 0;
         if (frame.type != 8) {
            if (frame.channel == 0) {
               this._channel0.handleFrame(frame);
            } else if (this.isOpen()) {
               ChannelManager cm = this._channelManager;
               if (cm != null) {
                  ChannelN channel;
                  try {
                     channel = cm.getChannel(frame.channel);
                  } catch (UnknownChannelException e) {
                     LOGGER.info("Received a frame on an unknown channel, ignoring it");
                     return;
                  }

                  channel.handleFrame(frame);
               }
            }
         }
      } else {
         this.handleSocketTimeout();
      }
   }

   public void handleHeartbeatFailure() {
      Exception ex = new MissedHeartbeatException(
         "Detected missed server heartbeats, heartbeat interval: " + this._heartbeat + " seconds, RabbitMQ node hostname: " + this.getHostAddress()
      );

      try {
         this._exceptionHandler.handleUnexpectedConnectionDriverException(this, ex);
         this.shutdown(null, false, ex, true);
      } finally {
         this.doFinalShutdown();
      }
   }

   public void handleIoError(Throwable ex) {
      try {
         this.handleFailure(ex);
      } finally {
         this.doFinalShutdown();
      }
   }

   private void handleFailure(Throwable ex) {
      if (ex instanceof EOFException) {
         if (!this._brokerInitiatedShutdown) {
            this.shutdown(null, false, ex, true);
         }
      } else {
         this._exceptionHandler.handleUnexpectedConnectionDriverException(this, ex);
         this.shutdown(null, false, ex, true);
      }
   }

   public void doFinalShutdown() {
      if (this.finalShutdownStarted.compareAndSet(false, true)) {
         this._frameHandler.close();
         this._appContinuation.set(null);
         this.closeMainLoopThreadIfNecessary();
         this.notifyListeners();
         this.notifyRecoveryCanBeginListeners();
      }
   }

   private void closeMainLoopThreadIfNecessary() {
      if (this.mainLoopReadThreadNotNull() && this.notInMainLoopThread() && this.mainLoopThread.isAlive()) {
         this.mainLoopThread.interrupt();
      }
   }

   private boolean notInMainLoopThread() {
      return Thread.currentThread() != this.mainLoopThread;
   }

   private boolean mainLoopReadThreadNotNull() {
      return this.mainLoopThread != null;
   }

   private void notifyRecoveryCanBeginListeners() {
      ShutdownSignalException sse = this.getCloseReason();

      for (RecoveryCanBeginListener fn : Utility.copy(this.recoveryCanBeginListeners)) {
         fn.recoveryCanBegin(sse);
      }
   }

   public void addRecoveryCanBeginListener(RecoveryCanBeginListener fn) {
      this.recoveryCanBeginListeners.add(fn);
   }

   public void removeRecoveryCanBeginListener(RecoveryCanBeginListener fn) {
      this.recoveryCanBeginListeners.remove(fn);
   }

   private void handleSocketTimeout() throws SocketTimeoutException {
      if (this._inConnectionNegotiation) {
         throw new SocketTimeoutException("Timeout during Connection negotiation");
      }

      if (this._heartbeat != 0) {
         if (++this._missedHeartbeats > 8) {
            throw new MissedHeartbeatException("Heartbeat missing with heartbeat = " + this._heartbeat + " seconds");
         }
      }
   }

   public boolean processControlCommand(Command c) throws IOException {
      me.neznamy.tab.libs.com.rabbitmq.client.Method method = c.getMethod();
      if (this.isOpen()) {
         if (method instanceof AMQP.Connection.Close) {
            this.handleConnectionClose(c);
            return true;
         }

         if (method instanceof AMQP.Connection.Blocked) {
            AMQP.Connection.Blocked blocked = (AMQP.Connection.Blocked)method;

            try {
               for (BlockedListener l : this.blockedListeners) {
                  l.handleBlocked(blocked.getReason());
               }
            } catch (Throwable ex) {
               this.getExceptionHandler().handleBlockedListenerException(this, ex);
            }

            return true;
         } else if (method instanceof AMQP.Connection.Unblocked) {
            try {
               for (BlockedListener l : this.blockedListeners) {
                  l.handleUnblocked();
               }
            } catch (Throwable ex) {
               this.getExceptionHandler().handleBlockedListenerException(this, ex);
            }

            return true;
         } else {
            return false;
         }
      } else if (method instanceof AMQP.Connection.Close) {
         try {
            this._channel0.quiescingTransmit(new AMQP.Connection.CloseOk.Builder().build());
         } catch (IOException var6) {
         }

         return true;
      } else if (method instanceof AMQP.Connection.CloseOk) {
         this._running = false;
         return !this._channel0.isOutstandingRpc();
      } else {
         return true;
      }
   }

   public void handleConnectionClose(Command closeCommand) {
      ShutdownSignalException sse = this.shutdown(closeCommand.getMethod(), false, null, this._inConnectionNegotiation);

      try {
         this._channel0.quiescingTransmit(new AMQP.Connection.CloseOk.Builder().build());
      } catch (IOException var6) {
      }

      this._brokerInitiatedShutdown = true;
      AMQConnection.SocketCloseWait scw = new AMQConnection.SocketCloseWait(sse);
      if (this.shutdownExecutor != null) {
         this.shutdownExecutor.execute(scw);
      } else {
         String name = "RabbitMQ connection shutdown monitor " + this.getHostAddress() + ":" + this.getPort();
         Thread waiter = Environment.newThread(this.threadFactory, scw, name);
         waiter.start();
      }
   }

   public ShutdownSignalException shutdown(
      me.neznamy.tab.libs.com.rabbitmq.client.Method reason, boolean initiatedByApplication, Throwable cause, boolean notifyRpc
   ) {
      ShutdownSignalException sse = this.startShutdown(reason, initiatedByApplication, cause, notifyRpc);
      this.finishShutdown(sse);
      return sse;
   }

   private ShutdownSignalException startShutdown(
      me.neznamy.tab.libs.com.rabbitmq.client.Method reason, boolean initiatedByApplication, Throwable cause, boolean notifyRpc
   ) {
      ShutdownSignalException sse = new ShutdownSignalException(true, initiatedByApplication, reason, this);
      sse.initCause(cause);
      if (!this.setShutdownCauseIfOpen(sse) && initiatedByApplication) {
         throw new AlreadyClosedException(this.getCloseReason(), cause);
      }

      this._heartbeatSender.shutdown();
      this._channel0.processShutdownSignal(sse, !initiatedByApplication, notifyRpc);
      return sse;
   }

   private void finishShutdown(ShutdownSignalException sse) {
      ChannelManager cm = this._channelManager;
      if (cm != null) {
         cm.handleSignal(sse);
      }
   }

   @Override
   public void close() throws IOException {
      this.close(-1);
   }

   @Override
   public void close(int timeout) throws IOException {
      this.close(200, "OK", timeout);
   }

   @Override
   public void close(int closeCode, String closeMessage) throws IOException {
      this.close(closeCode, closeMessage, -1);
   }

   @Override
   public void close(int closeCode, String closeMessage, int timeout) throws IOException {
      this.close(closeCode, closeMessage, true, null, timeout, false);
   }

   @Override
   public void abort() {
      this.abort(-1);
   }

   @Override
   public void abort(int closeCode, String closeMessage) {
      this.abort(closeCode, closeMessage, -1);
   }

   @Override
   public void abort(int timeout) {
      this.abort(200, "OK", timeout);
   }

   @Override
   public void abort(int closeCode, String closeMessage, int timeout) {
      try {
         this.close(closeCode, closeMessage, true, null, timeout, true);
      } catch (IOException var5) {
      }
   }

   public void close(int closeCode, String closeMessage, boolean initiatedByApplication, Throwable cause) throws IOException {
      this.close(closeCode, closeMessage, initiatedByApplication, cause, -1, false);
   }

   public void close(int closeCode, String closeMessage, boolean initiatedByApplication, Throwable cause, int timeout, boolean abort) throws IOException {
      boolean sync = Thread.currentThread() != this.ioLoopThread;

      try {
         AMQP.Connection.Close reason = new AMQP.Connection.Close.Builder().replyCode(closeCode).replyText(closeMessage).build();
         final ShutdownSignalException sse = this.startShutdown(reason, initiatedByApplication, cause, true);
         if (sync) {
            AMQChannel.BlockingRpcContinuation<AMQCommand> k = new AMQChannel.BlockingRpcContinuation<AMQCommand>() {
               public AMQCommand transformReply(AMQCommand command) {
                  AMQConnection.this.finishShutdown(sse);
                  return command;
               }
            };
            this._channel0.quiescingRpc(reason, k);
            k.getReply(timeout);
         } else {
            this._channel0.quiescingTransmit(reason);
         }
      } catch (TimeoutException tte) {
         if (!abort) {
            ShutdownSignalException sse = new ShutdownSignalException(true, true, null, this);
            sse.initCause(cause);
            throw sse;
         }
      } catch (ShutdownSignalException sse) {
         if (!abort) {
            throw sse;
         }
      } catch (IOException ioe) {
         if (!abort) {
            throw ioe;
         }
      } finally {
         if (sync) {
            this._frameHandler.close();
         }
      }
   }

   @Override
   public String toString() {
      String virtualHost = "/".equals(this._virtualHost) ? this._virtualHost : "/" + this._virtualHost;
      return "amqp://" + this.credentialsProvider.getUsername() + "@" + this.getHostAddress() + ":" + this.getPort() + virtualHost;
   }

   private String getHostAddress() {
      return this.getAddress() == null ? null : this.getAddress().getHostAddress();
   }

   @Override
   public void addBlockedListener(BlockedListener listener) {
      this.blockedListeners.add(listener);
   }

   @Override
   public BlockedListener addBlockedListener(final BlockedCallback blockedCallback, final UnblockedCallback unblockedCallback) {
      BlockedListener blockedListener = new BlockedListener() {
         @Override
         public void handleBlocked(String reason) throws IOException {
            blockedCallback.handle(reason);
         }

         @Override
         public void handleUnblocked() throws IOException {
            unblockedCallback.handle();
         }
      };
      this.addBlockedListener(blockedListener);
      return blockedListener;
   }

   @Override
   public boolean removeBlockedListener(BlockedListener listener) {
      return this.blockedListeners.remove(listener);
   }

   @Override
   public void clearBlockedListeners() {
      this.blockedListeners.clear();
   }

   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public void setId(String id) {
      this.id = id;
   }

   public void ioLoopThread(Thread thread) {
      if (this.ioLoopThreadSet.compareAndSet(false, true)) {
         this.ioLoopThread = thread;
      }
   }

   public int getChannelRpcTimeout() {
      return this.channelRpcTimeout;
   }

   public boolean willCheckRpcResponseType() {
      return this.channelShouldCheckRpcResponseType;
   }

   public TrafficListener getTrafficListener() {
      return this.trafficListener;
   }

   int getMaxInboundMessageBodySize() {
      return this.maxInboundMessageBodySize;
   }

   ObservationCollector.ConnectionInfo connectionInfo() {
      return this.connectionInfo;
   }

   private static class DefaultConnectionInfo implements ObservationCollector.ConnectionInfo {
      private final String peerAddress;
      private final int peerPort;

      private DefaultConnectionInfo(InetAddress address, int peerPort) {
         this.peerAddress = address == null ? "" : (address.getHostAddress() == null ? "" : address.getHostAddress());
         this.peerPort = peerPort;
      }

      @Override
      public String getPeerAddress() {
         return this.peerAddress;
      }

      @Override
      public int getPeerPort() {
         return this.peerPort;
      }
   }

   private class MainLoop implements Runnable {
      private MainLoop() {
      }

      @Override
      public void run() {
         boolean shouldDoFinalShutdown = true;

         try {
            while (AMQConnection.this._running) {
               Frame frame = AMQConnection.this._frameHandler.readFrame();
               AMQConnection.this.readFrame(frame);
            }
         } catch (Throwable ex) {
            if (ex instanceof InterruptedException) {
               shouldDoFinalShutdown = false;
            } else {
               AMQConnection.this.handleFailure(ex);
            }
         } finally {
            if (shouldDoFinalShutdown) {
               AMQConnection.this.doFinalShutdown();
            }
         }
      }
   }

   private class SocketCloseWait implements Runnable {
      private final ShutdownSignalException cause;

      public SocketCloseWait(ShutdownSignalException sse) {
         this.cause = sse;
      }

      @Override
      public void run() {
         try {
            AMQConnection.this._appContinuation.get(AMQConnection.SOCKET_CLOSE_TIMEOUT);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         } catch (TimeoutException var7) {
         } finally {
            AMQConnection.this._running = false;
            AMQConnection.this._channel0.notifyOutstandingRpc(this.cause);
         }
      }
   }
}
