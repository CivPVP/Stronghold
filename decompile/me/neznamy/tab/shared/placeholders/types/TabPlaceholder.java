package me.neznamy.tab.shared.placeholders.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TabPlaceholder implements Placeholder {
   protected final String ERROR_VALUE = "<ERROR>";
   private final int refresh;
   @NonNull
   protected final String identifier;
   @NonNull
   protected final PlaceholderReplacementPattern replacements;
   protected final List<String> parents = new ArrayList<>();

   protected TabPlaceholder(@NonNull String identifier, int refresh) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (refresh % 50 != 0 && refresh != -1) {
         throw new IllegalArgumentException("Refresh interval must be divisible by 50");
      }

      if (identifier.startsWith("%") && identifier.endsWith("%")) {
         this.identifier = identifier;
         this.refresh = refresh;
         Map<Object, Object> map = TAB.getInstance().getConfiguration().getConfig().getReplacements().getValues().get(identifier);
         this.replacements = map == null ? PlaceholderReplacementPattern.EMPTY : PlaceholderReplacementPattern.create(identifier, map);

         for (String nested : this.replacements.getNestedPlaceholders()) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(identifier);
         }
      } else {
         throw new IllegalArgumentException("Identifier must start and end with % (attempted to use \"" + identifier + "\")");
      }
   }

   @NotNull
   public String set(@NonNull String string, @Nullable TabPlayer player) {
      if (string == null) {
         throw new NullPointerException("string is marked non-null but is null");
      } else {
         return this.replace(string, this.identifier, this.setPlaceholders(this.getLastValue(player), player));
      }
   }

   @NotNull
   private String replace(@NonNull String string, @NonNull String original, @NonNull String replacement) {
      if (string == null) {
         throw new NullPointerException("string is marked non-null but is null");
      } else if (original == null) {
         throw new NullPointerException("original is marked non-null but is null");
      } else if (replacement == null) {
         throw new NullPointerException("replacement is marked non-null but is null");
      } else if (!string.contains(original)) {
         return string;
      } else {
         return string.equals(original) ? replacement : string.replace(original, replacement);
      }
   }

   @NotNull
   protected String setPlaceholders(@NonNull String text, @Nullable TabPlayer p) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (this.identifier.equals(text)) {
         return text;
      }

      String replaced = text;

      for (String s : PlaceholderManagerImpl.detectPlaceholders(text)) {
         if (!s.equals(this.identifier) && (!this.identifier.startsWith("%sync:") || !("%" + this.identifier.substring(6)).equals(s)) && !s.startsWith("%rel_")
            )
          {
            TabPlaceholder nested = TAB.getInstance().getPlaceholderManager().getPlaceholder(s);
            nested.addParent(this.identifier);
            replaced = nested.set(replaced, p);
         }
      }

      return replaced;
   }

   public void addParent(@NonNull String parent) {
      if (parent == null) {
         throw new NullPointerException("parent is marked non-null but is null");
      }

      if (!this.parents.contains(parent)) {
         this.parents.add(parent);
      }
   }

   public void updateParents(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (!this.parents.isEmpty()) {
         for (String id : new ArrayList<>(this.parents)) {
            TabPlaceholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(id);
            pl.updateFromNested(player);
            pl.updateParents(player);
         }
      }
   }

   public abstract void updateFromNested(@NonNull TabPlayer var1);

   @NotNull
   public abstract String getLastValue(@Nullable TabPlayer var1);

   @NotNull
   public abstract String getLastValueSafe(@NotNull TabPlayer var1);

   @Generated
   public String getERROR_VALUE() {
      return "<ERROR>";
   }

   @Generated
   @Override
   public int getRefresh() {
      return this.refresh;
   }

   @NonNull
   @Generated
   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @NonNull
   @Generated
   public PlaceholderReplacementPattern getReplacements() {
      return this.replacements;
   }

   @Generated
   public List<String> getParents() {
      return this.parents;
   }
}
