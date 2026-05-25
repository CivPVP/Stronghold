package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import me.neznamy.tab.libs.com.rabbitmq.client.ExceptionHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.RecoveryDelayHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslConfig;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.client.TrafficListener;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.RecoveredQueueNameSupplier;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.RetryHandler;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery.TopologyRecoveryFilter;

public class ConnectionParams {
   private CredentialsProvider credentialsProvider;
   private ExecutorService consumerWorkServiceExecutor;
   private ScheduledExecutorService heartbeatExecutor;
   private ExecutorService shutdownExecutor;
   private String virtualHost;
   private Map<String, Object> clientProperties;
   private int requestedFrameMax;
   private int requestedChannelMax;
   private int requestedHeartbeat;
   private int handshakeTimeout;
   private int shutdownTimeout;
   private SaslConfig saslConfig;
   private long networkRecoveryInterval;
   private RecoveryDelayHandler recoveryDelayHandler;
   private boolean topologyRecovery;
   private ExecutorService topologyRecoveryExecutor;
   private int channelRpcTimeout;
   private boolean channelShouldCheckRpcResponseType;
   private ErrorOnWriteListener errorOnWriteListener;
   private int workPoolTimeout = -1;
   private TopologyRecoveryFilter topologyRecoveryFilter;
   private Predicate<ShutdownSignalException> connectionRecoveryTriggeringCondition;
   private RetryHandler topologyRecoveryRetryHandler;
   private RecoveredQueueNameSupplier recoveredQueueNameSupplier;
   private ExceptionHandler exceptionHandler;
   private ThreadFactory threadFactory;
   private TrafficListener trafficListener;
   private CredentialsRefreshService credentialsRefreshService;
   private int maxInboundMessageBodySize;

   public CredentialsProvider getCredentialsProvider() {
      return this.credentialsProvider;
   }

   public ExecutorService getConsumerWorkServiceExecutor() {
      return this.consumerWorkServiceExecutor;
   }

   public String getVirtualHost() {
      return this.virtualHost;
   }

   public Map<String, Object> getClientProperties() {
      return this.clientProperties;
   }

   public int getRequestedFrameMax() {
      return this.requestedFrameMax;
   }

   public int getRequestedChannelMax() {
      return this.requestedChannelMax;
   }

   public int getRequestedHeartbeat() {
      return this.requestedHeartbeat;
   }

   public int getHandshakeTimeout() {
      return this.handshakeTimeout;
   }

   public void setHandshakeTimeout(int timeout) {
      this.handshakeTimeout = timeout;
   }

   public int getShutdownTimeout() {
      return this.shutdownTimeout;
   }

   public SaslConfig getSaslConfig() {
      return this.saslConfig;
   }

   public ExceptionHandler getExceptionHandler() {
      return this.exceptionHandler;
   }

   public long getNetworkRecoveryInterval() {
      return this.networkRecoveryInterval;
   }

   public RecoveryDelayHandler getRecoveryDelayHandler() {
      return this.recoveryDelayHandler == null ? new RecoveryDelayHandler.DefaultRecoveryDelayHandler(this.networkRecoveryInterval) : this.recoveryDelayHandler;
   }

   public boolean isTopologyRecoveryEnabled() {
      return this.topologyRecovery;
   }

   public ExecutorService getTopologyRecoveryExecutor() {
      return this.topologyRecoveryExecutor;
   }

   public ThreadFactory getThreadFactory() {
      return this.threadFactory;
   }

   public int getChannelRpcTimeout() {
      return this.channelRpcTimeout;
   }

   public boolean channelShouldCheckRpcResponseType() {
      return this.channelShouldCheckRpcResponseType;
   }

