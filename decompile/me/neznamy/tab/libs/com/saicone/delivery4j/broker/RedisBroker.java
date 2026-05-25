package me.neznamy.tab.libs.com.saicone.delivery4j.broker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import me.neznamy.tab.libs.com.saicone.delivery4j.Broker;
import me.neznamy.tab.libs.redis.clients.jedis.Jedis;
import me.neznamy.tab.libs.redis.clients.jedis.JedisPool;
import me.neznamy.tab.libs.redis.clients.jedis.JedisPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.JedisPubSub;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import org.jetbrains.annotations.NotNull;

public class RedisBroker extends Broker {
   private final JedisPool pool;
   private final Supplier<String> password;
   private final RedisBroker.Bridge bridge;
   private long sleepTime = 8L;
   private TimeUnit sleepUnit = TimeUnit.SECONDS;
   private Object aliveTask;

   @NotNull
   public static RedisBroker of(@NotNull String url) {
      String password = "";
      if (url.contains("@")) {
         String s = url.substring(0, url.lastIndexOf("@"));
         if (s.contains(":")) {
            password = s.substring(s.lastIndexOf(":") + 1);
         }
      }

      try {
         return of(new URI(url), password);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @NotNull
   public static RedisBroker of(@NotNull URI uri, @NotNull String password) {
      return new RedisBroker(new JedisPool(uri), password);
   }

   @NotNull
   public static RedisBroker of(@NotNull String host, int port, @NotNull String password, int database, boolean ssl) {
      return new RedisBroker(new JedisPool(new JedisPoolConfig(), host, port, 2000, password, database, ssl), password);
   }

   public RedisBroker(@NotNull JedisPool pool, @NotNull String password) {
      this.pool = pool;
      this.password = this.password(password);
      this.bridge = new RedisBroker.Bridge();
   }

   public RedisBroker(@NotNull JedisPool pool, @NotNull String password, @NotNull RedisBroker.Bridge bridge) {
      this.pool = pool;
      this.password = this.password(password);
      this.bridge = bridge;
   }

   @NotNull
   private Supplier<String> password(@NotNull String password) {
      return () -> {
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();

         for (int i = 2; i < stack.length; i++) {
            if (stack[i].getClassName().equals(RedisBroker.class.getName())) {
               return password;
            }
         }

         throw new SecurityException("Redis password is only accessible from Redis broker instance");
      };
   }

   @Override
   protected void onStart() {
      this.setEnabled(true);
      this.aliveTask = this.getExecutor().execute(this::alive);
   }

   @Override
   protected void onClose() {
      this.setEnabled(false);

      try {
         this.bridge.unsubscribe();
      } catch (Throwable var3) {
      }

      try {
         this.pool.destroy();
      } catch (Throwable var2) {
      }

      if (this.aliveTask != null) {
         this.getExecutor().cancel(this.aliveTask);
      }
   }

   @Override
   protected void onSubscribe(@NotNull String... channels) {
      try {
         this.bridge.unsubscribe();
      } catch (Throwable var3) {
      }

      if (this.aliveTask != null) {
         this.getExecutor().cancel(this.aliveTask);
      }

      this.aliveTask = this.getExecutor().execute(this::alive);
   }

   @Override
   protected void onUnsubscribe(@NotNull String... channels) {
      try {
         this.bridge.unsubscribe();
      } catch (Throwable var3) {
      }

      if (this.aliveTask != null) {
         this.getExecutor().cancel(this.aliveTask);
      }

      this.aliveTask = this.getExecutor().execute(this::alive);
   }

   @Override
   protected void onSend(@NotNull String channel, byte[] data) throws IOException {
      Jedis jedis = this.pool.getResource();

      try {
         String message = this.getCodec().encode(data);

         try {
            jedis.publish(channel, message);
         } catch (JedisDataException e) {
            if (!e.getMessage().contains("NOAUTH")) {
               throw new IOException(e);
            }

            jedis.auth(this.password.get());
            jedis.publish(channel, message);
         }
      } catch (Throwable var8) {
         if (jedis != null) {
            try {
               jedis.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }
         }

         throw var8;
      }

      if (jedis != null) {
         jedis.close();
      }
   }

   public void setReconnectionInterval(int time, @NotNull TimeUnit unit) {
      this.sleepTime = time;
      this.sleepUnit = unit;
   }

   @NotNull
   public JedisPool getPool() {
      return this.pool;
   }

   @NotNull
   public RedisBroker.Bridge getBridge() {
      return this.bridge;
   }

   private void alive() {
      boolean reconnected = false;

      while (this.isEnabled() && !Thread.interrupted() && this.pool != null && !this.pool.isClosed()) {
         try {
            Jedis jedis = this.pool.getResource();

            try {
               if (reconnected) {
                  this.getLogger().log(3, "Redis connection is alive again");
               }

               jedis.subscribe(this.bridge, this.getSubscribedChannels().toArray(new String[0]));
            } catch (Throwable var8) {
               if (jedis != null) {
                  try {
                     jedis.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (jedis != null) {
               jedis.close();
            }
         } catch (Throwable t) {
            if (!this.isEnabled()) {
               return;
            }

            if (reconnected) {
               this.getLogger()
                  .log(2, () -> "Redis connection dropped, automatic reconnection in " + this.sleepTime + " " + this.sleepUnit.name().toLowerCase() + "...", t);
            }

            try {
               this.bridge.unsubscribe();
            } catch (Throwable var6) {
            }

            if (!reconnected) {
               reconnected = true;
            } else {
               try {
                  Thread.sleep(this.sleepUnit.toMillis(this.sleepTime));
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               }
            }
         }
      }
   }

   public class Bridge extends JedisPubSub {
      public void onMessage(String channel, String message) {
         if (channel != null && RedisBroker.this.getSubscribedChannels().contains(channel) && message != null) {
            try {
               RedisBroker.this.receive(channel, RedisBroker.this.getCodec().decode(message));
            } catch (IOException e) {
               RedisBroker.this.getLogger().log(2, "Cannot process received message from channel '" + channel + "'", e);
            }
         }
      }

      public void onSubscribe(String channel, int subscribedChannels) {
         RedisBroker.this.getLogger().log(3, "Redis subscribed to channel '" + channel + "'");
      }

      public void onUnsubscribe(String channel, int subscribedChannels) {
         RedisBroker.this.getLogger().log(3, "Redis unsubscribed from channel '" + channel + "'");
      }
   }
}
