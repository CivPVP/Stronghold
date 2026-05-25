package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.params.ScanParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class ScanResult<T> {
   private byte[] cursor;
   private List<T> results;

   public ScanResult(String cursor, List<T> results) {
      this(SafeEncoder.encode(cursor), results);
   }

   public ScanResult(byte[] cursor, List<T> results) {
      this.cursor = cursor;
      this.results = results;
   }

   public String getCursor() {
      return SafeEncoder.encode(this.cursor);
   }

   public boolean isCompleteIteration() {
      return ScanParams.SCAN_POINTER_START.equals(this.getCursor());
   }

   public byte[] getCursorAsBytes() {
      return this.cursor;
   }

   public List<T> getResult() {
      return this.results;
   }
}
