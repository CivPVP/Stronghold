package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;

public class StreamPendingEntry implements Serializable {
   private static final long serialVersionUID = 1L;
   private StreamEntryID id;
   private String consumerName;
   private long idleTime;
   private long deliveredTimes;

   public StreamPendingEntry(StreamEntryID id, String consumerName, long idleTime, long deliveredTimes) {
      this.id = id;
      this.consumerName = consumerName;
      this.idleTime = idleTime;
      this.deliveredTimes = deliveredTimes;
   }

   public StreamEntryID getID() {
      return this.id;
   }

   public long getIdleTime() {
      return this.idleTime;
   }

   public long getDeliveredTimes() {
      return this.deliveredTimes;
   }

   public String getConsumerName() {
      return this.consumerName;
   }

   @Override
   public String toString() {
      return this.id + " " + this.consumerName + " idle:" + this.idleTime + " times:" + this.deliveredTimes;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeUnshared(this.id);
      out.writeUTF(this.consumerName);
      out.writeLong(this.idleTime);
      out.writeLong(this.deliveredTimes);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.id = (StreamEntryID)in.readUnshared();
      this.consumerName = in.readUTF();
      this.idleTime = in.readLong();
      this.deliveredTimes = in.readLong();
   }
}
