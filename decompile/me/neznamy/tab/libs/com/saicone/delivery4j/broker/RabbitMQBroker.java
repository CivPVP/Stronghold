package me.neznamy.tab.libs.com.saicone.delivery4j.broker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.BuiltinExchangeType;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.Connection;
import me.neznamy.tab.libs.com.rabbitmq.client.ConnectionFactory;
import me.neznamy.tab.libs.com.saicone.delivery4j.Broker;
import org.jetbrains.annotations.NotNull;

public class RabbitMQBroker extends Broker {
   private final Connection connection;
   private final String exchange;
   private long checkTime = 8L;
   private TimeUnit checkUnit = TimeUnit.SECONDS;
   private Channel cChannel = null;
   private String queue = null;
   private Object aliveTask = null;
   private boolean reconnected = false;

   @NotNull
   public static RabbitMQBroker of(@NotNull String url, @NotNull String exchange) {
      try {
         return of(new URI(url), exchange);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @NotNull
   public static RabbitMQBroker of(@NotNull URI uri, @NotNull String exchange) {
      ConnectionFactory factory = new ConnectionFactory();

      try {
         factory.setUri(uri);
         return new RabbitMQBroker(factory.newConnection(), exchange);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @NotNull
   public static RabbitMQBroker of(
      @NotNull String host, int port, @NotNull String username, @NotNull String password, @NotNull String virtualHost, @NotNull String exchange
   ) {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(host);
      factory.setPort(port);
      factory.setUsername(username);
      factory.setPassword(password);
      factory.setVirtualHost(virtualHost);

      try {
         return new RabbitMQBroker(factory.newConnection(), exchange);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public RabbitMQBroker(@NotNull Connection connection, @NotNull String exchange) {
      this.connection = connection;
      this.exchange = exchange;
   }

   @Override
   protected void onStart() {
      try {
         this.cChannel = this.connection.createChannel();
         this.queue = this.cChannel.queueDeclare("", false, true, true, null).getQueue();
         this.cChannel.exchangeDeclare(this.exchange, BuiltinExchangeType.TOPIC, false, true, null);

         for (String channel : this.getSubscribedChannels()) {
            this.cChannel.queueBind(this.queue, this.exchange, channel);
         }

         this.cChannel.basicConsume(this.queue, true, (consumerTag, message) -> {
            String channelx = message.getEnvelope().getRoutingKey();
            if (this.getSubscribedChannels().contains(channelx)) {
               this.receive(channelx, message.getBody());
            }
         }, __ -> {});
         if (this.reconnected) {
            this.getLogger().log(3, "RabbitMQ connection is alive again");
            this.reconnected = false;
         }

         this.setEnabled(true);
      } catch (Throwable t) {
         this.getLogger().log(1, "Cannot start RabbitMQ connection", t);
         return;
      }

      if (this.aliveTask == null) {
         this.aliveTask = this.getExecutor().execute(this::alive, this.checkTime, this.checkTime, this.checkUnit);
      }
   }

   @Override
   protected void onClose() {
      this.close(this.cChannel, this.connection);
      this.cChannel = null;
      if (this.aliveTask != null) {
         this.getExecutor().cancel(this.aliveTask);
         this.aliveTask = null;
      }
   }

   @Override
   protected void onSubscribe(@NotNull String... channels) {
      for (String channel : channels) {
         try {
            this.cChannel.queueBind(this.queue, this.exchange, channel);
         } catch (IOException e) {
            this.getLogger().log(1, "Cannot subscribe to channel '" + channel + "'", e);
         }
      }
   }

   @Override
   protected void onUnsubscribe(@NotNull String... channels) {
      for (String channel : channels) {
         try {
            this.cChannel.queueUnbind(this.queue, this.exchange, channel);
         } catch (IOException e) {
            this.getLogger().log(1, "Cannot unsubscribe from channel '" + channel + "'", e);
         }
      }
   }

   @Override
   protected void onSend(@NotNull String channel, byte[] data) throws IOException {
      if (this.cChannel != null) {
         try {
            this.cChannel.basicPublish(this.exchange, channel, new AMQP.BasicProperties.Builder().build(), data);
         } catch (Throwable t) {
            throw new IOException(t);
         }
      }
   }

   public void setReconnectionInterval(int time, @NotNull TimeUnit unit) {
      this.checkTime = time;
      this.checkUnit = unit;
   }

   @NotNull
   public Connection getConnection() {
      return this.connection;
   }

   private void alive() {
      if (this.isEnabled()) {
         if (!this.connection.isOpen() || this.cChannel == null || !this.cChannel.isOpen()) {
            this.close(this.cChannel);
            this.cChannel = null;
            this.reconnected = true;
            this.getLogger()
               .log(2, () -> "RabbitMQ connection dropped, automatic reconnection every " + this.checkTime + " " + this.checkUnit.name().toLowerCase() + "...");
            this.onStart();
         }
      }
   }

   private void close(AutoCloseable... closeables) {
      try {
         for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
               closeable.close();
            }
         }
      } catch (Throwable var6) {
      }
   }
}
