package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.Closeable;
import java.util.List;

public abstract class AbstractTransaction extends PipeliningBase implements Closeable {
   @Deprecated
   protected AbstractTransaction() {
      super(new CommandObjects());
   }

   protected AbstractTransaction(CommandObjects commandObjects) {
      super(commandObjects);
   }

   public abstract void multi();

   public abstract String watch(String... var1);

   public abstract String watch(byte[]... var1);

   public abstract String unwatch();

   @Override
   public abstract void close();

   public abstract List<Object> exec();

   public abstract String discard();

   public Response<Long> waitReplicas(int replicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitReplicas(replicas, timeout));
   }

   public Response<Long> publish(String channel, String message) {
      return this.appendCommand(this.commandObjects.publish(channel, message));
   }

   public Response<Long> publish(byte[] channel, byte[] message) {
      return this.appendCommand(this.commandObjects.publish(channel, message));
   }
}
