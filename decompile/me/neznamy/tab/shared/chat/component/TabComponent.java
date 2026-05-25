package me.neznamy.tab.shared.chat.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.chat.hook.AdventureHook;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.TriFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TabComponent {
   public static final LegacyTextComponent EMPTY_LEGACY_TEXT = new LegacyTextComponent("");
   private static final TriFunction<TabTextColor, String, TabTextColor, String> TABGradientFormatter = (start, text, end) -> {
      if (text.length() == 1) {
         return "#" + start.getHexCode() + text;
      }

      StringBuilder sb = new StringBuilder();
      List<Character> characters = new ArrayList<>();
      List<TabStyle> modifiers = new ArrayList<>();
      TabStyle modifier = new TabStyle();

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == 167 && i < text.length() - 1) {
            switch (text.charAt(i + 1)) {
               case 'k':
                  modifier.setObfuscated(true);
                  i++;
                  break;
               case 'l':
                  modifier.setBold(true);
                  i++;
                  break;
               case 'm':
                  modifier.setStrikethrough(true);
                  i++;
                  break;
               case 'n':
                  modifier.setUnderlined(true);
                  i++;
                  break;
               case 'o':
                  modifier.setItalic(true);
                  i++;
                  break;
               case 'p':
               case 'q':
               default:
                  characters.add('§');
                  modifiers.add(new TabStyle(modifier));
                  break;
               case 'r':
                  modifier = new TabStyle();
                  i++;
            }
         } else {
            characters.add(c);
            modifiers.add(new TabStyle(modifier));
         }
      }

      int length = characters.size();

      for (int i = 0; i < length; i++) {
         int red = (int)(start.getRed() + (float)(end.getRed() - start.getRed()) / (length - 1) * i);
         int green = (int)(start.getGreen() + (float)(end.getGreen() - start.getGreen()) / (length - 1) * i);
         int blue = (int)(start.getBlue() + (float)(end.getBlue() - start.getBlue()) / (length - 1) * i);
         sb.append(String.format("#%02X%02X%02X", red, green, blue));
         sb.append(modifiers.get(i).getMagicCodes());
         sb.append(characters.get(i));
      }

      return sb.toString();
   };
   private static final Function<TabTextColor, String> TABRGBFormatter = color -> "#" + color.getHexCode();
   private static final Pattern fontPattern = Pattern.compile("<font:(.*?)>(.*?)</font>");
   private static final Pattern ATLAS_PATTERN = Pattern.compile("<sprite:(?:\"([^\"]+)\"|([^:]+)):(?:\"([^\"]+)\"|([^>]+))>");
   private static final Pattern HEAD_PATTERN = Pattern.compile("<head:([^>]+)>");
   @Nullable
   private Object converted;
   @Nullable
   private Component adventureComponent;
   @Nullable
   private Object fixedFormat;
   @Nullable
   private Object textHolder;
   @Nullable
   private TabStyle lastStyle;
   @NotNull
   protected TabStyle modifier = new TabStyle();
   protected List<TabComponent> extra;

   public List<TabComponent> getExtra() {
      return this.extra == null ? Collections.emptyList() : this.extra;
   }

   public void addExtra(@NotNull TabComponent extra) {
      if (this.extra == null) {
         this.extra = new ArrayList<>();
      }

      this.extra.add(extra);
   }

   @NotNull
   public <T> T convert() {
      if (this.converted == null) {
         this.converted = TAB.getInstance().getPlatform().convertComponent(this);
      }

      return (T)this.converted;
   }

   @NotNull
   public Component toAdventure() {
      if (this.adventureComponent == null) {
         this.adventureComponent = AdventureHook.convert(this);
      }

      return this.adventureComponent;
   }

   public <F, C> F toFixedFormat(@NotNull Function<C, F> createFunction) {
      try {
         if (this.fixedFormat == null) {
            this.fixedFormat = createFunction.apply(this.convert());
         }

         return (F)this.fixedFormat;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @NotNull
   public <T> T toTextHolder(@NotNull Function<TabComponent, T> convertFunction) {
      if (this.textHolder == null) {
         this.textHolder = convertFunction.apply(this);
      }

      return (T)this.textHolder;
   }

   @NotNull
   public TabStyle getLastStyle() {
      if (this.lastStyle == null) {
         this.lastStyle = this.fetchLastStyle();
      }

      return this.lastStyle;
   }

   @NotNull
   public abstract String toLegacyText();

   @NotNull
   protected TabStyle fetchLastStyle() {
      TabStyle lastStyle = new TabStyle(this.modifier);
      if (this.extra != null && !this.extra.isEmpty()) {
         TabStyle childStyle = this.extra.get(this.extra.size() - 1).fetchLastStyle();
         if (childStyle.getColor() != null) {
            lastStyle.setColor(childStyle.getColor());
         }

         if (childStyle.getShadowColor() != null) {
            lastStyle.setShadowColor(childStyle.getShadowColor());
         }

         if (childStyle.getBold() != null) {
            lastStyle.setBold(childStyle.getBold());
         }

         if (childStyle.getItalic() != null) {
            lastStyle.setItalic(childStyle.getItalic());
         }

         if (childStyle.getUnderlined() != null) {
            lastStyle.setUnderlined(childStyle.getUnderlined());
         }

         if (childStyle.getStrikethrough() != null) {
            lastStyle.setStrikethrough(childStyle.getStrikethrough());
         }

         if (childStyle.getObfuscated() != null) {
            lastStyle.setObfuscated(childStyle.getObfuscated());
         }

         if (childStyle.getFont() != null) {
            lastStyle.setFont(childStyle.getFont());
         }
      }

      return lastStyle;
   }

   @NotNull
   public static TabTextComponent fromColoredText(@NotNull String originalText) {
      String remainingText = originalText;
      List<TabComponent> components = new ArrayList<>();

      while (!remainingText.isEmpty()) {
         Matcher m = fontPattern.matcher(remainingText);
         if (!m.find()) {
            components.addAll(toComponentArray(remainingText, null));
            break;
         }

         if (m.start() > 0) {
            components.addAll(toComponentArray(remainingText.substring(0, m.start()), null));
         }

         String match = m.group();
         components.addAll(toComponentArray(match.substring(match.indexOf(62) + 1, match.length() - 7), match.substring(6, match.indexOf(62))));
         remainingText = remainingText.substring(m.start() + match.length());
      }

      TabTextComponent root = new TabTextComponent("", components);
      root.modifier.setColor(TabTextColor.WHITE);
      root.modifier.setBold(false);
      root.modifier.setItalic(false);
      root.modifier.setUnderlined(false);
      root.modifier.setStrikethrough(false);
      root.modifier.setObfuscated(false);
      return root;
   }

   @NotNull
   private static List<TabComponent> toComponentArray(@NotNull String originalText, @Nullable String font) {
      String text = RGBUtils.getInstance().applyFormats(EnumChatFormat.color(originalText), TABGradientFormatter, TABRGBFormatter);
      List<TabComponent> components = new ArrayList<>();
      StringBuilder builder = new StringBuilder();
      TabTextComponent component = new TabTextComponent();
      component.modifier.setFont(font);

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == '<') {
            Matcher matcher = ATLAS_PATTERN.matcher(text.substring(i));
            if (matcher.find() && matcher.start() == 0) {
               if (builder.length() > 0) {
                  component.setText(builder.toString());
                  components.add(component);
                  component = new TabTextComponent(component);
                  component.setText("");
                  component.modifier.setFont(font);
                  builder = new StringBuilder();
               }

               String atlas = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
               String sprite = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
               components.add(atlasSprite(atlas, sprite));
               i += matcher.group(0).length() - 1;
               component = new TabTextComponent(component);
               component.setText("");
               component.modifier.setFont(font);
               continue;
            }

            matcher = HEAD_PATTERN.matcher(text.substring(i));
            if (matcher.find() && matcher.start() == 0) {
               String skinDefinition = matcher.group(1);
               if (builder.length() > 0) {
                  component.setText(builder.toString());
                  components.add(component);
                  component = new TabTextComponent(component);
                  component.setText("");
                  component.modifier.setFont(font);
                  builder = new StringBuilder();
               }

               components.add(head(skinDefinition));
               i += matcher.group(0).length() - 1;
               component = new TabTextComponent(component);
               component.setText("");
               component.modifier.setFont(font);
               continue;
            }
         }

         if (c == 167) {
            if (++i >= text.length()) {
               break;
            }

            c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
               c = (char)(c + ' ');
            }

            TabTextColor format = TabTextColor.getLegacyByChar(c);
            if (format != null) {
               if (builder.length() > 0) {
                  component.setText(builder.toString());
                  components.add(component);
                  component = new TabTextComponent(component);
                  component.setText("");
                  component.modifier.setFont(font);
                  builder = new StringBuilder();
               }

               if (format == TabTextColor.BOLD) {
                  component.modifier.setBold(true);
               } else if (format == TabTextColor.ITALIC) {
                  component.modifier.setItalic(true);
               } else if (format == TabTextColor.UNDERLINE) {
                  component.modifier.setUnderlined(true);
               } else if (format == TabTextColor.STRIKETHROUGH) {
                  component.modifier.setStrikethrough(true);
               } else if (format == TabTextColor.OBFUSCATED) {
                  component.modifier.setObfuscated(true);
               } else if (format == TabTextColor.RESET) {
                  component = new TabTextComponent();
                  component.modifier.setColor(TabTextColor.WHITE);
                  component.modifier.setFont(font);
               } else {
                  component = new TabTextComponent();
                  component.modifier.setColor(format);
                  component.modifier.setFont(font);
               }
            }
         } else if (c == '#' && text.length() > i + 6) {
            String hex = text.substring(i + 1, i + 7);
            if (isHexCode(hex)) {
               TabTextColor color = new TabTextColor(hex);
               i += 6;
               if (builder.length() > 0) {
                  component.setText(builder.toString());
                  components.add(component);
                  builder = new StringBuilder();
               }

               component = new TabTextComponent();
               component.modifier.setColor(color);
               component.modifier.setFont(font);
            } else {
               builder.append('#');
            }
         } else {
            builder.append(c);
         }
      }

      component.setText(builder.toString());
      components.add(component);
      return components;
   }

   private static boolean isHexCode(@NotNull String string) {
      for (int i = 0; i < string.length(); i++) {
         if ("0123456789AaBbCcDdEeFf".indexOf(string.charAt(i)) == -1) {
            return false;
         }
      }

      return true;
   }

   @NotNull
   public static TabComponent empty() {
      return EMPTY_LEGACY_TEXT;
   }

   @NotNull
   public static LegacyTextComponent legacyText(@NonNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (text.isEmpty()) {
         return EMPTY_LEGACY_TEXT;
      }

      LegacyTextComponent component = new LegacyTextComponent(text);
      component.modifier.setColor(TabTextColor.WHITE);
      component.modifier.setBold(false);
      component.modifier.setItalic(false);
      component.modifier.setUnderlined(false);
      component.modifier.setStrikethrough(false);
      component.modifier.setObfuscated(false);
      return component;
   }

   @NotNull
   public static TabTranslatableComponent translatable(@NonNull String key) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      } else {
         return new TabTranslatableComponent(key);
      }
   }

   @NotNull
   public static TabKeybindComponent keybind(@NonNull String keybind) {
      if (keybind == null) {
         throw new NullPointerException("keybind is marked non-null but is null");
      } else {
         return new TabKeybindComponent(keybind);
      }
   }

   @NotNull
   public static TabObjectComponent atlasSprite(@NonNull String atlas, @NonNull String sprite) {
      if (atlas == null) {
         throw new NullPointerException("atlas is marked non-null but is null");
      } else if (sprite == null) {
         throw new NullPointerException("sprite is marked non-null but is null");
      } else {
         return new TabObjectComponent(new TabAtlasSprite(atlas.toLowerCase(Locale.US).replace(" ", "_"), sprite.toLowerCase(Locale.US).replace(" ", "_")));
      }
   }

   @NotNull
   public static TabComponent head(@NonNull String skinDefinition) {
      if (skinDefinition == null) {
         throw new NullPointerException("skinDefinition is marked non-null but is null");
      }

      UUID id = null;
      String name = null;
      TabList.Skin skin = null;
      if (skinDefinition.startsWith("id:")) {
         String stringUUID = skinDefinition.substring(3);

         try {
            id = UUID.fromString(stringUUID);
         } catch (IllegalArgumentException e) {
            return new LegacyTextComponent(String.format("<Invalid UUID: \"%s\">", stringUUID));
         }
      } else if (skinDefinition.startsWith("name:")) {
         name = skinDefinition.substring(5);
         if (name.length() > 16) {
            return new LegacyTextComponent(String.format("<Invalid name (too long): \"%s\">", name));
         }
      } else {
         skin = TAB.getInstance().getConfiguration().getSkinManager().getSkin(skinDefinition);
         if (skin == null) {
            return new LegacyTextComponent(String.format("<Invalid skin: \"%s\">", skinDefinition));
         }
      }

      TabObjectComponent component = new TabObjectComponent(new TabPlayerSprite(id, name, skin, true));
      if (TAB.getInstance().getConfiguration().getConfig().getComponents().isDisableShadowForHeads()) {
         component.modifier.setShadowColor(0);
      }

      return component;
   }

   @NotNull
   public static TabComponent head(@NonNull TabPlayerSprite sprite) {
      if (sprite == null) {
         throw new NullPointerException("sprite is marked non-null but is null");
      } else {
         return new TabObjectComponent(sprite);
      }
   }

   @Generated
   public void setAdventureComponent(@Nullable Component adventureComponent) {
      this.adventureComponent = adventureComponent;
   }

   @NotNull
   @Generated
   public TabStyle getModifier() {
      return this.modifier;
   }

   @Generated
   public void setModifier(@NotNull TabStyle modifier) {
      if (modifier == null) {
         throw new NullPointerException("modifier is marked non-null but is null");
      }

      this.modifier = modifier;
   }
}
