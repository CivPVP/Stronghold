package me.neznamy.tab.platforms.bukkit.provider;

import java.util.UUID;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import org.jetbrains.annotations.NotNull;

public abstract class ComponentConverter<T> {
   protected final UUID NIL_UUID = new UUID(0L, 0L);

   @NotNull
   public T convert(@NotNull TabComponent component) {
      T nmsComponent;
      if (component instanceof TabTextComponent) {
         nmsComponent = this.newTextComponent(((TabTextComponent)component).getText());
      } else if (component instanceof TabTranslatableComponent) {
         nmsComponent = this.newTranslatableComponent(((TabTranslatableComponent)component).getKey());
      } else if (component instanceof TabKeybindComponent) {
         nmsComponent = this.newKeybindComponent(((TabKeybindComponent)component).getKeybind());
      } else {
         if (!(component instanceof TabObjectComponent)) {
            throw new IllegalArgumentException("Unexpected component type: " + component.getClass().getName());
         }

         if (((TabObjectComponent)component).getContents() instanceof TabAtlasSprite) {
            nmsComponent = this.newObjectComponent((TabAtlasSprite)((TabObjectComponent)component).getContents());
         } else {
            if (!(((TabObjectComponent)component).getContents() instanceof TabPlayerSprite)) {
               throw new IllegalArgumentException("Unexpected object component type: " + ((TabObjectComponent)component).getContents().getClass().getName());
            }

            nmsComponent = this.newObjectComponent((TabPlayerSprite)((TabObjectComponent)component).getContents());
         }
      }

      this.applyStyle(nmsComponent, component.getModifier());

      for (TabComponent extra : component.getExtra()) {
         this.addSibling(nmsComponent, this.convert(extra));
      }

      return nmsComponent;
   }

   @NotNull
   public abstract T newTextComponent(@NotNull String var1);

   @NotNull
   public abstract T newTranslatableComponent(@NotNull String var1);

   @NotNull
   public abstract T newKeybindComponent(@NotNull String var1);

   @NotNull
   public abstract T newObjectComponent(@NotNull TabAtlasSprite var1);

   @NotNull
   public abstract T newObjectComponent(@NotNull TabPlayerSprite var1);

   public abstract void applyStyle(@NotNull T var1, @NotNull TabStyle var2);

   public abstract void addSibling(@NotNull T var1, @NotNull T var2);
}
