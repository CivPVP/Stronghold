package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class RestoreParams implements IParams {
   private boolean replace;
   private boolean absTtl;
   private Long idleTime;
   private Long frequency;

   public static RestoreParams restoreParams() {
      return new RestoreParams();
   }

   public RestoreParams replace() {
      this.replace = true;
      return this;
   }

   public RestoreParams absTtl() {
      this.absTtl = true;
      return this;
   }

   public RestoreParams idleTime(long idleTime) {
      this.idleTime = idleTime;
      return this;
   }

   public RestoreParams frequency(long frequency) {
      this.frequency = frequency;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.replace) {
         args.add(Protocol.Keyword.REPLACE);
      }

      if (this.absTtl) {
         args.add(Protocol.Keyword.ABSTTL);
      }

      if (this.idleTime != null) {
         args.add(Protocol.Keyword.IDLETIME).add(this.idleTime);
      }

      if (this.frequency != null) {
         args.add(Protocol.Keyword.FREQ).add(this.frequency);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RestoreParams that = (RestoreParams)o;
         return this.replace == that.replace
            && this.absTtl == that.absTtl
            && Objects.equals(this.idleTime, that.idleTime)
            && Objects.equals(this.frequency, that.frequency);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.replace, this.absTtl, this.idleTime, this.frequency);
   }
}
