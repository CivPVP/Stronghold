package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;

public class StreamEntry implements Serializable {
   private static final long serialVersionUID = 1L;
   private StreamEntryID id;
   private Map<String, String> fields;

   public StreamEntry(StreamEntryID id, Map<String, String> fields) {
      this.id = id;
      this.fields = fields;
   }

   public StreamEntryID getID() {
      return this.id;
   }

   public Map<String, String> getFields() {
      return this.fields;
   }

   @Override
   public String toString() {
      return this.id + " " + this.fields;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeUnshared(this.id);
      out.writeUnshared(this.fields);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.id = (StreamEntryID)in.readUnshared();
      this.fields = (Map<String, String>)in.readUnshared();
   }
}
