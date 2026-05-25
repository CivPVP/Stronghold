package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class TSAddParams implements IParams {
   private Long retentionPeriod;
   private EncodingFormat encoding;
   private Long chunkSize;
   private DuplicatePolicy duplicatePolicy;
   private DuplicatePolicy onDuplicate;
   private boolean ignore;
   private long ignoreMaxTimediff;
   private double ignoreMaxValDiff;
   private Map<String, String> labels;

   public static TSAddParams addParams() {
      return new TSAddParams();
   }

   public TSAddParams retention(long retentionPeriod) {
      this.retentionPeriod = retentionPeriod;
      return this;
   }

   public TSAddParams encoding(EncodingFormat encoding) {
      this.encoding = encoding;
      return this;
   }

   public TSAddParams chunkSize(long chunkSize) {
      this.chunkSize = chunkSize;
      return this;
   }

   public TSAddParams duplicatePolicy(DuplicatePolicy duplicatePolicy) {
      this.duplicatePolicy = duplicatePolicy;
      return this;
   }

   public TSAddParams onDuplicate(DuplicatePolicy onDuplicate) {
      this.onDuplicate = onDuplicate;
      return this;
   }

   public TSAddParams ignore(long maxTimediff, double maxValDiff) {
      this.ignore = true;
      this.ignoreMaxTimediff = maxTimediff;
      this.ignoreMaxValDiff = maxValDiff;
      return this;
   }

   public TSAddParams labels(Map<String, String> labels) {
      this.labels = labels;
      return this;
   }

   public TSAddParams label(String label, String value) {
      if (this.labels == null) {
         this.labels = new LinkedHashMap<>();
      }

      this.labels.put(label, value);
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.retentionPeriod != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.RETENTION).add(Protocol.toByteArray(this.retentionPeriod));
      }

      if (this.encoding != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.ENCODING).add(this.encoding);
      }

      if (this.chunkSize != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.CHUNK_SIZE).add(Protocol.toByteArray(this.chunkSize));
      }

      if (this.duplicatePolicy != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.DUPLICATE_POLICY).add(this.duplicatePolicy);
      }

      if (this.duplicatePolicy != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.DUPLICATE_POLICY).add(this.duplicatePolicy);
      }

      if (this.onDuplicate != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.ON_DUPLICATE).add(this.onDuplicate);
      }

      if (this.ignore) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.IGNORE).add(this.ignoreMaxTimediff).add(this.ignoreMaxValDiff);
      }

      if (this.labels != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.LABELS);
         this.labels.entrySet().forEach(entry -> args.add(entry.getKey()).add(entry.getValue()));
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSAddParams that = (TSAddParams)o;
         return this.ignore == that.ignore
            && this.ignoreMaxTimediff == that.ignoreMaxTimediff
            && Double.compare(this.ignoreMaxValDiff, that.ignoreMaxValDiff) == 0
            && Objects.equals(this.retentionPeriod, that.retentionPeriod)
            && this.encoding == that.encoding
            && Objects.equals(this.chunkSize, that.chunkSize)
            && this.duplicatePolicy == that.duplicatePolicy
            && this.onDuplicate == that.onDuplicate
            && Objects.equals(this.labels, that.labels);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.retentionPeriod);
      result = 31 * result + Objects.hashCode(this.encoding);
      result = 31 * result + Objects.hashCode(this.chunkSize);
      result = 31 * result + Objects.hashCode(this.duplicatePolicy);
      result = 31 * result + Objects.hashCode(this.onDuplicate);
      result = 31 * result + Boolean.hashCode(this.ignore);
      result = 31 * result + Long.hashCode(this.ignoreMaxTimediff);
      result = 31 * result + Double.hashCode(this.ignoreMaxValDiff);
      return 31 * result + Objects.hashCode(this.labels);
   }
}
