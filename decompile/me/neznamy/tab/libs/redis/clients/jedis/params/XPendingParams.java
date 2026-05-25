package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.args.RawableFactory;

public class XPendingParams implements IParams {
   private Long idle;
   private Rawable start;
   private Rawable end;
   private Integer count;
   private Rawable consumer;

   public XPendingParams(StreamEntryID start, StreamEntryID end, int count) {
      this(start.toString(), end.toString(), count);
   }

   public XPendingParams(String start, String end, int count) {
      this(RawableFactory.from(start), RawableFactory.from(end), Integer.valueOf(count));
   }

   public XPendingParams(byte[] start, byte[] end, int count) {
      this(RawableFactory.from(start), RawableFactory.from(end), Integer.valueOf(count));
   }

   private XPendingParams(Rawable start, Rawable end, Integer count) {
      this.start = start;
      this.end = end;
      this.count = count;
   }

   public XPendingParams() {
      this.start = null;
      this.end = null;
      this.count = null;
   }

   public static XPendingParams xPendingParams(StreamEntryID start, StreamEntryID end, int count) {
      return new XPendingParams(start, end, count);
   }

   public static XPendingParams xPendingParams(String start, String end, int count) {
      return new XPendingParams(start, end, count);
   }

   public static XPendingParams xPendingParams(byte[] start, byte[] end, int count) {
      return new XPendingParams(start, end, count);
   }

   public static XPendingParams xPendingParams() {
      return new XPendingParams();
   }

   public XPendingParams idle(long idle) {
      this.idle = idle;
      return this;
   }

   public XPendingParams start(StreamEntryID start) {
      this.start = RawableFactory.from(start.toString());
      return this;
   }

   public XPendingParams end(StreamEntryID end) {
      this.end = RawableFactory.from(end.toString());
      return this;
   }

   public XPendingParams count(int count) {
      this.count = count;
      return this;
   }

   public XPendingParams consumer(String consumer) {
      this.consumer = RawableFactory.from(consumer);
      return this;
   }

   public XPendingParams consumer(byte[] consumer) {
      this.consumer = RawableFactory.from(consumer);
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.count == null) {
         throw new IllegalArgumentException("start, end and count must be set.");
      }

      if (this.start == null) {
         this.start = RawableFactory.from("-");
      }

      if (this.end == null) {
         this.end = RawableFactory.from("+");
      }

      if (this.idle != null) {
         args.add(Protocol.Keyword.IDLE).add(this.idle);
      }

      args.add(this.start).add(this.end).add(this.count);
      if (this.consumer != null) {
         args.add(this.consumer);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XPendingParams that = (XPendingParams)o;
         return Objects.equals(this.idle, that.idle)
            && Objects.equals(this.start, that.start)
            && Objects.equals(this.end, that.end)
            && Objects.equals(this.count, that.count)
            && Objects.equals(this.consumer, that.consumer);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.idle, this.start, this.end, this.count, this.consumer);
   }
}
