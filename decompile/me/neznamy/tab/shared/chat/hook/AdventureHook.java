package me.neznamy.tab.shared.chat.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.Style.Builder;
import net.kyori.adventure.text.format.TextDecoration.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdventureHook {
   private static final boolean SHADOW_COLOR_AVAILABLE;
   private static final boolean OBJECT_COMPONENTS_AVAILABLE = ReflectionUtils.classExists("net.kyori.adventure.text.ObjectComponent");

   @NotNull
   public static Component convert(@NotNull TabComponent component) {
      Builder style = Style.style()
         .color(component.getModifier().getColor() == null ? null : TextColor.color(component.getModifier().getColor().getRgb()))
         .decoration(TextDecoration.BOLD, getDecoration(component.getModifier().getBold()))
         .decoration(TextDecoration.ITALIC, getDecoration(component.getModifier().getItalic()))
         .decoration(TextDecoration.UNDERLINED, getDecoration(component.getModifier().getUnderlined()))
         .decoration(TextDecoration.STRIKETHROUGH, getDecoration(component.getModifier().getStrikethrough()))
         .decoration(TextDecoration.OBFUSCATED, getDecoration(component.getModifier().getObfuscated()))
         .font(component.getModifier().getFont() == null ? null : Key.key(component.getModifier().getFont()));
      if (SHADOW_COLOR_AVAILABLE) {
         AdventureShadowHook.setShadowColor(style, component.getModifier().getShadowColor());
      }

      List<Component> list = new ArrayList<>();

      for (TabComponent extra : component.getExtra()) {
         list.add(convert(extra));
      }

      if (component instanceof TabTextComponent) {
         return Component.text(((TabTextComponent)component).getText(), style.build()).children(list);
      } else if (component instanceof TabTranslatableComponent) {
         return Component.translatable(((TabTranslatableComponent)component).getKey(), style.build()).children(list);
      } else if (component instanceof TabKeybindComponent) {
         return Component.keybind(((TabKeybindComponent)component).getKeybind(), style.build()).children(list);
      } else if (component instanceof TabObjectComponent) {
         return (Component)(OBJECT_COMPONENTS_AVAILABLE
            ? AdventureObjectHook.convert((TabObjectComponent)component)
            : Component.text("<Object components are not supported in your version of Adventure library>"));
      } else {
         throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
      }
   }

   @NotNull
   private static State getDecoration(@Nullable Boolean state) {
      if (state == null) {
         return State.NOT_SET;
      } else {
         return state ? State.TRUE : State.FALSE;
      }
   }

   @NotNull
   public static TabComponent convert(@NotNull Component component) {
      TabComponent tabComponent;
      if (component instanceof TextComponent) {
         tabComponent = new TabTextComponent(((TextComponent)component).content());
      } else if (component instanceof TranslatableComponent) {
         tabComponent = TabComponent.translatable(((TranslatableComponent)component).key());
      } else if (component instanceof KeybindComponent) {
         tabComponent = TabComponent.keybind(((KeybindComponent)component).keybind());
      } else {
         if (!(component instanceof ObjectComponent)) {
            throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
         }

         tabComponent = AdventureObjectHook.convert((ObjectComponent)component);
      }

      Map<TextDecoration, State> decorations = component.style().decorations();
      TextColor color = component.color();
      Key font = component.font();
      tabComponent.setModifier(
         new TabStyle(
            color == null ? null : new TabTextColor(color.value()),
            SHADOW_COLOR_AVAILABLE ? AdventureShadowHook.getShadowColor(component) : null,
            getDecoration(decorations.get(TextDecoration.BOLD)),
            getDecoration(decorations.get(TextDecoration.ITALIC)),
            getDecoration(decorations.get(TextDecoration.UNDERLINED)),
            getDecoration(decorations.get(TextDecoration.STRIKETHROUGH)),
            getDecoration(decorations.get(TextDecoration.OBFUSCATED)),
            font == null ? null : font.asString()
         )
      );

      for (Component extra : component.children()) {
         tabComponent.addExtra(convert(extra));
      }

      tabComponent.setAdventureComponent(component);
      return tabComponent;
   }

   @Nullable
   private static Boolean getDecoration(@Nullable State state) {
      return state != null && state != State.NOT_SET ? state == State.TRUE : null;
   }

   static {
      boolean value;
      try {
         Component.class.getDeclaredMethod("shadowColor");
         value = true;
      } catch (Throwable t) {
         value = false;
      }

      SHADOW_COLOR_AVAILABLE = value;
   }
}
