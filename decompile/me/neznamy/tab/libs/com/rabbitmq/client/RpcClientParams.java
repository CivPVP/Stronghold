package me.neznamy.tab.libs.com.rabbitmq.client;

import java.util.function.Function;
import java.util.function.Supplier;

public class RpcClientParams {
   private Channel channel;
   private String exchange;
   private String routingKey;
   private String replyTo = "amq.rabbitmq.reply-to";
   private int timeout = -1;
   private boolean useMandatory = false;
   private Function<Object, RpcClient.Response> replyHandler = RpcClient.DEFAULT_REPLY_HANDLER;
   private Supplier<String> correlationIdSupplier = RpcClient.incrementingCorrelationIdSupplier();

   public Channel getChannel() {
      return this.channel;
   }

   public RpcClientParams channel(Channel channel) {
      this.channel = channel;
      return this;
   }

   public String getExchange() {
      return this.exchange;
   }

   public RpcClientParams exchange(String exchange) {
      this.exchange = exchange;
      return this;
   }

   public String getRoutingKey() {
      return this.routingKey;
   }

   public RpcClientParams routingKey(String routingKey) {
      this.routingKey = routingKey;
      return this;
   }

   public String getReplyTo() {
      return this.replyTo;
   }

   public RpcClientParams replyTo(String replyTo) {
      this.replyTo = replyTo;
      return this;
   }

   public int getTimeout() {
      return this.timeout;
   }

   public RpcClientParams timeout(int timeout) {
      this.timeout = timeout;
      return this;
   }

   public RpcClientParams useMandatory(boolean useMandatory) {
      this.useMandatory = useMandatory;
      return this;
   }

   public RpcClientParams useMandatory() {
      return this.useMandatory(true);
   }

   public boolean shouldUseMandatory() {
      return this.useMandatory;
   }

   public RpcClientParams correlationIdSupplier(Supplier<String> correlationIdGenerator) {
      this.correlationIdSupplier = correlationIdGenerator;
      return this;
   }

   public Supplier<String> getCorrelationIdSupplier() {
      return this.correlationIdSupplier;
   }

   public Function<Object, RpcClient.Response> getReplyHandler() {
      return this.replyHandler;
   }

   public RpcClientParams replyHandler(Function<Object, RpcClient.Response> replyHandler) {
      this.replyHandler = replyHandler;
      return this;
   }
}
