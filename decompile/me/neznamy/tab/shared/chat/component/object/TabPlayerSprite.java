package me.neznamy.tab.shared.chat.component.object;

import java.util.UUID;
import lombok.Generated;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.Nullable;

public class TabPlayerSprite implements ObjectInfo {
   @Nullable
   private final UUID id;
   @Nullable
   private final String name;
   @Nullable
   private final TabList.Skin skin;
   private final boolean showHat;

   @Nullable
   @Generated
   public UUID getId() {
      return this.id;
   }

   @Nullable
   @Generated
   public String getName() {
      return this.name;
   }

   @Nullable
   @Generated
   public TabList.Skin getSkin() {
      return this.skin;
   }

   @Generated
   public boolean isShowHat() {
      return this.showHat;
   }

   @Generated
   public TabPlayerSprite(@Nullable UUID id, @Nullable String name, @Nullable TabList.Skin skin, boolean showHat) {
      this.id = id;
      this.name = name;
      this.skin = skin;
      this.showHat = showHat;
   }
}
