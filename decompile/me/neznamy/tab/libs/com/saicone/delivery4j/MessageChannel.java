package me.neznamy.tab.libs.com.saicone.delivery4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import me.neznamy.tab.libs.com.saicone.delivery4j.util.Encryptor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageChannel {
   private final String name;
   private ChannelConsumer<String[]> consumer;
   private MessageChannel.Cache cache;
   private Encryptor encryptor;

   @NotNull
   public static MessageChannel of(@NotNull String name) {
      return new MessageChannel(name);
   }

   public MessageChannel(@NotNull String name) {
      this.name = name;
   }

   public MessageChannel(@NotNull String name, @Nullable ChannelConsumer<String[]> consumer) {
      this.name = name;
      this.consumer = consumer;
   }

   @NotNull
   public String getName() {
      return this.name;
   }

   @Nullable
   public ChannelConsumer<String[]> getConsumer() {
      return this.consumer;
   }

   @Nullable
   public MessageChannel.Cache getCache() {
      return this.cache;
   }

   @Nullable
   public Encryptor getEncryptor() {
      return this.encryptor;
   }

   @NotNull
   @Contract("_ -> this")
   public MessageChannel consume(@NotNull ChannelConsumer<String[]> consumer) {
      if (this.consumer == null) {
         this.consumer = consumer;
      } else {
         this.consumer = this.consumer.andThen(consumer);
      }

      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public MessageChannel consumeBefore(@NotNull ChannelConsumer<String[]> consumer) {
      if (this.consumer == null) {
         this.consumer = consumer;
      } else {
         this.consumer = consumer.andThen(this.consumer);
      }

      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public MessageChannel cache(boolean enable) {
      return enable ? this.cache(10L, TimeUnit.SECONDS) : this.cache(null);
   }

   @NotNull
   @Contract("_, _ -> this")
   public MessageChannel cache(long duration, @NotNull TimeUnit unit) {
      return this.cache(MessageChannel.Cache.of(duration, unit));
   }

   @NotNull
   @Contract("_ -> this")
   public MessageChannel cache(@Nullable MessageChannel.Cache cache) {
      this.cache = cache;
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public MessageChannel encryptor(@Nullable Encryptor encryptor) {
      this.encryptor = encryptor;
      return this;
   }

   public byte[] encode(@Nullable Object... lines) throws IOException {
      ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();

      byte[] var15;
      try {
         DataOutputStream out = new DataOutputStream(arrayOut);

         try {
            if (this.cache != null) {
               out.writeInt(this.cache.generate());
            }

            out.writeInt(lines.length);
            if (this.encryptor == null) {
               for (Object message : lines) {
                  out.writeUTF(Objects.toString(message));
               }
            } else {
               try {
                  for (Object message : lines) {
                     byte[] bytes = this.encryptor.encrypt(Objects.toString(message));
                     out.writeInt(bytes.length);
                     out.write(bytes);
                  }
               } catch (Throwable t) {
                  throw new IOException("Cannot encrypt message into channel " + this.name, t);
               }
            }

            var15 = arrayOut.toByteArray();
         } catch (Throwable var12) {
            try {
               out.close();
            } catch (Throwable var10) {
               var12.addSuppressed(var10);
            }

            throw var12;
         }

         out.close();
      } catch (Throwable var13) {
         try {
            arrayOut.close();
         } catch (Throwable var9) {
            var13.addSuppressed(var9);
         }

         throw var13;
      }

      arrayOut.close();
      return var15;
   }

   @Nullable
   public String[] decode(byte[] src) throws IOException {
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(src));

      String[] lines;
      label71: {
         String[] var12;
         try {
            if (this.cache != null && this.cache.contains(in.readInt())) {
               lines = null;
               break label71;
            }

            lines = new String[in.readInt()];

            try {
               if (this.encryptor == null) {
                  for (int i = 0; i < lines.length; i++) {
                     String message = in.readUTF();
                     lines[i] = message.equalsIgnoreCase("null") ? null : message;
                  }
               } else {
                  try {
                     for (int i = 0; i < lines.length; i++) {
                        String message = this.encryptor.decrypt(in.readNBytes(in.readInt()));
                        lines[i] = message.equalsIgnoreCase("null") ? null : message;
                     }
                  } catch (Throwable t) {
                     throw new IOException("Cannot decrypt message from channel " + this.name, t);
                  }
               }
            } catch (EOFException var8) {
            }

            var12 = lines;
         } catch (Throwable var9) {
            try {
               in.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }

            throw var9;
         }

         in.close();
         return var12;
      }

      in.close();
      return lines;
   }

   public boolean accept(byte[] src) throws IOException {
      String[] lines = this.decode(src);
      if (lines == null) {
         return false;
      }

      if (this.consumer != null) {
         this.consumer.accept(this.getName(), lines);
      }

      return true;
   }

   public void clear() {
      if (this.cache != null) {
         this.cache.clear();
      }
   }

   public abstract static class Cache {
      @NotNull
      public static MessageChannel.Cache of(final long duration, @NotNull final TimeUnit unit) {
         try {
            Class.forName("com.github.benmanes.caffeine.cache.Caffeine");
            return Class.forName("me.neznamy.tab.libs.com.saicone.delivery4j.cache.CaffeineCache")
               .asSubclass(MessageChannel.Cache.class)
               .getDeclaredConstructor(long.class, TimeUnit.class)
               .newInstance(duration, unit);
         } catch (Throwable var5) {
            try {
               Class.forName("com.google.common.cache.CacheBuilder");
               return Class.forName("me.neznamy.tab.libs.com.saicone.delivery4j.cache.GuavaCache")
                  .asSubclass(MessageChannel.Cache.class)
                  .getDeclaredConstructor(long.class, TimeUnit.class)
                  .newInstance(duration, unit);
            } catch (Throwable var4) {
               return new MessageChannel.Cache() {
                  private final Map<Integer, Long> cache = new HashMap<>();
                  private final long millis = unit.toMillis(duration);

                  @Override
                  protected void save(int id) {
                     long currentTime = System.currentTimeMillis();
                     if (id < 1999) {
                        long time = currentTime - this.millis;
                        this.cache.entrySet().removeIf(entry -> entry.getValue() <= time);
                     }

                     this.cache.put(id, currentTime);
                  }

                  @Override
                  public boolean contains(int id) {
                     return this.cache.containsKey(id);
                  }

                  @Override
                  public void clear() {
                     this.cache.clear();
                  }
               };
            }
         }
      }

      protected abstract void save(int var1);

      public abstract boolean contains(int var1);

      public int generate() {
         int id = ThreadLocalRandom.current().nextInt(0, 1000000);
         this.save(id);
         return id;
      }

      public abstract void clear();
   }
}
