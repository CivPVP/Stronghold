package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class StableDynamicLine extends ScoreboardLine {
   private final String[] EMPTY_ARRAY = new String[0];

   public StableDynamicLine(@NonNull ScoreboardImpl parent, int lineNumber, @NonNull String text) {
      super(parent, lineNumber, text);
      if (parent == null) {
         throw new NullPointerException("parent is marked non-null but is null");
      }

      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }
   }

   @Override
   public void refresh(@NotNull TabPlayer refreshed, boolean force) {
      if (refreshed.scoreboardData.activeScoreboard == this.parent) {
         String[] prefixSuffix = this.replaceText(refreshed, force, false);
         if (prefixSuffix.length != 0) {
            this.updateTeam(refreshed, prefixSuffix[0], prefixSuffix[1]);
         }
      }
   }

   @Override
   public void register(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      p.scoreboardData.lineProperties.put(this, new Property(this, p, this.text));
      this.getScoreRefresher().registerProperties(p);
      String[] prefixSuffix = this.replaceText(p, true, true);
      if (prefixSuffix.length != 0) {
         this.addLine(p, this.forcedPlayerNameStart, prefixSuffix[0], prefixSuffix[1]);
      }
   }

   @Override
   public void unregister(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.scoreboardData.activeScoreboard == this.parent && !p.scoreboardData.lineProperties.get(this).get().isEmpty()) {
         this.removeLine(p, this.forcedPlayerNameStart);
      }
   }

   private String[] replaceText(TabPlayer p, boolean force, boolean suppressToggle) {
      Property scoreProperty = p.scoreboardData.lineProperties.get(this);
      if (scoreProperty == null) {
         return this.EMPTY_ARRAY;
      }

      boolean emptyBefore = scoreProperty.get().isEmpty();
      if (!scoreProperty.update() && !force) {
         return this.EMPTY_ARRAY;
      }

      String replaced = scoreProperty.get();
      if (!p.getVersion().supportsRGB()) {
         replaced = this.parent.getManager().getCache().get(replaced).toLegacyText();
      }

      String[] split = this.split(p, replaced);
      if (!replaced.isEmpty()) {
         if (emptyBefore) {
            this.addLine(p, this.forcedPlayerNameStart, split[0], split[1]);
            this.parent.recalculateScores(p);
            return this.EMPTY_ARRAY;
         } else {
            return split;
         }
      } else {
         if (!suppressToggle) {
            this.removeLine(p, this.forcedPlayerNameStart);
            this.parent.recalculateScores(p);
         }

         return this.EMPTY_ARRAY;
      }
   }

   private String[] split(@NonNull TabPlayer p, @NonNull String text) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      if (p.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
         return new String[]{text, ""};
      }

      int charLimit = 16;
      if (text.length() > charLimit) {
         StringBuilder prefix = new StringBuilder(text);
         StringBuilder suffix = new StringBuilder(text);
         prefix.setLength(charLimit);
         suffix.delete(0, charLimit);
         if (prefix.charAt(charLimit - 1) == 167) {
            prefix.setLength(prefix.length() - 1);
            suffix.insert(0, '§');
         }

         String prefixString = prefix.toString();
         suffix.insert(0, this.getLastColors(this.parent.getManager().getCache().get(prefixString).toLegacyText()));
         return new String[]{prefixString, suffix.toString()};
      } else {
         return new String[]{text, ""};
      }
   }

   @Override
   public void setText(@NonNull String text) {
      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      this.ensureActive();
      this.initializeText(text);

      for (TabPlayer p : this.parent.getPlayers()) {
         p.scoreboardData.lineProperties.get(this).changeRawValue(text);
         String[] prefixSuffix = this.replaceText(p, true, true);
         if (prefixSuffix.length == 0) {
            if (!text.isEmpty()) {
               continue;
            }

            prefixSuffix = new String[]{"", ""};
         }

         this.updateTeam(p, prefixSuffix[0], prefixSuffix[1]);
      }
   }
}
