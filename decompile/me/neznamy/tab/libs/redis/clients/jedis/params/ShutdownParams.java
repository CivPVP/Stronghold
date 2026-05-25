package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.args.SaveMode;

public class ShutdownParams implements IParams {
   private SaveMode saveMode;
   private boolean now;
   private boolean force;

   public static ShutdownParams shutdownParams() {
      return new ShutdownParams();
   }

   public ShutdownParams saveMode(SaveMode saveMode) {
      this.saveMode = saveMode;
      return this;
   }

   public ShutdownParams nosave() {
      return this.saveMode(SaveMode.NOSAVE);
   }

   public ShutdownParams save() {
      return this.saveMode(SaveMode.SAVE);
   }

   public ShutdownParams now() {
      this.now = true;
      return this;
   }

   public ShutdownParams force() {
      this.force = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.saveMode != null) {
         args.add(this.saveMode);
      }

      if (this.now) {
         args.add(Protocol.Keyword.NOW);
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
         ShutdownParams that = (ShutdownParams)o;
         return this.now == that.now && this.force == that.force && this.saveMode == that.saveMode;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.saveMode, this.now, this.force);
   }
}
