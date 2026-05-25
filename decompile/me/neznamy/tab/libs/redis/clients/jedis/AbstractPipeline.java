package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.Closeable;

public abstract class AbstractPipeline extends PipeliningBase implements Closeable {
   protected AbstractPipeline(CommandObjects commandObjects) {
      super(commandObjects);
   }

   @Override
   public abstract void close();

   public abstract void sync();

   public Response<Long> publish(String channel, String message) {
      return this.appendCommand(this.commandObjects.publish(channel, message));
   }

   public Response<Long> publish(byte[] channel, byte[] message) {
      return this.appendCommand(this.commandObjects.publish(channel, message));
   }
}
