package me.neznamy.tab.libs.org.apache.commons.pool2;

import java.time.Instant;

public interface TrackedUse {
   @Deprecated
   long getLastUsed();

   default Instant getLastUsedInstant() {
      return Instant.ofEpochMilli(this.getLastUsed());
   }
}
