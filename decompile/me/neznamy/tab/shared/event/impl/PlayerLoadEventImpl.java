package me.neznamy.tab.shared.event.impl;

import lombok.Generated;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerLoadEventImpl implements PlayerLoadEvent {
   @NotNull
   private final TabPlayer player;
   private final boolean join;

   @Generated
   public PlayerLoadEventImpl(@NotNull TabPlayer player, boolean join) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
      this.join = join;
   }

   @NotNull
   @Generated
   public TabPlayer getPlayer() {
      return this.player;
   }

   @Generated
   @Override
   public boolean isJoin() {
      return this.join;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof PlayerLoadEventImpl)) {
         return false;
      }

      PlayerLoadEventImpl other = (PlayerLoadEventImpl)o;
      if (!other.canEqual(this)) {
         return false;
      }

      if (this.isJoin() != other.isJoin()) {
         return false;
      }

      Object this$player = this.getPlayer();
      Object other$player = other.getPlayer();
      return this$player == null ? other$player == null : this$player.equals(other$player);
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof PlayerLoadEventImpl;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + (this.isJoin() ? 79 : 97);
      Object $player = this.getPlayer();
      return result * 59 + ($player == null ? 43 : $player.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "PlayerLoadEventImpl(player=" + this.getPlayer() + ", join=" + this.isJoin() + ")";
   }
}
