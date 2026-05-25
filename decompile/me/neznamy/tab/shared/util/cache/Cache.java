package me.neznamy.tab.shared.util.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

public abstract class Cache<K, V> {
   private int accessCount;
   private final String name;
   private final int cacheSize;
   private final Map<K, V> cache = new HashMap<>();

   @NotNull
   public synchronized V get(@NotNull K key) {
      this.accessCount++;
      if (this.cache.size() > this.cacheSize) {
         float efficiency = (float)(this.accessCount - this.cacheSize) / this.accessCount;
         TAB.getInstance()
            .debug("Clearing " + this.name + " cache due to limit (efficiency " + efficiency * 100.0F + "% with " + this.accessCount + " accesses)");
         this.accessCount = 0;
         this.cache.clear();
      }

      return this.cache.computeIfAbsent(key, this::convert);
   }

   @NotNull
   public abstract V convert(@NotNull K var1);

   @Generated
   public Cache(String name, int cacheSize) {
      this.name = name;
      this.cacheSize = cacheSize;
   }
}
