package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class StreamEntryID implements Comparable<StreamEntryID>, Serializable {
   private static final long serialVersionUID = 1L;
   private long time;
   private long sequence;
   public static final StreamEntryID NEW_ENTRY = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return "*";
      }
   };
   public static final StreamEntryID XGROUP_LAST_ENTRY = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return "$";
      }
   };
   @Deprecated
   public static final StreamEntryID LAST_ENTRY = XGROUP_LAST_ENTRY;
   public static final StreamEntryID XREAD_NEW_ENTRY = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return "$";
      }
   };
   public static final StreamEntryID XREADGROUP_UNDELIVERED_ENTRY = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return ">";
      }
   };
   @Deprecated
   public static final StreamEntryID UNRECEIVED_ENTRY = XREADGROUP_UNDELIVERED_ENTRY;
   public static final StreamEntryID MINIMUM_ID = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return "-";
      }
   };
   public static final StreamEntryID MAXIMUM_ID = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return "+";
      }
   };
   public static final StreamEntryID XREAD_LAST_ENTRY = new StreamEntryID() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
         return "+";
      }
   };

   public StreamEntryID() {
      this(0L, 0L);
   }

   public StreamEntryID(byte[] id) {
      this(SafeEncoder.encode(id));
   }

   public StreamEntryID(String id) {
      String[] split = id.split("-");
      this.time = Long.parseLong(split[0]);
      this.sequence = Long.parseLong(split[1]);
   }

   public StreamEntryID(long time) {
      this(time, 0L);
   }

   public StreamEntryID(long time, long sequence) {
      this.time = time;
      this.sequence = sequence;
   }

   @Override
   public String toString() {
      return this.time + "-" + this.sequence;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      StreamEntryID other = (StreamEntryID)obj;
      return this.time == other.time && this.sequence == other.sequence;
   }

   @Override
   public int hashCode() {
      return this.toString().hashCode();
   }

   public int compareTo(StreamEntryID other) {
      int timeCompare = Long.compare(this.time, other.time);
      return timeCompare != 0 ? timeCompare : Long.compare(this.sequence, other.sequence);
   }

   public long getTime() {
      return this.time;
   }

   public long getSequence() {
      return this.sequence;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeLong(this.time);
      out.writeLong(this.sequence);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.time = in.readLong();
      this.sequence = in.readLong();
   }
}
