package me.neznamy.tab.shared.chat;

import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabStyle {
   @Nullable
   private TabTextColor color;
   @Nullable
   private Integer shadowColor;
   @Nullable
   private Boolean bold;
   @Nullable
   private Boolean italic;
   @Nullable
   private Boolean underlined;
   @Nullable
   private Boolean strikethrough;
   @Nullable
   private Boolean obfuscated;
   @Nullable
   private String font;

   public TabStyle(@NotNull TabStyle modifier) {
      this.color = modifier.color;
      this.shadowColor = modifier.shadowColor;
      this.bold = modifier.bold;
      this.italic = modifier.italic;
      this.obfuscated = modifier.obfuscated;
      this.strikethrough = modifier.strikethrough;
      this.underlined = modifier.underlined;
      this.font = modifier.font;
   }

   @NotNull
   public String getMagicCodes() {
      StringBuilder builder = new StringBuilder();
      if (Boolean.TRUE.equals(this.bold)) {
         builder.append("§l");
      }

      if (Boolean.TRUE.equals(this.italic)) {
         builder.append("§o");
      }

      if (Boolean.TRUE.equals(this.obfuscated)) {
         builder.append("§k");
      }

      if (Boolean.TRUE.equals(this.strikethrough)) {
         builder.append("§m");
      }

      if (Boolean.TRUE.equals(this.underlined)) {
         builder.append("§n");
      }

      return builder.toString();
   }

   @NotNull
   public EnumChatFormat toEnumChatFormat() {
      if (Boolean.TRUE == this.bold) {
         return EnumChatFormat.BOLD;
      } else if (Boolean.TRUE == this.italic) {
         return EnumChatFormat.ITALIC;
      } else if (Boolean.TRUE == this.underlined) {
         return EnumChatFormat.UNDERLINE;
      } else if (Boolean.TRUE == this.strikethrough) {
         return EnumChatFormat.STRIKETHROUGH;
      } else if (Boolean.TRUE == this.obfuscated) {
         return EnumChatFormat.OBFUSCATED;
      } else {
         return this.color != null ? this.color.getLegacyColor() : EnumChatFormat.RESET;
      }
   }

   @Nullable
   @Generated
   public TabTextColor getColor() {
      return this.color;
   }

   @Nullable
   @Generated
   public Integer getShadowColor() {
      return this.shadowColor;
   }

   @Nullable
   @Generated
   public Boolean getBold() {
      return this.bold;
   }

   @Nullable
   @Generated
   public Boolean getItalic() {
      return this.italic;
   }

   @Nullable
   @Generated
   public Boolean getUnderlined() {
      return this.underlined;
   }

   @Nullable
   @Generated
   public Boolean getStrikethrough() {
      return this.strikethrough;
   }

   @Nullable
   @Generated
   public Boolean getObfuscated() {
      return this.obfuscated;
   }

   @Nullable
   @Generated
   public String getFont() {
      return this.font;
   }

   @Generated
   public void setColor(@Nullable TabTextColor color) {
      this.color = color;
   }

   @Generated
   public void setShadowColor(@Nullable Integer shadowColor) {
      this.shadowColor = shadowColor;
   }

   @Generated
   public void setBold(@Nullable Boolean bold) {
      this.bold = bold;
   }

   @Generated
   public void setItalic(@Nullable Boolean italic) {
      this.italic = italic;
   }

   @Generated
   public void setUnderlined(@Nullable Boolean underlined) {
      this.underlined = underlined;
   }

   @Generated
   public void setStrikethrough(@Nullable Boolean strikethrough) {
      this.strikethrough = strikethrough;
   }

   @Generated
   public void setObfuscated(@Nullable Boolean obfuscated) {
      this.obfuscated = obfuscated;
   }

   @Generated
   public void setFont(@Nullable String font) {
      this.font = font;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof TabStyle)) {
         return false;
      }

      TabStyle other = (TabStyle)o;
      if (!other.canEqual(this)) {
         return false;
      }

      Object this$shadowColor = this.getShadowColor();
      Object other$shadowColor = other.getShadowColor();
      if (this$shadowColor == null ? other$shadowColor == null : this$shadowColor.equals(other$shadowColor)) {
         Object this$bold = this.getBold();
         Object other$bold = other.getBold();
         if (this$bold == null ? other$bold == null : this$bold.equals(other$bold)) {
            Object this$italic = this.getItalic();
            Object other$italic = other.getItalic();
            if (this$italic == null ? other$italic == null : this$italic.equals(other$italic)) {
               Object this$underlined = this.getUnderlined();
               Object other$underlined = other.getUnderlined();
               if (this$underlined == null ? other$underlined == null : this$underlined.equals(other$underlined)) {
                  Object this$strikethrough = this.getStrikethrough();
                  Object other$strikethrough = other.getStrikethrough();
                  if (this$strikethrough == null ? other$strikethrough == null : this$strikethrough.equals(other$strikethrough)) {
                     Object this$obfuscated = this.getObfuscated();
                     Object other$obfuscated = other.getObfuscated();
                     if (this$obfuscated == null ? other$obfuscated == null : this$obfuscated.equals(other$obfuscated)) {
                        Object this$color = this.getColor();
                        Object other$color = other.getColor();
                        if (this$color == null ? other$color == null : this$color.equals(other$color)) {
                           Object this$font = this.getFont();
                           Object other$font = other.getFont();
                           return this$font == null ? other$font == null : this$font.equals(other$font);
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof TabStyle;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $shadowColor = this.getShadowColor();
      result = result * 59 + ($shadowColor == null ? 43 : $shadowColor.hashCode());
      Object $bold = this.getBold();
      result = result * 59 + ($bold == null ? 43 : $bold.hashCode());
      Object $italic = this.getItalic();
      result = result * 59 + ($italic == null ? 43 : $italic.hashCode());
      Object $underlined = this.getUnderlined();
      result = result * 59 + ($underlined == null ? 43 : $underlined.hashCode());
      Object $strikethrough = this.getStrikethrough();
      result = result * 59 + ($strikethrough == null ? 43 : $strikethrough.hashCode());
      Object $obfuscated = this.getObfuscated();
      result = result * 59 + ($obfuscated == null ? 43 : $obfuscated.hashCode());
      Object $color = this.getColor();
      result = result * 59 + ($color == null ? 43 : $color.hashCode());
      Object $font = this.getFont();
      return result * 59 + ($font == null ? 43 : $font.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "TabStyle(color="
         + this.getColor()
         + ", shadowColor="
         + this.getShadowColor()
         + ", bold="
         + this.getBold()
         + ", italic="
         + this.getItalic()
         + ", underlined="
         + this.getUnderlined()
         + ", strikethrough="
         + this.getStrikethrough()
         + ", obfuscated="
         + this.getObfuscated()
         + ", font="
         + this.getFont()
         + ")";
   }

   @Generated
   public TabStyle() {
   }

   @Generated
   public TabStyle(
      @Nullable TabTextColor color,
      @Nullable Integer shadowColor,
      @Nullable Boolean bold,
      @Nullable Boolean italic,
      @Nullable Boolean underlined,
      @Nullable Boolean strikethrough,
      @Nullable Boolean obfuscated,
      @Nullable String font
   ) {
      this.color = color;
      this.shadowColor = shadowColor;
      this.bold = bold;
      this.italic = italic;
      this.underlined = underlined;
      this.strikethrough = strikethrough;
      this.obfuscated = obfuscated;
      this.font = font;
   }
}
