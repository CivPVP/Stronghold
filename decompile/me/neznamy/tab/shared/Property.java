package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Property {
   @Nullable
   private final String name;
   @Nullable
   private final RefreshableFeature listener;
   @NotNull
   private final TabPlayer owner;
   @NotNull
   private String originalRawValue;
   @Nullable
   private String temporaryValue;
   private String rawFormattedValue;
   private String lastReplacedValue;
   private boolean mayContainRelPlaceholders;
   @Nullable
   private String source;
   private String[] placeholders;
   private String[] relPlaceholders;

   public Property(@Nullable RefreshableFeature listener, @NotNull TabPlayer owner, @NotNull String rawValue) {
      this(null, listener, owner, rawValue, null);
   }

   public Property(@Nullable String name, @Nullable RefreshableFeature listener, @NotNull TabPlayer owner, @NotNull String rawValue, @Nullable String source) {
      this.name = name;
      this.listener = listener;
      this.owner = owner;
      this.source = source;
      this.originalRawValue = rawValue;
      this.analyze(this.originalRawValue);
   }

   private void analyze(@NotNull String value) {
      List<String> placeholders0 = new ArrayList<>();
      List<String> relPlaceholders0 = new ArrayList<>();

      for (String identifier : PlaceholderManagerImpl.detectPlaceholders(value)) {
         placeholders0.add(identifier);
         if (identifier.startsWith("%rel_")) {
            relPlaceholders0.add(identifier);
         }
      }

      String rawFormattedValue0 = value;

      for (String placeholder : placeholders0) {
         rawFormattedValue0 = this.replaceFirst(rawFormattedValue0, placeholder);
      }

      if (!placeholders0.isEmpty() && rawFormattedValue0.contains("%")) {
         int index = rawFormattedValue0.lastIndexOf(37);
         if (rawFormattedValue0.length() == index + 1 || rawFormattedValue0.charAt(index + 1) != 's') {
            StringBuilder sb = new StringBuilder(rawFormattedValue0);
            sb.insert(index + 1, "%");
            rawFormattedValue0 = sb.toString();
         }
      }

      this.rawFormattedValue = EnumChatFormat.color(rawFormattedValue0);
      this.placeholders = placeholders0.toArray(new String[0]);
      this.relPlaceholders = relPlaceholders0.toArray(new String[0]);
      if (this.listener != null) {
         this.listener.addUsedPlaceholders(placeholders0);
      }

      this.lastReplacedValue = this.rawFormattedValue;
      this.update();
      if (this.name != null) {
         TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
         expansion.setPropertyValue(this.owner, this.name, this.lastReplacedValue);
         expansion.setRawPropertyValue(this.owner, this.name, this.getCurrentRawValue());
      }
   }

   private String replaceFirst(String original, String searchString) {
      int index = original.indexOf(searchString);
      return index != -1 ? original.substring(0, index) + "%s" + original.substring(index + searchString.length()) : original;
   }

   public boolean changeRawValue(@NotNull String newValue) {
      return this.changeRawValue(newValue, null);
   }

   public boolean changeRawValue(@NotNull String newValue, @Nullable String newSource) {
      if (this.originalRawValue.equals(newValue)) {
         return false;
      }

      this.originalRawValue = newValue;
      this.source = newSource;
      if (this.temporaryValue == null) {
         this.analyze(this.originalRawValue);
      }

      return true;
   }

   @Nullable
   public String getSource() {
      return this.temporaryValue == null ? this.source : "API";
   }

   public void setTemporaryValue(@Nullable String temporaryValue) {
      if (temporaryValue != null) {
         this.temporaryValue = temporaryValue;
         this.analyze(this.temporaryValue);
      } else {
         this.temporaryValue = null;
         this.analyze(this.originalRawValue);
      }
   }

   @NotNull
   public String getCurrentRawValue() {
      return this.temporaryValue != null ? this.temporaryValue : this.originalRawValue;
   }

   @NotNull
   public String updateAndGet() {
      this.update();
      return this.get();
   }

   public boolean update() {
      if (this.placeholders.length == 0) {
         return false;
      }

      String string;
      if ("%s".equals(this.rawFormattedValue)) {
         string = TAB.getInstance().getPlaceholderManager().getPlaceholder(this.placeholders[0]).set(this.placeholders[0], this.owner);
      } else {
         Object[] values = new String[this.placeholders.length];

         for (int i = 0; i < this.placeholders.length; i++) {
            values[i] = TAB.getInstance().getPlaceholderManager().getPlaceholder(this.placeholders[i]).set(this.placeholders[i], this.owner);
         }

         string = String.format(this.rawFormattedValue, values);
      }

      string = EnumChatFormat.color(string);
      if (!this.lastReplacedValue.equals(string)) {
         this.lastReplacedValue = string;
         this.mayContainRelPlaceholders = this.lastReplacedValue.indexOf(37) != -1;
         if (this.name != null) {
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setPropertyValue(this.owner, this.name, this.lastReplacedValue);
         }

         return true;
      } else {
         return false;
      }
   }

   @NotNull
   public String get() {
      return this.lastReplacedValue;
   }

   @NotNull
   public String getFormat(@NotNull TabPlayer viewer) {
      if (!this.mayContainRelPlaceholders) {
         return this.lastReplacedValue;
      }

      String format = this.lastReplacedValue;

      for (String identifier : this.relPlaceholders) {
         RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl)TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
         format = format.replace(pl.getIdentifier(), EnumChatFormat.color(pl.getLastValue(viewer, this.owner)));
      }

      for (String identifier : PlaceholderManagerImpl.detectPlaceholders(format)) {
         if (identifier.startsWith("%rel_")) {
            RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl)TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
            format = format.replace(pl.getIdentifier(), EnumChatFormat.color(pl.getLastValue(viewer, this.owner)));
            if (this.listener != null) {
               this.listener.addUsedPlaceholder(identifier);
            }
         }
      }

      return format;
   }

   @NotNull
   public String getOriginalReplacedValue() {
      String value = this.originalRawValue;

      for (String identifier : PlaceholderManagerImpl.detectPlaceholders(value)) {
         value = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).set(identifier, this.owner);
      }

      return EnumChatFormat.color(value);
   }

   @Nullable
   @Generated
   public String getName() {
      return this.name;
   }

   @NotNull
   @Generated
   public String getOriginalRawValue() {
      return this.originalRawValue;
   }

   @Nullable
   @Generated
   public String getTemporaryValue() {
      return this.temporaryValue;
   }
}
