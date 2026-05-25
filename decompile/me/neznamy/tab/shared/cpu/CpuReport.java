package me.neznamy.tab.shared.cpu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class CpuReport {
   @NotNull
   private final Map<String, Map<String, Float>> featureUsage;
   private final double featureUsageTotal;
   @NotNull
   private final Map<String, Float> placeholderUsage;
   private final double placeholderUsageTotal;

   public CpuReport(int updateRateSeconds, @NotNull Map<String, Map<String, AtomicLong>> features, @NotNull Map<String, AtomicLong> placeholders) {
      long TIME_PERCENT = TimeUnit.SECONDS.toNanos(1L) / updateRateSeconds;
      TreeMap<Long, Entry<String, Map<String, Float>>> sorted = new TreeMap<>((o1, o2) -> Long.compare(o2, o1));
      features.forEach(
         (key, val) -> {
            Map<String, Float> percent = new LinkedHashMap<>(val.size());
            long sum = val.entrySet()
               .stream()
               .sorted(Entry.comparingByValue((o1, o2) -> Long.compare(o2.get(), o1.get())))
               .peek(e -> percent.put(e.getKey(), (float)e.getValue().get() / (float)TIME_PERCENT))
               .mapToLong(e -> e.getValue().get())
               .sum();
            sorted.put(sum, new SimpleImmutableEntry<>(key, percent));
         }
      );
      this.featureUsage = sorted.values().stream().collect(() -> new LinkedHashMap<>(features.size()), (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
      this.featureUsageTotal = this.featureUsage.values().stream().mapToDouble(map -> map.values().stream().mapToDouble(Float::floatValue).sum()).sum();
      this.placeholderUsage = placeholders.entrySet()
         .stream()
         .sorted(Entry.comparingByValue((o1, o2) -> Long.compare(o2.get(), o1.get())))
         .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), (float)e.getValue().get() / (float)TIME_PERCENT), Map::putAll);
      this.placeholderUsageTotal = this.placeholderUsage.values().stream().mapToDouble(Float::floatValue).sum();
   }

   @NotNull
   @Generated
   public Map<String, Map<String, Float>> getFeatureUsage() {
      return this.featureUsage;
   }

   @Generated
   public double getFeatureUsageTotal() {
      return this.featureUsageTotal;
   }

   @NotNull
   @Generated
   public Map<String, Float> getPlaceholderUsage() {
      return this.placeholderUsage;
   }

   @Generated
   public double getPlaceholderUsageTotal() {
      return this.placeholderUsageTotal;
   }
}
