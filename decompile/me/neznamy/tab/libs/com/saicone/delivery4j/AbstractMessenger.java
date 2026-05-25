package me.neznamy.tab.libs.com.saicone.delivery4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import me.neznamy.tab.libs.com.saicone.delivery4j.util.ByteCodec;
import me.neznamy.tab.libs.com.saicone.delivery4j.util.DelayedExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMessenger {
   private Executor executor = CompletableFuture.completedFuture(null).defaultExecutor();
   private Broker broker;
   private final Map<String, MessageChannel> channels = new HashMap<>();

   public boolean isEnabled() {
      return this.broker != null && this.broker.isEnabled();
   }

   @NotNull
   public Executor getExecutor() {
      return this.executor;
   }

   @Nullable
   public Broker getBroker() {
      return this.broker;
   }

   @NotNull
   public Map<String, MessageChannel> getChannels() {
      return this.channels;
   }

   public void setExecutor(@NotNull Executor executor) {
      this.executor = executor;
   }

   public void setBroker(@Nullable Broker broker) {
      this.broker = broker;
   }

   @NotNull
   protected Broker loadBroker() {
      if (this.getBroker() != null) {
         return this.getBroker();
      } else {
         throw new IllegalStateException("Override loadBroker to load a broker or provide one on start messenger instance");
      }
   }

   public void start() {
      this.start(this.loadBroker());
   }

   public void start(@NotNull Broker broker) {
      this.close();
      if (this instanceof Executor) {
         this.executor = (Executor)this;
      }

      broker.getSubscribedChannels().addAll(this.getChannels().keySet());
      broker.setConsumer(this::accept);
      if (this instanceof ByteCodec) {
         try {
            broker.setCodec((ByteCodec<String>)this);
         } catch (Throwable var3) {
         }
      }

      if (this instanceof DelayedExecutor) {
         broker.setExecutor((DelayedExecutor<?>)this);
      }

      if (this instanceof Broker.Logger) {
         broker.setLogger((Broker.Logger)this);
      }

      this.broker = broker;
      this.broker.start();
   }

   public void close() {
      if (this.broker != null) {
         this.broker.close();
      }
   }

   public void clear() {
      if (this.broker != null) {
         this.broker.clear();
      }

      for (Entry<String, MessageChannel> entry : this.channels.entrySet()) {
         entry.getValue().clear();
      }

      this.channels.clear();
   }

   @NotNull
   public MessageChannel subscribe(@NotNull String channel) {
      MessageChannel messageChannel = this.channels.get(channel);
      if (messageChannel == null) {
         messageChannel = new MessageChannel(channel);
         this.channels.put(channel, messageChannel);
      }

      if (this.broker != null) {
         this.broker.subscribe(channel);
      }

      return messageChannel;
   }

   @Nullable
   public MessageChannel subscribe(@NotNull MessageChannel channel) {
      if (this.broker != null) {
         this.broker.subscribe(channel.getName());
      }

      return this.channels.put(channel.getName(), channel);
   }

   @NotNull
   public CompletableFuture<Void> send(@NotNull String channel, @Nullable Object... lines) {
      if (!this.isEnabled()) {
         throw new IllegalStateException("The messenger is not enabled");
      } else {
         MessageChannel messageChannel = this.channels.get(channel);
         if (messageChannel == null) {
            throw new IllegalStateException("The messaging chanel '" + channel + "' doesn't exist");
         } else {
            return CompletableFuture.supplyAsync(() -> {
               try {
                  this.broker.send(channel, messageChannel.encode(lines));
                  return null;
               } catch (IOException e) {
                  throw new CompletionException(e);
               }
            }, this.executor);
         }
      }
   }

   public boolean accept(@NotNull String channel, byte[] src) throws IOException {
      MessageChannel messageChannel = this.channels.get(channel);
      if (messageChannel == null) {
         throw new IllegalStateException("The messaging chanel '" + channel + "' doesn't exist");
      } else {
         return messageChannel.accept(src);
      }
   }
}
