package me.neznamy.tab.shared.placeholders.animation;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;

public class Animation {
   private final PlaceholderManagerImpl placeholderManager;
   private final String name;
   private final String[] messages;
   private final int interval;
   private final int refresh;

   public Animation(@NotNull PlaceholderManagerImpl placeholderManager, @NonNull String name, @NotNull AnimationConfiguration.AnimationDefinition configuration) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      }

      this.placeholderManager = placeholderManager;
      this.name = name;
      this.messages = configuration.getTexts().toArray(new String[0]);
      this.interval = configuration.getChangeInterval();
      int refresh = this.interval;
      List<String> nestedPlaceholders = new ArrayList<>();

      for (int i = 0; i < this.messages.length; i++) {
         this.messages[i] = EnumChatFormat.color(this.messages[i]);
         nestedPlaceholders.addAll(PlaceholderManagerImpl.detectPlaceholders(this.messages[i]));
      }

      for (String placeholder : nestedPlaceholders) {
         int localRefresh;
         if (placeholder.startsWith("%animation:")) {
            AnimationConfiguration cfg = TAB.getInstance().getConfiguration().getAnimations().getAnimations();
            localRefresh = cfg.getAnimations().containsKey(placeholder) ? cfg.getAnimations().get(placeholder).getChangeInterval() : this.interval;
         } else {
            localRefresh = placeholderManager.getPlaceholder(placeholder).getRefresh();
         }

         if (localRefresh != -1 && localRefresh < refresh) {
            refresh = localRefresh;
         }
      }

      this.refresh = refresh;
   }

   @NotNull
   public String getMessage() {
      return this.messages[(int)(this.placeholderManager.getLoopTime() % (this.messages.length * this.interval) / this.interval)];
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getRefresh() {
      return this.refresh;
   }
}
