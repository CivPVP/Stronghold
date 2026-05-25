package me.neznamy.tab.shared.data;

import java.util.List;
import lombok.Generated;
import lombok.NonNull;

public class ServerGroup {
   @NonNull
   private final String name;
   @NonNull
   private final List<String> patterns;

   @Generated
   public ServerGroup(@NonNull String name, @NonNull List<String> patterns) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      if (patterns == null) {
         throw new NullPointerException("patterns is marked non-null but is null");
      }

      this.name = name;
      this.patterns = patterns;
   }

   @NonNull
   @Generated
   public String getName() {
      return this.name;
   }

   @NonNull
   @Generated
   public List<String> getPatterns() {
      return this.patterns;
   }
}
