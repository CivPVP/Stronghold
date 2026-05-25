package me.neznamy.tab.platforms.bukkit.v1_16_R2;

import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.server.v1_16_R2.ChatComponentKeybind;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.ChatHexColor;
import net.minecraft.server.v1_16_R2.ChatMessage;
import net.minecraft.server.v1_16_R2.ChatModifier;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.IChatMutableComponent;
import org.jetbrains.annotations.NotNull;

public class NMSComponentConverter extends ComponentConverter<IChatBaseComponent> {
   @NotNull
   public IChatBaseComponent newTextComponent(@NotNull String text) {
      return new ChatComponentText(text);
   }

   @NotNull
   public IChatBaseComponent newTranslatableComponent(@NotNull String key) {
      return new ChatMessage(key);
   }

   @NotNull
   public IChatBaseComponent newKeybindComponent(@NotNull String keybind) {
      return new ChatComponentKeybind(keybind);
   }

   @NotNull
   public IChatBaseComponent newObjectComponent(@NotNull TabAtlasSprite sprite) {
      return new ChatComponentText("<Object components were added in 1.21.9>");
   }

   @NotNull
   public IChatBaseComponent newObjectComponent(@NotNull TabPlayerSprite sprite) {
      return new ChatComponentText("<Object components were added in 1.21.9>");
   }

   public void applyStyle(@NotNull IChatBaseComponent nmsComponent, @NotNull TabStyle modifier) {
      ((IChatMutableComponent)nmsComponent)
         .setChatModifier(
            ChatModifier.a
               .setColor(modifier.getColor() == null ? null : ChatHexColor.a(modifier.getColor().getRgb()))
               .setBold(modifier.getBold())
               .setItalic(modifier.getItalic())
               .setUnderline(modifier.getUnderlined())
               .setStrikethrough(modifier.getStrikethrough())
               .setRandom(modifier.getObfuscated())
         );
   }

   public void addSibling(@NotNull IChatBaseComponent parent, @NotNull IChatBaseComponent child) {
      ((IChatMutableComponent)parent).addSibling(child);
   }
}
