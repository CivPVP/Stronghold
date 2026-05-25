package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.Arrays;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class TSRangeParams implements IParams {
   private Long fromTimestamp;
   private Long toTimestamp;
   private boolean latest;
   private long[] filterByTimestamps;
   private double[] filterByValues;
   private Integer count;
   private byte[] align;
   private AggregationType aggregationType;
   private long bucketDuration;
   private byte[] bucketTimestamp;
   private boolean empty;

   public TSRangeParams(long fromTimestamp, long toTimestamp) {
      this.fromTimestamp = fromTimestamp;
      this.toTimestamp = toTimestamp;
   }

   public static TSRangeParams rangeParams(long fromTimestamp, long toTimestamp) {
      return new TSRangeParams(fromTimestamp, toTimestamp);
   }

   public TSRangeParams() {
   }

   public static TSRangeParams rangeParams() {
      return new TSRangeParams();
   }

   public TSRangeParams fromTimestamp(long fromTimestamp) {
      this.fromTimestamp = fromTimestamp;
      return this;
   }

   public TSRangeParams toTimestamp(long toTimestamp) {
      this.toTimestamp = toTimestamp;
      return this;
   }

   public TSRangeParams latest() {
      this.latest = true;
      return this;
   }

   public TSRangeParams filterByTS(long... timestamps) {
      this.filterByTimestamps = timestamps;
      return this;
   }

   public TSRangeParams filterByValues(double min, double max) {
      this.filterByValues = new double[]{min, max};
      return this;
   }

   public TSRangeParams count(int count) {
      this.count = count;
      return this;
   }

   private TSRangeParams align(byte[] raw) {
      this.align = raw;
      return this;
   }

   public TSRangeParams align(long timestamp) {
      return this.align(Protocol.toByteArray(timestamp));
   }

   public TSRangeParams alignStart() {
      return this.align(TimeSeriesProtocol.MINUS);
   }

   public TSRangeParams alignEnd() {
      return this.align(TimeSeriesProtocol.PLUS);
   }

   public TSRangeParams aggregation(AggregationType aggregationType, long bucketDuration) {
      this.aggregationType = aggregationType;
      this.bucketDuration = bucketDuration;
      return this;
   }

   public TSRangeParams bucketTimestamp(String bucketTimestamp) {
      this.bucketTimestamp = SafeEncoder.encode(bucketTimestamp);
      return this;
   }

   public TSRangeParams bucketTimestampLow() {
      this.bucketTimestamp = TimeSeriesProtocol.MINUS;
      return this;
   }

   public TSRangeParams bucketTimestampHigh() {
      this.bucketTimestamp = TimeSeriesProtocol.PLUS;
      return this;
   }

   public TSRangeParams bucketTimestampMid() {
      this.bucketTimestamp = Protocol.BYTES_TILDE;
      return this;
   }

   public TSRangeParams empty() {
      this.empty = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
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
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSRangeParams that = (TSRangeParams)o;
         return this.latest == that.latest
            && this.bucketDuration == that.bucketDuration
            && this.empty == that.empty
            && Objects.equals(this.fromTimestamp, that.fromTimestamp)
            && Objects.equals(this.toTimestamp, that.toTimestamp)
            && Arrays.equals(this.filterByTimestamps, that.filterByTimestamps)
            && Arrays.equals(this.filterByValues, that.filterByValues)
            && Objects.equals(this.count, that.count)
            && Arrays.equals(this.align, that.align)
            && this.aggregationType == that.aggregationType
            && Arrays.equals(this.bucketTimestamp, that.bucketTimestamp);
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
      result = 31 * result + Objects.hashCode(this.count);
      result = 31 * result + Arrays.hashCode(this.align);
      result = 31 * result + Objects.hashCode(this.aggregationType);
      result = 31 * result + Long.hashCode(this.bucketDuration);
      result = 31 * result + Arrays.hashCode(this.bucketTimestamp);
      return 31 * result + Boolean.hashCode(this.empty);
   }
}
