package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.MethodArgumentReader;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.MethodArgumentWriter;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ValueReader;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ValueWriter;
import me.neznamy.tab.libs.com.rabbitmq.utility.BlockingCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient implements AutoCloseable {
   private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
   private final Channel _channel;
   private final String _exchange;
   private final String _routingKey;
   private final String _replyTo;
   private final int _timeout;
   protected static final int NO_TIMEOUT = -1;
   private final boolean _useMandatory;
   private final AtomicBoolean closed = new AtomicBoolean(false);
   public static final Function<Object, RpcClient.Response> DEFAULT_REPLY_HANDLER = reply -> {
      if (reply instanceof ShutdownSignalException) {
         ShutdownSignalException sig = (ShutdownSignalException)reply;
         ShutdownSignalException wrapper = new ShutdownSignalException(sig.isHardError(), sig.isInitiatedByApplication(), sig.getReason(), sig.getReference());
         wrapper.initCause(sig);
         throw wrapper;
      } else if (reply instanceof UnroutableRpcRequestException) {
         throw (UnroutableRpcRequestException)reply;
      } else {
         return (RpcClient.Response)reply;
      }
   };
   private final Function<Object, RpcClient.Response> _replyHandler;
   private final Map<String, BlockingCell<Object>> _continuationMap = new HashMap<>();
   private final Supplier<String> _correlationIdSupplier;
   private final ReturnListener _returnListener;
   private String lastCorrelationId = "0";
   private final DefaultConsumer _consumer;

   public RpcClient(RpcClientParams params) throws IOException {
      this._channel = params.getChannel();
      this._exchange = params.getExchange();
      this._routingKey = params.getRoutingKey();
      this._replyTo = params.getReplyTo();
      if (params.getTimeout() < -1) {
         throw new IllegalArgumentException("Timeout argument must be NO_TIMEOUT(-1) or non-negative.");
      }

      this._timeout = params.getTimeout();
      this._useMandatory = params.shouldUseMandatory();
      this._replyHandler = params.getReplyHandler();
      this._correlationIdSupplier = params.getCorrelationIdSupplier();
      this._consumer = this.setupConsumer();
      if (this._useMandatory) {
         this._returnListener = this._channel.addReturnListener(returnMessage -> {
            synchronized (this._continuationMap) {
               String replyId = returnMessage.getProperties().getCorrelationId();
               BlockingCell<Object> blocker = this._continuationMap.remove(replyId);
               if (blocker == null) {
                  LOGGER.warn("No outstanding request for correlation ID {}", replyId);
               } else {
                  blocker.set(new UnroutableRpcRequestException(returnMessage));
               }
            }
         });
      } else {
         this._returnListener = null;
      }
   }

   @Deprecated
   public RpcClient(Channel channel, String exchange, String routingKey, String replyTo, int timeout) throws IOException {
      this(new RpcClientParams().channel(channel).exchange(exchange).routingKey(routingKey).replyTo(replyTo).timeout(timeout).useMandatory(false));
   }

   @Deprecated
   public RpcClient(Channel channel, String exchange, String routingKey, String replyTo) throws IOException {
      this(channel, exchange, routingKey, replyTo, -1);
   }

   @Deprecated
   public RpcClient(Channel channel, String exchange, String routingKey) throws IOException {
      this(channel, exchange, routingKey, "amq.rabbitmq.reply-to", -1);
   }

   @Deprecated
   public RpcClient(Channel channel, String exchange, String routingKey, int timeout) throws IOException {
      this(channel, exchange, routingKey, "amq.rabbitmq.reply-to", timeout);
   }

   private void checkNotClosed() throws IOException {
      if (this.closed.get()) {
         throw new EOFException("RpcClient is closed");
      }
   }

   @Override
   public void close() throws IOException {
      if (this.closed.compareAndSet(false, true)) {
         this._channel.basicCancel(this._consumer.getConsumerTag());
         if (this._returnListener != null) {
            this._channel.removeReturnListener(this._returnListener);
         }
      }
   }

   protected DefaultConsumer setupConsumer() throws IOException {
      DefaultConsumer consumer = new DefaultConsumer(this._channel) {
         @Override
         public void handleShutdownSignal(String consumerTag, ShutdownSignalException signal) {
            synchronized (RpcClient.this._continuationMap) {
               for (Entry<String, BlockingCell<Object>> entry : RpcClient.this._continuationMap.entrySet()) {
                  entry.getValue().set(signal);
               }

               RpcClient.this.closed.set(true);
            }
         }

         @Override
         public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            synchronized (RpcClient.this._continuationMap) {
               String replyId = properties.getCorrelationId();
               BlockingCell<Object> blocker = RpcClient.this._continuationMap.remove(replyId);
               if (blocker == null) {
                  RpcClient.LOGGER.warn("No outstanding request for correlation ID {}", replyId);
               } else {
                  blocker.set(new RpcClient.Response(consumerTag, envelope, properties, body));
               }
            }
         }
      };
      this._channel.basicConsume(this._replyTo, true, consumer);
      return consumer;
   }

   public void publish(AMQP.BasicProperties props, byte[] message) throws IOException {
      this._channel.basicPublish(this._exchange, this._routingKey, this._useMandatory, props, message);
   }

   public RpcClient.Response doCall(AMQP.BasicProperties props, byte[] message) throws IOException, TimeoutException {
      return this.doCall(props, message, this._timeout);
   }

   public RpcClient.Response doCall(AMQP.BasicProperties props, byte[] message, int timeout) throws IOException, ShutdownSignalException, TimeoutException {
      this.checkNotClosed();
      BlockingCell<Object> k = new BlockingCell<>();
      String replyId;
      synchronized (this._continuationMap) {
         replyId = this._correlationIdSupplier.get();
         this.lastCorrelationId = replyId;
         props = (props == null ? new AMQP.BasicProperties.Builder() : props.builder()).correlationId(replyId).replyTo(this._replyTo).build();
         this._continuationMap.put(replyId, k);
      }

      this.publish(props, message);

      Object reply;
      try {
         reply = k.uninterruptibleGet(timeout);
      } catch (TimeoutException ex) {
         this._continuationMap.remove(replyId);
         throw ex;
      }

      return this._replyHandler.apply(reply);
   }

   public byte[] primitiveCall(AMQP.BasicProperties props, byte[] message) throws IOException, ShutdownSignalException, TimeoutException {
      return this.primitiveCall(props, message, this._timeout);
   }

   public byte[] primitiveCall(AMQP.BasicProperties props, byte[] message, int timeout) throws IOException, ShutdownSignalException, TimeoutException {
      return this.doCall(props, message, timeout).getBody();
   }

   public byte[] primitiveCall(byte[] message) throws IOException, ShutdownSignalException, TimeoutException {
      return this.primitiveCall(null, message);
   }

   public RpcClient.Response responseCall(byte[] message) throws IOException, ShutdownSignalException, TimeoutException {
      return this.responseCall(message, this._timeout);
   }

   public RpcClient.Response responseCall(byte[] message, int timeout) throws IOException, ShutdownSignalException, TimeoutException {
      return this.doCall(null, message, timeout);
   }

   public String stringCall(String message) throws IOException, ShutdownSignalException, TimeoutException {
      byte[] request;
      try {
         request = message.getBytes("UTF-8");
      } catch (IOException _e) {
         request = message.getBytes();
      }

      byte[] reply = this.primitiveCall(request);

      try {
         return new String(reply, "UTF-8");
      } catch (IOException _e) {
         return new String(reply);
      }
   }

   public Map<String, Object> mapCall(Map<String, Object> message) throws IOException, ShutdownSignalException, TimeoutException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      MethodArgumentWriter writer = new MethodArgumentWriter(new ValueWriter(new DataOutputStream(buffer)));
      writer.writeTable(message);
      writer.flush();
      byte[] reply = this.primitiveCall(buffer.toByteArray());
      MethodArgumentReader reader = new MethodArgumentReader(new ValueReader(new DataInputStream(new ByteArrayInputStream(reply))));
      return reader.readTable();
   }

   public Map<String, Object> mapCall(Object[] keyValuePairs) throws IOException, ShutdownSignalException, TimeoutException {
      Map<String, Object> message = new HashMap<>();

      for (int i = 0; i < keyValuePairs.length; i += 2) {
         message.put((String)keyValuePairs[i], keyValuePairs[i + 1]);
      }

      return this.mapCall(message);
   }

   public Channel getChannel() {
      return this._channel;
   }

   public String getExchange() {
      return this._exchange;
   }

   public String getRoutingKey() {
      return this._routingKey;
   }

   public Map<String, BlockingCell<Object>> getContinuationMap() {
      return this._continuationMap;
   }

   public int getCorrelationId() {
      return Integer.valueOf(this.lastCorrelationId);
   }

   public Consumer getConsumer() {
      return this._consumer;
   }

   public static Supplier<String> incrementingCorrelationIdSupplier() {
      return incrementingCorrelationIdSupplier("");
   }

   public static Supplier<String> incrementingCorrelationIdSupplier(String prefix) {
      return new RpcClient.IncrementingCorrelationIdSupplier(prefix);
   }

   private static class IncrementingCorrelationIdSupplier implements Supplier<String> {
      private final String prefix;
      private int correlationId;

      public IncrementingCorrelationIdSupplier(String prefix) {
         this.prefix = prefix;
      }

      public String get() {
         return this.prefix + ++this.correlationId;
      }
   }

   public static class Response {
      protected String consumerTag;
      protected Envelope envelope;
      protected AMQP.BasicProperties properties;
      protected byte[] body;

      public Response() {
      }

      public Response(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
         this.consumerTag = consumerTag;
         this.envelope = envelope;
         this.properties = properties;
         this.body = body;
      }

      public String getConsumerTag() {
         return this.consumerTag;
      }

      public Envelope getEnvelope() {
         return this.envelope;
      }

      public AMQP.BasicProperties getProperties() {
         return this.properties;
      }

      public byte[] getBody() {
         return this.body;
      }
   }
}
