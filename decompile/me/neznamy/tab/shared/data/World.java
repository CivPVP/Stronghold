package me.neznamy.tab.shared.data;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class World {
   @NonNull
   private final String name;

   @Contract("!null -> !null")
   public static World byName(@Nullable String name) {
      return name == null ? null : TAB.getInstance().getDataManager().getWorlds().computeIfAbsent(name, World::new);
   }

   @Generated
   private World(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.name = name;
   }

   @NonNull
   @Generated
   public String getName() {
      return this.name;
   }
}
