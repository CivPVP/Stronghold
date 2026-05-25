package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class XClaimParams implements IParams {
   private Long idleTime;
   private Long idleUnixTime;
   private Integer retryCount;
   private boolean force;

   public static XClaimParams xClaimParams() {
      return new XClaimParams();
   }

   public XClaimParams idle(long idleTime) {
      this.idleTime = idleTime;
      return this;
   }

   public XClaimParams time(long idleUnixTime) {
      this.idleUnixTime = idleUnixTime;
      return this;
   }

   public XClaimParams retryCount(int count) {
      this.retryCount = count;
      return this;
   }

   public XClaimParams force() {
      this.force = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.idleTime != null) {
         args.add(Protocol.Keyword.IDLE).add(this.idleTime);
      }

      if (this.idleUnixTime != null) {
         args.add(Protocol.Keyword.TIME).add(this.idleUnixTime);
      }

      if (this.retryCount != null) {
         args.add(Protocol.Keyword.RETRYCOUNT).add(this.retryCount);
      }

      if (this.force) {
         args.add(Protocol.Keyword.FORCE);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XClaimParams that = (XClaimParams)o;
         return this.force == that.force
            && Objects.equals(this.idleTime, that.idleTime)
            && Objects.equals(this.idleUnixTime, that.idleUnixTime)
            && Objects.equals(this.retryCount, that.retryCount);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.idleTime, this.idleUnixTime, this.retryCount, this.force);
   }
}
