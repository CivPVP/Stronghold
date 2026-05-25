package me.neznamy.tab.shared.platform.decorators;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.BossBar;
import org.jetbrains.annotations.NotNull;

public abstract class SafeBossBar<T> implements BossBar {
   private final Map<UUID, SafeBossBar<T>.BossBarInfo> bossBars = new ConcurrentHashMap<>();
   private boolean frozen;

   @Override
   public synchronized void create(@NotNull UUID id, @NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
      SafeBossBar<T>.BossBarInfo bar = new SafeBossBar.BossBarInfo(title, progress, color, style, this.constructBossBar(title, progress, color, style));
      this.bossBars.put(id, bar);
      if (!this.frozen) {
         this.create(bar);
      }
   }

   @Override
   public synchronized void update(@NotNull UUID id, @NotNull TabComponent title) {
      SafeBossBar<T>.BossBarInfo bar = this.bossBars.get(id);
      if (bar != null) {
         bar.setTitle(title);
         if (!this.frozen) {
            this.updateTitle(bar);
         }
      }
   }

   @Override
   public synchronized void update(@NotNull UUID id, float progress) {
      SafeBossBar<T>.BossBarInfo bar = this.bossBars.get(id);
      if (bar != null) {
         bar.setProgress(progress);
         if (!this.frozen) {
            this.updateProgress(bar);
         }
      }
   }

   @Override
   public synchronized void update(@NotNull UUID id, @NotNull BarStyle style) {
      SafeBossBar<T>.BossBarInfo bar = this.bossBars.get(id);
      if (bar != null) {
         bar.setStyle(style);
         if (!this.frozen) {
            this.updateStyle(bar);
         }
      }
   }

   @Override
   public synchronized void update(@NotNull UUID id, @NotNull BarColor color) {
      SafeBossBar<T>.BossBarInfo bar = this.bossBars.get(id);
      if (bar != null) {
         bar.setColor(color);
         if (!this.frozen) {
            this.updateColor(bar);
         }
      }
   }

   @Override
   public synchronized void remove(@NotNull UUID id) {
      SafeBossBar<T>.BossBarInfo bar = this.bossBars.remove(id);
      if (bar != null) {
         if (!this.frozen) {
            this.remove(bar);
         }
      }
   }

   @Override
   public synchronized void clear() {
      for (UUID id : this.bossBars.keySet()) {
         this.remove(id);
      }
   }

   public synchronized void freeze() {
      this.frozen = true;
   }

   public synchronized void unfreezeAndResend() {
      this.frozen = false;

      for (SafeBossBar<T>.BossBarInfo bar : this.bossBars.values()) {
         bar.setBossBar(this.constructBossBar(bar.getTitle(), bar.getProgress(), bar.getColor(), bar.getStyle()));
         this.create(bar);
      }
   }

   @NotNull
   public abstract T constructBossBar(@NotNull TabComponent var1, float var2, @NotNull BarColor var3, @NotNull BarStyle var4);

   public abstract void create(@NotNull SafeBossBar.BossBarInfo var1);

   public abstract void updateTitle(@NotNull SafeBossBar.BossBarInfo var1);

   public abstract void updateProgress(@NotNull SafeBossBar.BossBarInfo var1);

   public abstract void updateStyle(@NotNull SafeBossBar.BossBarInfo var1);

   public abstract void updateColor(@NotNull SafeBossBar.BossBarInfo var1);

   public abstract void remove(@NotNull SafeBossBar.BossBarInfo var1);

   public class BossBarInfo {
      @NonNull
      private TabComponent title;
      private float progress;
      @NonNull
      private BarColor color;
      @NonNull
      private BarStyle style;
      @NonNull
      private T bossBar;

      @Generated
      public BossBarInfo(
         @NonNull final TabComponent title, final float progress, @NonNull final BarColor color, @NonNull final BarStyle style, @NonNull final T bossBar
      ) {
         if (title == null) {
            throw new NullPointerException("title is marked non-null but is null");
         }

         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         if (style == null) {
            throw new NullPointerException("style is marked non-null but is null");
         }

         if (bossBar == null) {
            throw new NullPointerException("bossBar is marked non-null but is null");
         }

         this.title = title;
         this.progress = progress;
         this.color = color;
         this.style = style;
         this.bossBar = bossBar;
      }

      @NonNull
      @Generated
      public TabComponent getTitle() {
         return this.title;
      }

      @Generated
      public float getProgress() {
         return this.progress;
      }

      @NonNull
      @Generated
      public BarColor getColor() {
         return this.color;
      }

      @NonNull
      @Generated
      public BarStyle getStyle() {
         return this.style;
      }

      @NonNull
      @Generated
      public T getBossBar() {
         return this.bossBar;
      }

      @Generated
      public void setTitle(@NonNull TabComponent title) {
         if (title == null) {
            throw new NullPointerException("title is marked non-null but is null");
         }

         this.title = title;
      }

      @Generated
      public void setProgress(float progress) {
         this.progress = progress;
      }

      @Generated
      public void setColor(@NonNull BarColor color) {
         if (color == null) {
            throw new NullPointerException("color is marked non-null but is null");
         }

         this.color = color;
      }

      @Generated
      public void setStyle(@NonNull BarStyle style) {
         if (style == null) {
            throw new NullPointerException("style is marked non-null but is null");
         }

         this.style = style;
      }

      @Generated
      public void setBossBar(@NonNull T bossBar) {
         if (bossBar == null) {
            throw new NullPointerException("bossBar is marked non-null but is null");
         }

         this.bossBar = bossBar;
      }
   }
}
