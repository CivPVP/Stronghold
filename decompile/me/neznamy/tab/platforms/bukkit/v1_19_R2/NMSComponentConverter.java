package me.neznamy.tab.platforms.bukkit.v1_19_R2;

import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.MinecraftKey;
import org.jetbrains.annotations.NotNull;

public class NMSComponentConverter extends ComponentConverter<IChatBaseComponent> {
   @NotNull
   public IChatBaseComponent newTextComponent(@NotNull String text) {
      return IChatBaseComponent.b(text);
   }

   @NotNull
   public IChatBaseComponent newTranslatableComponent(@NotNull String key) {
      return IChatBaseComponent.c(key);
   }

   @NotNull
   public IChatBaseComponent newKeybindComponent(@NotNull String keybind) {
      return IChatBaseComponent.d(keybind);
   }

   @NotNull
   public IChatBaseComponent newObjectComponent(@NotNull TabAtlasSprite sprite) {
      return IChatBaseComponent.b("<Object components were added in 1.21.9>");
   }

   @NotNull
   public IChatBaseComponent newObjectComponent(@NotNull TabPlayerSprite sprite) {
      return IChatBaseComponent.b("<Object components were added in 1.21.9>");
   }

   public void applyStyle(@NotNull IChatBaseComponent nmsComponent, @NotNull TabStyle modifier) {
      ((IChatMutableComponent)nmsComponent)
         .c(
            ChatModifier.a
               .a(modifier.getColor() == null ? null : ChatHexColor.a(modifier.getColor().getRgb()))
               .a(modifier.getBold())
               .b(modifier.getItalic())
               .c(modifier.getUnderlined())
               .d(modifier.getStrikethrough())
               .e(modifier.getObfuscated())
               .a(modifier.getFont() == null ? null : MinecraftKey.a(modifier.getFont()))
         );
   }

   public void addSibling(@NotNull IChatBaseComponent parent, @NotNull IChatBaseComponent child) {
      ((IChatMutableComponent)parent).b(child);
   }
}
