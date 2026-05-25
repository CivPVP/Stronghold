package me.neznamy.tab.shared.chat.component.object;

import lombok.Generated;
import lombok.NonNull;

public class TabAtlasSprite implements ObjectInfo {
   @NonNull
   private final String atlas;
   @NonNull
   private final String sprite;

   @Generated
   public TabAtlasSprite(@NonNull String atlas, @NonNull String sprite) {
      if (atlas == null) {
         throw new NullPointerException("atlas is marked non-null but is null");
      }

      if (sprite == null) {
         throw new NullPointerException("sprite is marked non-null but is null");
      }

      this.atlas = atlas;
      this.sprite = sprite;
   }

   @NonNull
   @Generated
   public String getAtlas() {
      return this.atlas;
   }

   @NonNull
   @Generated
   public String getSprite() {
      return this.sprite;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof TabAtlasSprite)) {
         return false;
      } else {
         TabAtlasSprite other = (TabAtlasSprite)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$atlas = this.getAtlas();
            Object other$atlas = other.getAtlas();
            if (this$atlas == null ? other$atlas == null : this$atlas.equals(other$atlas)) {
               Object this$sprite = this.getSprite();
               Object other$sprite = other.getSprite();
               return this$sprite == null ? other$sprite == null : this$sprite.equals(other$sprite);
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof TabAtlasSprite;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $atlas = this.getAtlas();
      result = result * 59 + ($atlas == null ? 43 : $atlas.hashCode());
      Object $sprite = this.getSprite();
      return result * 59 + ($sprite == null ? 43 : $sprite.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "TabAtlasSprite(atlas=" + this.getAtlas() + ", sprite=" + this.getSprite() + ")";
   }
}
