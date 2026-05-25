package me.neznamy.tab.platforms.paper_1_20_5;

import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PaperComponentConverter extends ComponentConverter<Component> {
   @NotNull
   public Component newTextComponent(@NotNull String text) {
      return Component.literal(text);
   }

   @NotNull
   public Component newTranslatableComponent(@NotNull String key) {
      return Component.translatable(key);
   }

   @NotNull
   public Component newKeybindComponent(@NotNull String keybind) {
      return Component.keybind(keybind);
   }

   @NotNull
   public Component newObjectComponent(@NotNull TabAtlasSprite sprite) {
      return Component.literal("<Object components were added in 1.21.9>");
   }

   @NotNull
   public Component newObjectComponent(@NotNull TabPlayerSprite sprite) {
      return Component.literal("<Object components were added in 1.21.9>");
   }

   public void applyStyle(@NotNull Component nmsComponent, @NotNull TabStyle modifier) {
      Style style = Style.EMPTY
         .withColor(modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()))
         .withBold(modifier.getBold())
         .withItalic(modifier.getItalic())
         .withUnderlined(modifier.getUnderlined())
         .withStrikethrough(modifier.getStrikethrough())
         .withObfuscated(modifier.getObfuscated())
         .withFont(modifier.getFont() == null ? null : ResourceLocation.tryParse(modifier.getFont()));
      ((MutableComponent)nmsComponent).setStyle(style);
   }

   public void addSibling(@NotNull Component parent, @NotNull Component child) {
      ((MutableComponent)parent).append(child);
   }
}
