package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class TSMRangeElements extends KeyValue<String, List<TSElement>> {
   private final Map<String, String> labels;
   private final List<AggregationType> aggregators;
   private final List<String> reducers;
   private final List<String> sources;

   public TSMRangeElements(String key, Map<String, String> labels, List<TSElement> value) {
      super(key, value);
      this.labels = labels;
      this.aggregators = null;
      this.reducers = null;
      this.sources = null;
   }

   public TSMRangeElements(String key, Map<String, String> labels, List<AggregationType> aggregators, List<TSElement> value) {
      super(key, value);
      this.labels = labels;
      this.aggregators = aggregators;
      this.reducers = null;
      this.sources = null;
   }

   public TSMRangeElements(String key, Map<String, String> labels, List<String> reducers, List<String> sources, List<TSElement> value) {
      super(key, value);
      this.labels = labels;
      this.aggregators = null;
      this.reducers = reducers;
      this.sources = sources;
   }

   public Map<String, String> getLabels() {
      return this.labels;
   }

   public List<AggregationType> getAggregators() {
      return this.aggregators;
   }

   public List<String> getReducers() {
      return this.reducers;
   }

   public List<String> getSources() {
      return this.sources;
   }

   public List<TSElement> getElements() {
      return this.getValue();
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder()
         .append(this.getClass().getSimpleName())
         .append("{key=")
         .append(this.getKey())
         .append(", labels=")
         .append(this.labels);
      if (this.aggregators != null) {
         sb.append(", aggregators=").append(this.aggregators);
      }

      if (this.reducers != null && this.sources != null) {
         sb.append(", reducers").append(this.reducers).append(", sources").append(this.sources);
      }

      return sb.append(", elements=").append(this.getElements()).append('}').toString();
   }
}
