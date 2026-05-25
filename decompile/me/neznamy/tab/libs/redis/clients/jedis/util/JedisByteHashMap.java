package me.neznamy.tab.libs.redis.clients.jedis.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class JedisByteHashMap implements Map<byte[], byte[]>, Cloneable, Serializable {
   private static final long serialVersionUID = -6971431362627219416L;
   private final Map<JedisByteHashMap.ByteArrayWrapper, byte[]> internalMap = new HashMap<>();

   @Override
   public void clear() {
      this.internalMap.clear();
   }

   @Override
   public boolean containsKey(Object key) {
      return key instanceof byte[] ? this.internalMap.containsKey(new JedisByteHashMap.ByteArrayWrapper((byte[])key)) : this.internalMap.containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return this.internalMap.containsValue(value);
   }

   @Override
   public Set<Entry<byte[], byte[]>> entrySet() {
      Iterator<Entry<JedisByteHashMap.ByteArrayWrapper, byte[]>> iterator = this.internalMap.entrySet().iterator();
      HashSet<Entry<byte[], byte[]>> hashSet = new HashSet<>();

      while (iterator.hasNext()) {
         Entry<JedisByteHashMap.ByteArrayWrapper, byte[]> entry = iterator.next();
         hashSet.add(new JedisByteHashMap.JedisByteEntry(entry.getKey().data, entry.getValue()));
      }

      return hashSet;
   }

   public byte[] get(Object key) {
      return key instanceof byte[] ? this.internalMap.get(new JedisByteHashMap.ByteArrayWrapper((byte[])key)) : this.internalMap.get(key);
   }

   @Override
   public boolean isEmpty() {
      return this.internalMap.isEmpty();
   }

   @Override
   public Set<byte[]> keySet() {
      Set<byte[]> keySet = new HashSet<>();
      Iterator<JedisByteHashMap.ByteArrayWrapper> iterator = this.internalMap.keySet().iterator();

      while (iterator.hasNext()) {
         keySet.add(iterator.next().data);
      }

      return keySet;
   }

   public byte[] put(byte[] key, byte[] value) {
      return this.internalMap.put(new JedisByteHashMap.ByteArrayWrapper(key), value);
   }

   @Override
   public void putAll(Map<byte[], byte[]> m) {
      for (Entry<byte[], byte[]> next : m.entrySet()) {
         this.internalMap.put(new JedisByteHashMap.ByteArrayWrapper(next.getKey()), next.getValue());
      }
   }

   public byte[] remove(Object key) {
      return key instanceof byte[] ? this.internalMap.remove(new JedisByteHashMap.ByteArrayWrapper((byte[])key)) : this.internalMap.remove(key);
   }

   @Override
   public int size() {
      return this.internalMap.size();
   }

   @Override
   public Collection<byte[]> values() {
      return this.internalMap.values();
   }

   private static final class ByteArrayWrapper implements Serializable {
      private final byte[] data;

      public ByteArrayWrapper(byte[] data) {
         if (data == null) {
            throw new NullPointerException();
         }

         this.data = data;
      }

      @Override
      public boolean equals(Object other) {
         if (other == null) {
            return false;
         } else if (other == this) {
            return true;
         } else {
            return !(other instanceof JedisByteHashMap.ByteArrayWrapper) ? false : Arrays.equals(this.data, ((JedisByteHashMap.ByteArrayWrapper)other).data);
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.data);
      }
   }

   private static final class JedisByteEntry implements Entry<byte[], byte[]> {
      private byte[] value;
      private byte[] key;

      public JedisByteEntry(byte[] key, byte[] value) {
         this.key = key;
         this.value = value;
      }

      public byte[] getKey() {
         return this.key;
      }

      public byte[] getValue() {
         return this.value;
      }

      public byte[] setValue(byte[] value) {
         this.value = value;
         return value;
      }
   }
}
