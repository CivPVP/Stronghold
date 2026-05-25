package me.neznamy.tab.shared.event.impl;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Generated;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.plugin.PlaceholderRegisterEvent;
import org.jetbrains.annotations.NotNull;

public class TabPlaceholderRegisterEvent implements PlaceholderRegisterEvent {
   @NotNull
   private final String identifier;
   private Supplier<String> serverPlaceholder;
   private Function<TabPlayer, String> playerPlaceholder;
   private BiFunction<TabPlayer, TabPlayer, String> relationalPlaceholder;

   @Generated
   public TabPlaceholderRegisterEvent(@NotNull String identifier) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      this.identifier = identifier;
   }

   @NotNull
   @Generated
   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   public Supplier<String> getServerPlaceholder() {
      return this.serverPlaceholder;
   }

   @Generated
   public Function<TabPlayer, String> getPlayerPlaceholder() {
      return this.playerPlaceholder;
   }

   @Generated
   public BiFunction<TabPlayer, TabPlayer, String> getRelationalPlaceholder() {
      return this.relationalPlaceholder;
   }

   @Generated
   @Override
   public void setServerPlaceholder(Supplier<String> serverPlaceholder) {
      this.serverPlaceholder = serverPlaceholder;
   }

   @Generated
   @Override
   public void setPlayerPlaceholder(Function<TabPlayer, String> playerPlaceholder) {
      this.playerPlaceholder = playerPlaceholder;
   }

   @Generated
   @Override
   public void setRelationalPlaceholder(BiFunction<TabPlayer, TabPlayer, String> relationalPlaceholder) {
      this.relationalPlaceholder = relationalPlaceholder;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof TabPlaceholderRegisterEvent)) {
         return false;
      }

      TabPlaceholderRegisterEvent other = (TabPlaceholderRegisterEvent)o;
      if (!other.canEqual(this)) {
         return false;
      }

      Object this$identifier = this.getIdentifier();
      Object other$identifier = other.getIdentifier();
      if (this$identifier == null ? other$identifier == null : this$identifier.equals(other$identifier)) {
         Object this$serverPlaceholder = this.getServerPlaceholder();
         Object other$serverPlaceholder = other.getServerPlaceholder();
         if (this$serverPlaceholder == null ? other$serverPlaceholder == null : this$serverPlaceholder.equals(other$serverPlaceholder)) {
            Object this$playerPlaceholder = this.getPlayerPlaceholder();
            Object other$playerPlaceholder = other.getPlayerPlaceholder();
            if (this$playerPlaceholder == null ? other$playerPlaceholder == null : this$playerPlaceholder.equals(other$playerPlaceholder)) {
               Object this$relationalPlaceholder = this.getRelationalPlaceholder();
               Object other$relationalPlaceholder = other.getRelationalPlaceholder();
               return this$relationalPlaceholder == null ? other$relationalPlaceholder == null : this$relationalPlaceholder.equals(other$relationalPlaceholder);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof TabPlaceholderRegisterEvent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $identifier = this.getIdentifier();
      result = result * 59 + ($identifier == null ? 43 : $identifier.hashCode());
      Object $serverPlaceholder = this.getServerPlaceholder();
      result = result * 59 + ($serverPlaceholder == null ? 43 : $serverPlaceholder.hashCode());
      Object $playerPlaceholder = this.getPlayerPlaceholder();
      result = result * 59 + ($playerPlaceholder == null ? 43 : $playerPlaceholder.hashCode());
      Object $relationalPlaceholder = this.getRelationalPlaceholder();
      return result * 59 + ($relationalPlaceholder == null ? 43 : $relationalPlaceholder.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "TabPlaceholderRegisterEvent(identifier="
         + this.getIdentifier()
         + ", serverPlaceholder="
         + this.getServerPlaceholder()
         + ", playerPlaceholder="
         + this.getPlayerPlaceholder()
         + ", relationalPlaceholder="
         + this.getRelationalPlaceholder()
         + ")";
   }
}
