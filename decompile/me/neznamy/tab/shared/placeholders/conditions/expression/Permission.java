package me.neznamy.tab.shared.placeholders.conditions.expression;

import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class Permission extends ConditionalExpression {
   private final String permission;

   @Override
   public boolean isMet(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return p.hasPermission(this.permission);
      }
   }

   @NotNull
   @Override
   public ConditionalExpression invert() {
      return new NotPermission(this.permission);
   }

   @NotNull
   @Override
   public String toShortFormat() {
      return "permission:" + this.permission;
   }

   @Generated
   public Permission(String permission) {
      this.permission = permission;
   }
}
