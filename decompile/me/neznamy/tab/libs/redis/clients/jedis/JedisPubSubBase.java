package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Arrays;
import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public abstract class JedisPubSubBase<T> {
   private int subscribedChannels = 0;
   private volatile Connection client;

   public void onMessage(T channel, T message) {
   }

   public void onPMessage(T pattern, T channel, T message) {
   }

   public void onSubscribe(T channel, int subscribedChannels) {
   }

   public void onUnsubscribe(T channel, int subscribedChannels) {
   }

   public void onPUnsubscribe(T pattern, int subscribedChannels) {
   }

   public void onPSubscribe(T pattern, int subscribedChannels) {
   }

   public void onPong(T pattern) {
   }

   private void sendAndFlushCommand(Protocol.Command command, T... args) {
      if (this.client == null) {
         throw new JedisException(this.getClass() + " is not connected to a Connection.");
      }

      CommandArguments cargs = new CommandArguments(command).addObjects(args);
      this.client.sendCommand(cargs);
      this.client.flush();
   }

   public final void unsubscribe() {
      this.sendAndFlushCommand(Protocol.Command.UNSUBSCRIBE, (T[])(new Object[0]));
   }

   public final void unsubscribe(T... channels) {
      this.sendAndFlushCommand(Protocol.Command.UNSUBSCRIBE, channels);
   }

   public final void subscribe(T... channels) {
      this.sendAndFlushCommand(Protocol.Command.SUBSCRIBE, channels);
   }

   public final void psubscribe(T... patterns) {
      this.sendAndFlushCommand(Protocol.Command.PSUBSCRIBE, patterns);
   }

   public final void punsubscribe() {
      this.sendAndFlushCommand(Protocol.Command.PUNSUBSCRIBE, (T[])(new Object[0]));
   }

   public final void punsubscribe(T... patterns) {
      this.sendAndFlushCommand(Protocol.Command.PUNSUBSCRIBE, patterns);
   }

   public final void ping() {
      this.sendAndFlushCommand(Protocol.Command.PING, (T[])(new Object[0]));
   }

   public final void ping(T argument) {
      this.sendAndFlushCommand(Protocol.Command.PING, argument);
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
         this.subscribe(channels);
         this.process();
      } finally {
         this.client.rollbackTimeout();
      }
   }

   public final void proceedWithPatterns(Connection client, T... patterns) {
      this.client = client;
      this.client.setTimeoutInfinite();

      try {
         this.psubscribe(patterns);
         this.process();
      } finally {
         this.client.rollbackTimeout();
      }
   }

   protected abstract T encode(byte[] var1);

   private void process() {
      do {
         Object reply = this.client.getUnflushedObject();
         if (reply instanceof List) {
            List<Object> listReply = (List<Object>)reply;
            Object firstObj = listReply.get(0);
            if (!(firstObj instanceof byte[])) {
               throw new JedisException("Unknown message type: " + firstObj);
            }

            byte[] resp = (byte[])firstObj;
            if (Arrays.equals(Protocol.ResponseKeyword.SUBSCRIBE.getRaw(), resp)) {
               this.subscribedChannels = ((Long)listReply.get(2)).intValue();
               byte[] bchannel = (byte[])listReply.get(1);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               this.onSubscribe(enchannel, this.subscribedChannels);
            } else if (Arrays.equals(Protocol.ResponseKeyword.UNSUBSCRIBE.getRaw(), resp)) {
               this.subscribedChannels = ((Long)listReply.get(2)).intValue();
               byte[] bchannel = (byte[])listReply.get(1);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               this.onUnsubscribe(enchannel, this.subscribedChannels);
            } else if (Arrays.equals(Protocol.ResponseKeyword.MESSAGE.getRaw(), resp)) {
               byte[] bchannel = (byte[])listReply.get(1);
               Object mesg = listReply.get(2);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               if (mesg instanceof List) {
                  ((List)mesg).forEach(bmesgx -> this.onMessage(enchannel, this.encode(bmesgx)));
               } else {
                  this.onMessage(enchannel, mesg == null ? null : this.encode((byte[])mesg));
               }
            } else if (Arrays.equals(Protocol.ResponseKeyword.PMESSAGE.getRaw(), resp)) {
               byte[] bpattern = (byte[])listReply.get(1);
               byte[] bchannel = (byte[])listReply.get(2);
               byte[] bmesg = (byte[])listReply.get(3);
               T enpattern = bpattern == null ? null : this.encode(bpattern);
               T enchannel = bchannel == null ? null : this.encode(bchannel);
               T enmesg = bmesg == null ? null : this.encode(bmesg);
               this.onPMessage(enpattern, enchannel, enmesg);
            } else if (Arrays.equals(Protocol.ResponseKeyword.PSUBSCRIBE.getRaw(), resp)) {
               this.subscribedChannels = ((Long)listReply.get(2)).intValue();
               byte[] bpattern = (byte[])listReply.get(1);
               T enpattern = bpattern == null ? null : this.encode(bpattern);
               this.onPSubscribe(enpattern, this.subscribedChannels);
            } else if (Arrays.equals(Protocol.ResponseKeyword.PUNSUBSCRIBE.getRaw(), resp)) {
               this.subscribedChannels = ((Long)listReply.get(2)).intValue();
               byte[] bpattern = (byte[])listReply.get(1);
               T enpattern = bpattern == null ? null : this.encode(bpattern);
               this.onPUnsubscribe(enpattern, this.subscribedChannels);
            } else {
               if (!Arrays.equals(Protocol.ResponseKeyword.PONG.getRaw(), resp)) {
                  throw new JedisException("Unknown message type: " + firstObj);
               }

               byte[] bpattern = (byte[])listReply.get(1);
               T enpattern = bpattern == null ? null : this.encode(bpattern);
               this.onPong(enpattern);
            }
         } else {
            if (!(reply instanceof byte[])) {
               throw new JedisException("Unknown message type: " + reply);
            }

            byte[] resp = (byte[])reply;
            if ("PONG".equals(SafeEncoder.encode(resp))) {
               this.onPong(null);
            } else {
               this.onPong(this.encode(resp));
            }
         }
      } while (!Thread.currentThread().isInterrupted() && this.isSubscribed());
   }
}
