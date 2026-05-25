package me.neznamy.tab.shared.features.scoreboard.lines;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.ScoreRefresher;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ScoreboardLine extends RefreshableFeature implements Line, CustomThreaded {
   protected final int lineNumber;
   protected String text;
   protected String numberFormat;
   protected final ScoreboardImpl parent;
   protected final String teamName;
   protected final String forcedPlayerNameStart;
   private final ScoreRefresher scoreRefresher;
   private final Set<TabPlayer> shownPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

   protected ScoreboardLine(@NonNull ScoreboardImpl parent, int lineNumber, String text) {
      if (parent == null) {
         throw new NullPointerException("parent is marked non-null but is null");
      }

      this.initializeText(text);
      this.parent = parent;
      this.lineNumber = lineNumber;
      this.teamName = "TAB-Sidebar-" + lineNumber;
      if (lineNumber > 99) {
         throw new IllegalStateException("Internal code does not support more than 99 lines per scoreboard.");
      }

      this.forcedPlayerNameStart = String.format("§%d§%d§r", lineNumber / 10 % 10, lineNumber % 10);
      this.scoreRefresher = new ScoreRefresher(this, this.numberFormat);
      TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardScore(parent.getName(), lineNumber), this.scoreRefresher);
   }

   public abstract void register(@NonNull TabPlayer var1);

   public abstract void unregister(@NonNull TabPlayer var1);

   @NotNull
   public String getPlayerName(@NonNull TabPlayer viewer) {
      if (viewer == null) {
         throw new NullPointerException("viewer is marked non-null but is null");
      } else {
         return this.forcedPlayerNameStart;
      }
   }

   protected String[] split(@NonNull String string, int firstElementMaxLength) {
      if (string == null) {
         throw new NullPointerException("string is marked non-null but is null");
      }

      if (string.length() <= firstElementMaxLength) {
         return new String[]{string, ""};
      }

      int splitIndex = firstElementMaxLength;
      if (string.charAt(splitIndex - 1) == 167) {
         splitIndex--;
      }

      return new String[]{string.substring(0, splitIndex), string.substring(splitIndex)};
   }

   protected void addLine(@NonNull TabPlayer p, @NonNull String fakePlayer, @NonNull String prefix, @NonNull String suffix) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (fakePlayer == null) {
         throw new NullPointerException("fakePlayer is marked non-null but is null");
      }

      if (prefix == null) {
         throw new NullPointerException("prefix is marked non-null but is null");
      }

      if (suffix == null) {
         throw new NullPointerException("suffix is marked non-null but is null");
      }

      p.getScoreboard().setScore("TAB-Scoreboard", fakePlayer, this.getNumber(p), null, this.scoreRefresher.getNumberFormat(p));
      p.getScoreboard()
         .registerTeam(
            this.teamName,
            this.parent.getManager().getCache().get(prefix),
            this.parent.getManager().getCache().get(suffix),
            Scoreboard.NameVisibility.NEVER,
            Scoreboard.CollisionRule.NEVER,
            Collections.singletonList(fakePlayer),
            0,
            TabTextColor.RESET.getLegacyColor()
         );
      this.shownPlayers.add(p);
   }

   protected void removeLine(@NonNull TabPlayer p, @NonNull String fakePlayer) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      }

      if (fakePlayer == null) {
         throw new NullPointerException("fakePlayer is marked non-null but is null");
      }

      p.getScoreboard().removeScore("TAB-Scoreboard", fakePlayer);
      p.getScoreboard().unregisterTeam(this.teamName);
      this.shownPlayers.remove(p);
   }

   public int getNumber(@NonNull TabPlayer p) {
      if (p == null) {
         throw new NullPointerException("p is marked non-null but is null");
      } else {
         return !this.parent.getManager().getConfiguration().isUseNumbers() && p.getVersion().getMinorVersion() >= 8
            ? this.parent.getManager().getConfiguration().getStaticNumber()
            : this.parent.getLines().size() + 1 - this.lineNumber;
      }
   }

   protected String[] splitText(@NonNull String playerNameStart, @NonNull String text, int maxNameLength) {
      if (playerNameStart == null) {
         throw new NullPointerException("playerNameStart is marked non-null but is null");
      }

      if (text == null) {
         throw new NullPointerException("text is marked non-null but is null");
      }

      String prefixValue;
      String nameValue;
      String suffixValue;
      if (text.length() <= maxNameLength - playerNameStart.length()) {
         prefixValue = "";
         nameValue = playerNameStart + text;
         suffixValue = "";
      } else {
         String[] prefixOther = this.split(text, 16);
         prefixValue = prefixOther[0];
         String other = prefixOther[1];
         other = playerNameStart + this.getLastColors(prefixValue) + other;
         String[] nameSuffix = this.split(other, maxNameLength);
         nameValue = nameSuffix[0];
         suffixValue = nameSuffix[1];
      }

      return new String[]{prefixValue, nameValue, suffixValue};
   }

   public boolean isShownTo(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      } else {
         return this.shownPlayers.contains(player);
      }
   }

   protected void initializeText(@NotNull String text) {
      String[] split = text.split("\\|\\|");
      this.text = split[0];
      this.numberFormat = split.length >= 2 ? split[1] : "";
   }

   protected void updateTeam(@NotNull TabPlayer player, @NotNull String prefix, @NotNull String suffix) {
      player.getScoreboard()
         .updateTeam(
            this.teamName,
            this.parent.getManager().getCache().get(prefix),
            this.parent.getManager().getCache().get(suffix),
            TabTextColor.RESET.getLegacyColor()
         );
   }

   public void removePlayerSilently(@NonNull TabPlayer player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.shownPlayers.remove(player);
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.parent.getCustomThread();
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.parent.getFeatureName();
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating Scoreboard lines";
   }

   @NotNull
   protected String getLastColors(@NotNull String input) {
      StringBuilder result = new StringBuilder();
      int length = input.length();

      for (int index = length - 1; index > -1; index--) {
         char section = input.charAt(index);
         if ((section == 167 || section == '&') && index < length - 1) {
            char c = input.charAt(index + 1);
            if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(c))) {
               result.insert(0, '§');
               result.insert(1, c);
               if ("0123456789AaBbCcDdEeFfRr".contains(String.valueOf(c))) {
                  break;
               }
            }
         }
      }

      return result.toString();
   }

   @Generated
   public int getLineNumber() {
      return this.lineNumber;
   }

   @Generated
   @Override
   public String getText() {
      return this.text;
   }

   @Generated
   public String getNumberFormat() {
      return this.numberFormat;
   }

   @Generated
   public ScoreboardImpl getParent() {
      return this.parent;
   }

   @Generated
   public String getTeamName() {
      return this.teamName;
   }

   @Generated
   public String getForcedPlayerNameStart() {
      return this.forcedPlayerNameStart;
   }

   @Generated
   public ScoreRefresher getScoreRefresher() {
      return this.scoreRefresher;
   }

   @Generated
   public Set<TabPlayer> getShownPlayers() {
      return this.shownPlayers;
   }
}
