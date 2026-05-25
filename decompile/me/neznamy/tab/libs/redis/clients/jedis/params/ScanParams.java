package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class ScanParams implements IParams {
   private final Map<Protocol.Keyword, ByteBuffer> params = new EnumMap<>(Protocol.Keyword.class);
   public static final String SCAN_POINTER_START = String.valueOf(0);
   public static final byte[] SCAN_POINTER_START_BINARY = SafeEncoder.encode(SCAN_POINTER_START);

   public ScanParams match(byte[] pattern) {
      this.params.put(Protocol.Keyword.MATCH, ByteBuffer.wrap(pattern));
      return this;
   }

   public ScanParams match(String pattern) {
      this.params.put(Protocol.Keyword.MATCH, ByteBuffer.wrap(SafeEncoder.encode(pattern)));
      return this;
   }

   public ScanParams count(Integer count) {
      this.params.put(Protocol.Keyword.COUNT, ByteBuffer.wrap(Protocol.toByteArray(count)));
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      for (Entry<Protocol.Keyword, ByteBuffer> param : this.params.entrySet()) {
         args.add(param.getKey());
         args.add(param.getValue().array());
      }
   }

   public byte[] binaryMatch() {
      return this.params.containsKey(Protocol.Keyword.MATCH) ? this.params.get(Protocol.Keyword.MATCH).array() : null;
   }

   public String match() {
      return this.params.containsKey(Protocol.Keyword.MATCH) ? new String(this.params.get(Protocol.Keyword.MATCH).array()) : null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ScanParams that = (ScanParams)o;
         return Objects.equals(this.params, that.params);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.params);
   }
}
