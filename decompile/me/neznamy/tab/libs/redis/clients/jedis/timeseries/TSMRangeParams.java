package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.Arrays;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class TSMRangeParams implements IParams {
   private Long fromTimestamp;
   private Long toTimestamp;
   private boolean latest;
   private long[] filterByTimestamps;
   private double[] filterByValues;
   private boolean withLabels;
   private String[] selectedLabels;
   private Integer count;
   private byte[] align;
   private AggregationType aggregationType;
   private long bucketDuration;
   private byte[] bucketTimestamp;
   private boolean empty;
   private String[] filters;
   private String groupByLabel;
   private String groupByReduce;

   public TSMRangeParams(long fromTimestamp, long toTimestamp) {
      this.fromTimestamp = fromTimestamp;
      this.toTimestamp = toTimestamp;
   }

   public static TSMRangeParams multiRangeParams(long fromTimestamp, long toTimestamp) {
      return new TSMRangeParams(fromTimestamp, toTimestamp);
   }

   public TSMRangeParams() {
   }

   public static TSMRangeParams multiRangeParams() {
      return new TSMRangeParams();
   }

   public TSMRangeParams fromTimestamp(long fromTimestamp) {
      this.fromTimestamp = fromTimestamp;
      return this;
   }

   public TSMRangeParams toTimestamp(long toTimestamp) {
      this.toTimestamp = toTimestamp;
      return this;
   }

   public TSMRangeParams latest() {
      this.latest = true;
      return this;
   }

   public TSMRangeParams filterByTS(long... timestamps) {
      this.filterByTimestamps = timestamps;
      return this;
   }

   public TSMRangeParams filterByValues(double min, double max) {
      this.filterByValues = new double[]{min, max};
      return this;
   }

   public TSMRangeParams withLabels(boolean withLabels) {
      this.withLabels = withLabels;
      return this;
   }

   public TSMRangeParams withLabels() {
      return this.withLabels(true);
   }

   public TSMRangeParams selectedLabels(String... labels) {
      this.selectedLabels = labels;
      return this;
   }

   public TSMRangeParams count(int count) {
      this.count = count;
      return this;
   }

   private TSMRangeParams align(byte[] raw) {
      this.align = raw;
      return this;
   }

   public TSMRangeParams align(long timestamp) {
      return this.align(Protocol.toByteArray(timestamp));
   }

   public TSMRangeParams alignStart() {
      return this.align(TimeSeriesProtocol.MINUS);
   }

   public TSMRangeParams alignEnd() {
      return this.align(TimeSeriesProtocol.PLUS);
   }

   public TSMRangeParams aggregation(AggregationType aggregationType, long bucketDuration) {
      this.aggregationType = aggregationType;
      this.bucketDuration = bucketDuration;
      return this;
   }

   public TSMRangeParams bucketTimestamp(String bucketTimestamp) {
      this.bucketTimestamp = SafeEncoder.encode(bucketTimestamp);
      return this;
   }

   public TSMRangeParams bucketTimestampLow() {
      this.bucketTimestamp = TimeSeriesProtocol.MINUS;
      return this;
   }

   public TSMRangeParams bucketTimestampHigh() {
      this.bucketTimestamp = TimeSeriesProtocol.PLUS;
      return this;
   }

   public TSMRangeParams bucketTimestampMid() {
      this.bucketTimestamp = Protocol.BYTES_TILDE;
      return this;
   }

   public TSMRangeParams empty() {
      this.empty = true;
      return this;
   }

   public TSMRangeParams filter(String... filters) {
      this.filters = filters;
      return this;
   }

   public TSMRangeParams groupBy(String label, String reduce) {
      this.groupByLabel = label;
      this.groupByReduce = reduce;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.filters == null) {
         throw new IllegalArgumentException("FILTER arguments must be set.");
      }

      if (this.fromTimestamp == null) {
         args.add(TimeSeriesProtocol.MINUS);
      } else {
         args.add(Protocol.toByteArray(this.fromTimestamp));
      }

      if (this.toTimestamp == null) {
         args.add(TimeSeriesProtocol.PLUS);
      } else {
         args.add(Protocol.toByteArray(this.toTimestamp));
      }

      if (this.latest) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.LATEST);
      }

      if (this.filterByTimestamps != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.FILTER_BY_TS);

         for (long ts : this.filterByTimestamps) {
            args.add(Protocol.toByteArray(ts));
         }
      }

      if (this.filterByValues != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.FILTER_BY_VALUE);

         for (double value : this.filterByValues) {
            args.add(Protocol.toByteArray(value));
         }
      }

      if (this.withLabels) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.WITHLABELS);
      } else if (this.selectedLabels != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.SELECTED_LABELS);

         for (String label : this.selectedLabels) {
            args.add(label);
         }
      }

      if (this.count != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.COUNT).add(Protocol.toByteArray(this.count));
      }

      if (this.aggregationType != null) {
         if (this.align != null) {
            args.add(TimeSeriesProtocol.TimeSeriesKeyword.ALIGN).add(this.align);
         }

         args.add(TimeSeriesProtocol.TimeSeriesKeyword.AGGREGATION).add(this.aggregationType).add(Protocol.toByteArray(this.bucketDuration));
         if (this.bucketTimestamp != null) {
            args.add(TimeSeriesProtocol.TimeSeriesKeyword.BUCKETTIMESTAMP).add(this.bucketTimestamp);
         }

         if (this.empty) {
            args.add(TimeSeriesProtocol.TimeSeriesKeyword.EMPTY);
         }
      }

      args.add(TimeSeriesProtocol.TimeSeriesKeyword.FILTER);

      for (String filter : this.filters) {
         args.add(filter);
      }

      if (this.groupByLabel != null && this.groupByReduce != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.GROUPBY).add(this.groupByLabel).add(TimeSeriesProtocol.TimeSeriesKeyword.REDUCE).add(this.groupByReduce);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSMRangeParams that = (TSMRangeParams)o;
         return this.latest == that.latest
            && this.withLabels == that.withLabels
            && this.bucketDuration == that.bucketDuration
            && this.empty == that.empty
            && Objects.equals(this.fromTimestamp, that.fromTimestamp)
            && Objects.equals(this.toTimestamp, that.toTimestamp)
            && Arrays.equals(this.filterByTimestamps, that.filterByTimestamps)
            && Arrays.equals(this.filterByValues, that.filterByValues)
            && Arrays.equals(this.selectedLabels, that.selectedLabels)
            && Objects.equals(this.count, that.count)
            && Arrays.equals(this.align, that.align)
            && this.aggregationType == that.aggregationType
            && Arrays.equals(this.bucketTimestamp, that.bucketTimestamp)
            && Arrays.equals(this.filters, that.filters)
            && Objects.equals(this.groupByLabel, that.groupByLabel)
            && Objects.equals(this.groupByReduce, that.groupByReduce);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.fromTimestamp);
      result = 31 * result + Objects.hashCode(this.toTimestamp);
      result = 31 * result + Boolean.hashCode(this.latest);
      result = 31 * result + Arrays.hashCode(this.filterByTimestamps);
      result = 31 * result + Arrays.hashCode(this.filterByValues);
      result = 31 * result + Boolean.hashCode(this.withLabels);
      result = 31 * result + Arrays.hashCode(this.selectedLabels);
      result = 31 * result + Objects.hashCode(this.count);
      result = 31 * result + Arrays.hashCode(this.align);
      result = 31 * result + Objects.hashCode(this.aggregationType);
      result = 31 * result + Long.hashCode(this.bucketDuration);
      result = 31 * result + Arrays.hashCode(this.bucketTimestamp);
      result = 31 * result + Boolean.hashCode(this.empty);
      result = 31 * result + Arrays.hashCode(this.filters);
      result = 31 * result + Objects.hashCode(this.groupByLabel);
      return 31 * result + Objects.hashCode(this.groupByReduce);
   }
}
