package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import me.neznamy.tab.libs.com.rabbitmq.utility.Utility;

public class RpcServer {
   private final Channel _channel;
   private final String _queueName;
   private volatile boolean _mainloopRunning = true;
   private RpcServer.RpcConsumer _consumer;

   public RpcServer(Channel channel) throws IOException {
      this(channel, null);
   }

   public RpcServer(Channel channel, String queueName) throws IOException {
      this._channel = channel;
      if (queueName != null && !queueName.equals("")) {
         this._queueName = queueName;
      } else {
         this._queueName = this._channel.queueDeclare().getQueue();
      }

      this._consumer = this.setupConsumer();
   }

   public void close() throws IOException {
      if (this._consumer != null) {
         this._channel.basicCancel(this._consumer.getConsumerTag());
         this._consumer = null;
      }

      this.terminateMainloop();
   }

   protected RpcServer.RpcConsumer setupConsumer() throws IOException {
      RpcServer.RpcConsumer consumer = new RpcServer.DefaultRpcConsumer(this._channel);
      this._channel.basicConsume(this._queueName, consumer);
      return consumer;
   }

   public ShutdownSignalException mainloop() throws IOException {
      try {
         while (this._mainloopRunning) {
            Delivery request;
            try {
               request = this._consumer.nextDelivery();
            } catch (InterruptedException ie) {
               Thread.currentThread().interrupt();
               this._mainloopRunning = false;
               continue;
            }

            this.processRequest(request);
            this._channel.basicAck(request.getEnvelope().getDeliveryTag(), false);
         }

         return null;
      } catch (ShutdownSignalException sse) {
         return sse;
      }
   }

   public void terminateMainloop() {
      this._mainloopRunning = false;
   }

   public void processRequest(Delivery request) throws IOException {
      AMQP.BasicProperties requestProperties = request.getProperties();
      String correlationId = requestProperties.getCorrelationId();
      String replyTo = requestProperties.getReplyTo();
      if (correlationId != null && replyTo != null) {
         AMQP.BasicProperties.Builder replyPropertiesBuilder = new AMQP.BasicProperties.Builder().correlationId(correlationId);
         AMQP.BasicProperties replyProperties = this.preprocessReplyProperties(request, replyPropertiesBuilder);
         byte[] replyBody = this.handleCall(request, replyProperties);
         replyProperties = this.postprocessReplyProperties(request, replyProperties.builder());
         this._channel.basicPublish("", replyTo, replyProperties, replyBody);
      } else {
         this.handleCast(request);
      }
   }

   public byte[] handleCall(Delivery request, AMQP.BasicProperties replyProperties) {
      return this.handleCall(request.getProperties(), request.getBody(), replyProperties);
   }

   public byte[] handleCall(AMQP.BasicProperties requestProperties, byte[] requestBody, AMQP.BasicProperties replyProperties) {
      return this.handleCall(requestBody, replyProperties);
   }

   public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
      return new byte[0];
   }

   protected AMQP.BasicProperties preprocessReplyProperties(Delivery request, AMQP.BasicProperties.Builder builder) {
      return builder.build();
   }

   protected AMQP.BasicProperties postprocessReplyProperties(Delivery request, AMQP.BasicProperties.Builder builder) {
      return builder.build();
   }

   public void handleCast(Delivery request) {
      this.handleCast(request.getProperties(), request.getBody());
   }

   public void handleCast(AMQP.BasicProperties requestProperties, byte[] requestBody) {
      this.handleCast(requestBody);
   }

   public void handleCast(byte[] requestBody) {
   }

   public Channel getChannel() {
      return this._channel;
   }

   public String getQueueName() {
      return this._queueName;
   }

   private static class DefaultRpcConsumer extends DefaultConsumer implements RpcServer.RpcConsumer {
      private static final Delivery POISON = new Delivery(null, null, null);
      private final BlockingQueue<Delivery> _queue;
      private volatile ShutdownSignalException _shutdown;
      private volatile ConsumerCancelledException _cancelled;

      public DefaultRpcConsumer(Channel ch) {
         this(ch, new LinkedBlockingQueue<>());
      }

      public DefaultRpcConsumer(Channel ch, BlockingQueue<Delivery> q) {
         super(ch);
         this._queue = q;
      }

      @Override
      public Delivery nextDelivery() throws InterruptedException, ShutdownSignalException, ConsumerCancelledException {
         return this.handle(this._queue.take());
      }

      @Override
      public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
         this._shutdown = sig;
         this._queue.add(POISON);
      }

      @Override
      public void handleCancel(String consumerTag) throws IOException {
         this._cancelled = new ConsumerCancelledException();
         this._queue.add(POISON);
      }

      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
         this.checkShutdown();
         this._queue.add(new Delivery(envelope, properties, body));
      }

      private void checkShutdown() {
         if (this._shutdown != null) {
            throw (ShutdownSignalException)Utility.fixStackTrace(this._shutdown);
         }
      }

      private Delivery handle(Delivery delivery) {
         if (delivery == POISON || delivery == null && (this._shutdown != null || this._cancelled != null)) {
            if (delivery == POISON) {
               this._queue.add(POISON);
               if (this._shutdown == null && this._cancelled == null) {
                  throw new IllegalStateException("POISON in queue, but null _shutdown and null _cancelled. This should never happen, please report as a BUG");
               }
            }

            if (null != this._shutdown) {
               throw (ShutdownSignalException)Utility.fixStackTrace(this._shutdown);
            }

            if (null != this._cancelled) {
               throw (ConsumerCancelledException)Utility.fixStackTrace(this._cancelled);
            }
         }

         return delivery;
      }
   }

   public interface RpcConsumer extends Consumer {
      Delivery nextDelivery() throws InterruptedException, ShutdownSignalException, ConsumerCancelledException;

      String getConsumerTag();
   }
}
