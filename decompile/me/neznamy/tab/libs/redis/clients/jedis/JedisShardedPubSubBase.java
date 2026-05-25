package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Arrays;
import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;

public abstract class JedisShardedPubSubBase<T> {
   private int subscribedChannels = 0;
   private volatile Connection client;

   public void onSMessage(T channel, T message) {
   }

   public void onSSubscribe(T channel, int subscribedChannels) {
   }

   public void onSUnsubscribe(T channel, int subscribedChannels) {
   }

   private void sendAndFlushCommand(Protocol.Command command, T... args) {
      if (this.client == null) {
         throw new JedisException(this.getClass() + " is not connected to a Connection.");
      }

      CommandArguments cargs = new CommandArguments(command).addObjects(args);
      this.client.sendCommand(cargs);
      this.client.flush();
   }

   public final void sunsubscribe() {
      this.sendAndFlushCommand(Protocol.Command.SUNSUBSCRIBE, (T[])(new Object[0]));
   }

   public final void sunsubscribe(T... channels) {
      this.sendAndFlushCommand(Protocol.Command.SUNSUBSCRIBE, channels);
   }

   public final void ssubscribe(T... channels) {
      this.sendAndFlushCommand(Protocol.Command.SSUBSCRIBE, channels);
   }

   public final boolean isSubscribed() {
      return this.subscribedChannels > 0;
   }

   public final int getSubscribedChannels() {
      return this.subscribedChannels;
   }

   public final void proceed(Connection client, T... channels) {
      this.client = client;
      this.client.setTimeoutInfinite();

      try {
         this.ssubscribe(channels);
         this.process();
      } finally {
         this.client.rollbackTimeout();
      }
   }

   protected abstract T encode(byte[] var1);

   private void process() {
      while (true) {
         Object reply = this.client.getUnflushedObject();
         if (reply instanceof List) {
            List<Object> listReply = (List<Object>)reply;
            Object firstObj = listReply.get(0);
            if (!(firstObj instanceof byte[])) {
               throw new JedisException("Unknown message type: " + firstObj);
            }

            byte[] resp = (byte[])firstObj;
            if (Arrays.equals(Protocol.ResponseKeyword.SSUBSCRIBE.getRaw(), resp)) {
               this.subscribedChannels = ((Long)listReply.get(2)).intValue();
               byte[] bchannel = (byte[])listReply.get(1);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               this.onSSubscribe(enchannel, this.subscribedChannels);
            } else if (Arrays.equals(Protocol.ResponseKeyword.SUNSUBSCRIBE.getRaw(), resp)) {
               this.subscribedChannels = ((Long)listReply.get(2)).intValue();
               byte[] bchannel = (byte[])listReply.get(1);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               this.onSUnsubscribe(enchannel, this.subscribedChannels);
            } else {
               if (!Arrays.equals(Protocol.ResponseKeyword.SMESSAGE.getRaw(), resp)) {
                  throw new JedisException("Unknown message type: " + firstObj);
               }

               byte[] bchannel = (byte[])listReply.get(1);
               byte[] bmesg = (byte[])listReply.get(2);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               T enmesg = bmesg == null ? null : this.encode(bmesg);
               this.onSMessage(enchannel, enmesg);
            }

            if (!Thread.currentThread().isInterrupted() && this.isSubscribed()) {
               continue;
            }

            return;
         }

         throw new JedisException("Unknown message type: " + reply);
      }
   }
}
