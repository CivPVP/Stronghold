package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class LongLine extends ScoreboardLine {
   public LongLine(@NonNull ScoreboardImpl parent, int lineNumber, @NonNull String text) {
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
         Property lineProperty = refreshed.scoreboardData.lineProperties.get(this);
         if (lineProperty.update()) {
            if (refreshed.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
               this.updateTeam(refreshed, lineProperty.get(), "");
            } else {
               this.removeLine(refreshed, refreshed.scoreboardData.lineNameProperties.get(this).get());
               String[] values = this.splitText(
                  this.forcedPlayerNameStart,
                  this.parent.getManager().getCache().get(lineProperty.get()).toLegacyText(),
                  refreshed.getVersion().getMinorVersion() >= 8 ? 40 : 16
               );
               this.addLine(refreshed, values[1], values[0], values[2]);
               refreshed.scoreboardData.lineNameProperties.get(this).changeRawValue(values[1]);
            }
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
      String value = p.scoreboardData.lineProperties.get(this).get();
      if (p.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
         this.addLine(p, this.forcedPlayerNameStart, value, "");
         p.scoreboardData.lineNameProperties.put(this, new Property(this, p, this.forcedPlayerNameStart));
      } else {
         String[] values = this.splitText(
            this.forcedPlayerNameStart, this.parent.getManager().getCache().get(value).toLegacyText(), p.getVersion().getMinorVersion() >= 8 ? 40 : 16
         );
         this.addLine(p, values[1], values[0], values[2]);
         p.scoreboardData.lineNameProperties.put(this, new Property(this, p, values[1]));
      }
   }

   @Override
   public void unregister(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (p.scoreboardData.activeScoreboard == this.parent) {
         this.removeLine(p, p.scoreboardData.lineNameProperties.get(this).get());
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
         this.refresh(p, false);
      }
   }

   @NotNull
   @Override
   public String getPlayerName(@NonNull TabPlayer viewer) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      } else {
         return viewer.scoreboardData.lineNameProperties.get(this).get();
      }
   }
}