   public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
      this.credentialsProvider = credentialsProvider;
   }

   public void setConsumerWorkServiceExecutor(ExecutorService consumerWorkServiceExecutor) {
      this.consumerWorkServiceExecutor = consumerWorkServiceExecutor;
   }

   public void setVirtualHost(String virtualHost) {
      this.virtualHost = virtualHost;
   }

   public void setClientProperties(Map<String, Object> clientProperties) {
      this.clientProperties = clientProperties;
   }

   public void setRequestedFrameMax(int requestedFrameMax) {
      this.requestedFrameMax = requestedFrameMax;
   }

   public void setRequestedChannelMax(int requestedChannelMax) {
      this.requestedChannelMax = requestedChannelMax;
   }

   public void setRequestedHeartbeat(int requestedHeartbeat) {
      this.requestedHeartbeat = requestedHeartbeat;
   }

   public void setShutdownTimeout(int shutdownTimeout) {
      this.shutdownTimeout = shutdownTimeout;
   }

   public void setSaslConfig(SaslConfig saslConfig) {
      this.saslConfig = saslConfig;
   }

   public void setNetworkRecoveryInterval(long networkRecoveryInterval) {
      this.networkRecoveryInterval = networkRecoveryInterval;
   }

   public void setRecoveryDelayHandler(RecoveryDelayHandler recoveryDelayHandler) {
      this.recoveryDelayHandler = recoveryDelayHandler;
   }

   public void setTopologyRecovery(boolean topologyRecovery) {
      this.topologyRecovery = topologyRecovery;
   }

   public void setTopologyRecoveryExecutor(ExecutorService topologyRecoveryExecutor) {
      this.topologyRecoveryExecutor = topologyRecoveryExecutor;
   }

   public void setExceptionHandler(ExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
   }

   public void setThreadFactory(ThreadFactory threadFactory) {
      this.threadFactory = threadFactory;
   }

   public ExecutorService getShutdownExecutor() {
      return this.shutdownExecutor;
   }

   public void setShutdownExecutor(ExecutorService shutdownExecutor) {
      this.shutdownExecutor = shutdownExecutor;
   }

   public ScheduledExecutorService getHeartbeatExecutor() {
      return this.heartbeatExecutor;
   }

   public void setHeartbeatExecutor(ScheduledExecutorService heartbeatExecutor) {
      this.heartbeatExecutor = heartbeatExecutor;
   }

   public void setChannelRpcTimeout(int channelRpcTimeout) {
      this.channelRpcTimeout = channelRpcTimeout;
   }

   public void setChannelShouldCheckRpcResponseType(boolean channelShouldCheckRpcResponseType) {
      this.channelShouldCheckRpcResponseType = channelShouldCheckRpcResponseType;
   }

   public void setErrorOnWriteListener(ErrorOnWriteListener errorOnWriteListener) {
      this.errorOnWriteListener = errorOnWriteListener;
   }

   public ErrorOnWriteListener getErrorOnWriteListener() {
      return this.errorOnWriteListener;
   }

   public void setWorkPoolTimeout(int workPoolTimeout) {
      this.workPoolTimeout = workPoolTimeout;
   }

   public int getWorkPoolTimeout() {
      return this.workPoolTimeout;
   }

   public void setTopologyRecoveryFilter(TopologyRecoveryFilter topologyRecoveryFilter) {
      this.topologyRecoveryFilter = topologyRecoveryFilter;
   }

   public TopologyRecoveryFilter getTopologyRecoveryFilter() {
      return this.topologyRecoveryFilter;
   }

   public void setConnectionRecoveryTriggeringCondition(Predicate<ShutdownSignalException> connectionRecoveryTriggeringCondition) {
      this.connectionRecoveryTriggeringCondition = connectionRecoveryTriggeringCondition;
   }

   public Predicate<ShutdownSignalException> getConnectionRecoveryTriggeringCondition() {
      return this.connectionRecoveryTriggeringCondition;
   }

   public void setTopologyRecoveryRetryHandler(RetryHandler topologyRecoveryRetryHandler) {
      this.topologyRecoveryRetryHandler = topologyRecoveryRetryHandler;
   }

   public RetryHandler getTopologyRecoveryRetryHandler() {
      return this.topologyRecoveryRetryHandler;
   }

   public void setRecoveredQueueNameSupplier(RecoveredQueueNameSupplier recoveredQueueNameSupplier) {
      this.recoveredQueueNameSupplier = recoveredQueueNameSupplier;
   }

   public RecoveredQueueNameSupplier getRecoveredQueueNameSupplier() {
      return this.recoveredQueueNameSupplier;
   }

   public void setTrafficListener(TrafficListener trafficListener) {
      this.trafficListener = trafficListener;
   }

   public TrafficListener getTrafficListener() {
      return this.trafficListener;
   }

   public void setCredentialsRefreshService(CredentialsRefreshService credentialsRefreshService) {
      this.credentialsRefreshService = credentialsRefreshService;
   }

   public CredentialsRefreshService getCredentialsRefreshService() {
      return this.credentialsRefreshService;
   }

   public int getMaxInboundMessageBodySize() {
      return this.maxInboundMessageBodySize;
   }

   public void setMaxInboundMessageBodySize(int maxInboundMessageBodySize) {
      this.maxInboundMessageBodySize = maxInboundMessageBodySize;
   }
}
