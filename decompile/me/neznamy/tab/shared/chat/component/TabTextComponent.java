package me.neznamy.tab.shared.chat.component;

import java.util.List;
import lombok.Generated;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabTextComponent extends TabComponent {
   @NotNull
   protected String text;

   public TabTextComponent(@NotNull String text, List<TabComponent> extra) {
      this.text = text;
      super.extra = extra;
   }

   public TabTextComponent(@NotNull TabTextComponent component) {
      this.text = component.text;
      this.modifier = new TabStyle(component.modifier);
   }

   public TabTextComponent(@NotNull String text, @Nullable TabTextColor color) {
      this.text = text;
      this.modifier.setColor(color);
   }

   @NotNull
   @Override
   public String toLegacyText() {
      StringBuilder builder = new StringBuilder();
      this.append(builder, "");
      return builder.toString();
   }

   @NotNull
   private String append(@NotNull StringBuilder builder, @NotNull String previousFormatting) {
      String formatting = this.getFormatting();
      if (!formatting.equals(previousFormatting)) {
         builder.append(formatting);
      }

      builder.append(this.text);

      for (TabComponent component : this.getExtra()) {
         if (component instanceof TabTextComponent) {
            formatting = ((TabTextComponent)component).append(builder, formatting);
         }
      }

      return formatting;
   }

   @NotNull
   private String getFormatting() {
      StringBuilder builder = new StringBuilder();
      if (this.modifier.getColor() != null) {
         builder.append("§");
         if (this.modifier.getColor().getLegacyColor() == EnumChatFormat.WHITE) {
            builder.append("r");
         } else {
            builder.append(this.modifier.getColor().getLegacyColor().getCharacter());
         }
      }

      builder.append(this.modifier.getMagicCodes());
      return builder.toString();
   }

   @NotNull
   @Generated
   public String getText() {
      return this.text;
   }

   @Generated
   public void setText(@NotNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      this.text = text;
   }

   @Generated
   public TabTextComponent(@NotNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      this.text = text;
   }

   @Generated
   public TabTextComponent() {
   }
}
