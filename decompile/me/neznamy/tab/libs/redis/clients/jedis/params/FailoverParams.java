package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.HostAndPort;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class FailoverParams implements IParams {
   private HostAndPort to;
   private boolean force;
   private Long timeout;

   public static FailoverParams failoverParams() {
      return new FailoverParams();
   }

   public FailoverParams to(String host, int port) {
      return this.to(new HostAndPort(host, port));
   }

   public FailoverParams to(HostAndPort to) {
      this.to = to;
      return this;
   }

   public FailoverParams force() {
      this.force = true;
      return this;
   }

   public FailoverParams timeout(long timeout) {
      this.timeout = timeout;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.to != null) {
         args.add(Protocol.Keyword.TO).add(this.to.getHost()).add(this.to.getPort());
      }

      if (this.force) {
         if (this.to == null || this.timeout == null) {
            throw new IllegalArgumentException("FAILOVER with force option requires both a timeout and target HOST and IP.");
         }

         args.add(Protocol.Keyword.FORCE);
      }

      if (this.timeout != null) {
         args.add(Protocol.Keyword.TIMEOUT).add(this.timeout);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FailoverParams that = (FailoverParams)o;
         return this.force == that.force && Objects.equals(this.to, that.to) && Objects.equals(this.timeout, that.timeout);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.to, this.force, this.timeout);
   }
}
