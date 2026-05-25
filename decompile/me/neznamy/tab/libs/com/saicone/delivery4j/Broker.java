package me.neznamy.tab.libs.com.saicone.delivery4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import me.neznamy.tab.libs.com.saicone.delivery4j.util.ByteCodec;
import me.neznamy.tab.libs.com.saicone.delivery4j.util.DelayedExecutor;
import org.jetbrains.annotations.NotNull;

public abstract class Broker {
   private ChannelConsumer<byte[]> consumer = (channel, data) -> {};
   private ByteCodec<String> codec = ByteCodec.BASE64;
   private DelayedExecutor<?> executor = DelayedExecutor.JAVA;
   private Broker.Logger logger = Broker.Logger.of(this.getClass());
   private final Set<String> subscribedChannels = new HashSet<>();
   private boolean enabled = false;

   protected void onStart() {
   }

   protected void onClose() {
   }

   protected void onSubscribe(@NotNull String... channels) {
   }

   protected void onUnsubscribe(@NotNull String... channels) {
   }

   protected abstract void onSend(@NotNull String var1, byte[] var2) throws IOException;

   protected void onReceive(@NotNull String channel, byte[] data) throws IOException {
   }

   @NotNull
   public ChannelConsumer<byte[]> getConsumer() {
      return this.consumer;
   }

   @NotNull
   public ByteCodec<String> getCodec() {
      return this.codec;
   }

   @NotNull
   public DelayedExecutor<Object> getExecutor() {
      return (DelayedExecutor<Object>)this.executor;
   }

   @NotNull
   public Broker.Logger getLogger() {
      return this.logger;
   }

   @NotNull
   public Set<String> getSubscribedChannels() {
      return this.subscribedChannels;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setConsumer(@NotNull ChannelConsumer<byte[]> consumer) {
      this.consumer = consumer;
   }

   public void setCodec(@NotNull ByteCodec<String> codec) {
      this.codec = codec;
   }

   public void setExecutor(@NotNull DelayedExecutor<?> executor) {
      this.executor = executor;
   }

   public void setLogger(@NotNull Broker.Logger logger) {
      this.logger = logger;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void start() {
      this.close();
      this.onStart();
   }

   public void close() {
      if (this.isEnabled()) {
         this.setEnabled(false);
         this.onClose();
      }
   }

   public void clear() {
      this.getSubscribedChannels().clear();
   }

   public boolean subscribe(@NotNull String... channels) {
      Set<String> list = new HashSet<>();

      for (String channel : channels) {
         if (this.getSubscribedChannels().add(channel)) {
            list.add(channel);
         }
      }

      if (list.isEmpty()) {
         return false;
      }

      this.onSubscribe(list.toArray(new String[0]));
      return true;
   }

   public boolean unsubscribe(@NotNull String... channels) {
      Set<String> list = new HashSet<>();

      for (String channel : channels) {
         if (this.getSubscribedChannels().remove(channel)) {
            list.add(channel);
         }
      }

      if (list.isEmpty()) {
         return false;
      }

      this.onUnsubscribe(list.toArray(new String[0]));
      return true;
   }

   public void send(@NotNull String channel, byte[] data) throws IOException {
      this.onSend(channel, data);
   }

   public void receive(@NotNull String channel, byte[] data) throws IOException {
      this.getConsumer().accept(channel, data);
      this.onReceive(channel, data);
   }

   public interface Logger {
      boolean DEBUG = "true".equals(System.getProperty("saicone.delivery4j.debug"));

      @NotNull
      static Broker.Logger of(@NotNull final Class<?> clazz) {
         try {
            Class.forName("org.apache.logging.log4j.Logger");
            return Class.forName("me.neznamy.tab.libs.com.saicone.delivery4j.log.Log4jLogger")
               .asSubclass(Broker.Logger.class)
               .getDeclaredConstructor(Class.class)
               .newInstance(clazz);
         } catch (Throwable var3) {
            try {
               Class.forName("org.slf4j.Logger");
               return Class.forName("me.neznamy.tab.libs.com.saicone.delivery4j.log.Slf4jLogger")
                  .asSubclass(Broker.Logger.class)
                  .getDeclaredConstructor(Class.class)
                  .newInstance(clazz);
            } catch (Throwable var2) {
               return new Broker.Logger() {
                  private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(clazz.getName());

                  private void log(int level, @NotNull Consumer<Level> consumer) {
                     switch (level) {
                        case 1:
                           consumer.accept(Level.SEVERE);
                           break;
                        case 2:
                           consumer.accept(Level.WARNING);
                           break;
                        case 3:
                           consumer.accept(Level.INFO);
                           break;
                        case 4:
                        default:
                           if (DEBUG) {
                              consumer.accept(Level.INFO);
                           }
                     }
                  }

                  @Override
                  public void log(int level, @NotNull String msg) {
                     this.log(level, lvl -> this.logger.log(lvl, msg));
                  }

                  @Override
                  public void log(int level, @NotNull String msg, @NotNull Throwable throwable) {
                     this.log(level, lvl -> this.logger.log(lvl, msg, throwable));
                  }

                  @Override
                  public void log(int level, @NotNull Supplier<String> msg) {
                     this.log(level, lvl -> this.logger.log(lvl, msg));
                  }

                  @Override
                  public void log(int level, @NotNull Supplier<String> msg, @NotNull Throwable throwable) {
                     this.log(level, lvl -> this.logger.log(lvl, throwable, msg));
                  }
               };
            }
         }
      }

      void log(int var1, @NotNull String var2);

      void log(int var1, @NotNull String var2, @NotNull Throwable var3);

      default void log(int level, @NotNull Supplier<String> msg) {
         this.log(level, msg.get());
      }

      default void log(int level, @NotNull Supplier<String> msg, @NotNull Throwable throwable) {
         this.log(level, msg.get(), throwable);
      }
   }
}
