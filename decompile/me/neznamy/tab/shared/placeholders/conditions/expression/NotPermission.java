package me.neznamy.tab.shared.placeholders.conditions.expression;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class NotPermission extends ConditionalExpression {
   private final String permission;

   @Override
   public boolean isMet(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return !p.hasPermission(this.permission);
      }
   }

   @NotNull
   @Override
   public ConditionalExpression invert() {
      return new Permission(this.permission);
   }

   @NotNull
   @Override
   public String toShortFormat() {
      return "!permission:" + this.permission;
   }

   @Generated
   public NotPermission(String permission) {
      this.permission = permission;
   }
}
