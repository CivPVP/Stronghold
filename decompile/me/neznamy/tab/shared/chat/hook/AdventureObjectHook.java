package me.neznamy.tab.shared.chat.hook;

import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.object.ObjectInfo;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents.ProfileProperty;
import org.jetbrains.annotations.NotNull;

public class AdventureObjectHook {
   @NotNull
   public static Component convert(@NotNull TabObjectComponent component) {
      ObjectInfo info = component.getContents();
      if (info instanceof TabAtlasSprite) {
         return Component.object(ObjectContents.sprite(Key.key(((TabAtlasSprite)info).getAtlas()), Key.key(((TabAtlasSprite)info).getSprite())));
      }

      if (info instanceof TabPlayerSprite) {
         TabPlayerSprite sprite = (TabPlayerSprite)info;
         List<ProfileProperty> properties;
         if (sprite.getSkin() == null) {
            properties = Collections.emptyList();
         } else {
            properties = Collections.singletonList(PlayerHeadObjectContents.property("textures", sprite.getSkin().getValue(), sprite.getSkin().getSignature()));
         }

         return Component.object(
            ObjectContents.playerHead().id(sprite.getId()).name(sprite.getName()).profileProperties(properties).hat(sprite.isShowHat()).build()
         );
      } else {
         throw new UnsupportedOperationException("ObjectComponent with " + info.getClass().getName() + " is not supported");
      }
   }

   @NotNull
   public static TabComponent convert(@NotNull ObjectComponent component) {
      ObjectContents contents = component.contents();
      if (contents instanceof SpriteObjectContents) {
         return TabComponent.atlasSprite(((SpriteObjectContents)contents).atlas().asString(), ((SpriteObjectContents)contents).sprite().asString());
      } else if (contents instanceof PlayerHeadObjectContents) {
         PlayerHeadObjectContents head = (PlayerHeadObjectContents)contents;
         ProfileProperty skin = head.profileProperties().stream().filter(p -> p.name().equals("textures")).findFirst().orElse(null);
         return TabComponent.head(
            new TabPlayerSprite(head.id(), head.name(), skin == null ? null : new TabList.Skin(skin.value(), skin.signature()), head.hat())
         );
      } else {
         throw new UnsupportedOperationException("ObjectComponent with " + contents.getClass().getName() + " is not supported");
      }
   }
}
