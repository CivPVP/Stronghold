package me.neznamy.tab.libs.redis.clients.jedis.timeseries;

import java.util.Arrays;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class TSMGetParams implements IParams {
   private boolean latest;
   private boolean withLabels;
   private String[] selectedLabels;

   public static TSMGetParams multiGetParams() {
      return new TSMGetParams();
   }

   public TSMGetParams latest() {
      this.latest = true;
      return this;
   }

   public TSMGetParams withLabels(boolean withLabels) {
      this.withLabels = withLabels;
      return this;
   }

   public TSMGetParams withLabels() {
      return this.withLabels(true);
   }

   public TSMGetParams selectedLabels(String... labels) {
      this.selectedLabels = labels;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.latest) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.LATEST);
      }

      if (this.withLabels) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.WITHLABELS);
      } else if (this.selectedLabels != null) {
         args.add(TimeSeriesProtocol.TimeSeriesKeyword.SELECTED_LABELS);

         for (String label : this.selectedLabels) {
            args.add(label);
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSMGetParams that = (TSMGetParams)o;
         return this.latest == that.latest && this.withLabels == that.withLabels && Arrays.equals(this.selectedLabels, that.selectedLabels);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Boolean.hashCode(this.latest);
      result = 31 * result + Boolean.hashCode(this.withLabels);
      return 31 * result + Arrays.hashCode(this.selectedLabels);
   }
}
