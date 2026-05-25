package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class VisibilityRefresher extends RefreshableFeature implements JoinListener, Loadable, CustomThreaded {
   @NotNull
   private final NameTag nameTags;
   @NotNull
   private final Condition invisibleCondition;

   public VisibilityRefresher(@NotNull NameTag nameTags) {
      this.nameTags = nameTags;
      this.invisibleCondition = TAB.getInstance()
         .getPlaceholderManager()
         .getConditionManager()
         .getByNameOrExpression(nameTags.getConfiguration().getInvisibleNameTags());
   }

   @Override
   public void load() {
      TAB.getInstance().getPlaceholderManager().registerInternalPlayerPlaceholder("%invisible%", 500, p -> {
         TabPlayer player = (TabPlayer)p;
         boolean newInvisibility = this.invisibleCondition.isMet((TabPlayer)p);
         if (newInvisibility) {
            player.teamData.hideNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
         } else {
            player.teamData.showNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
         }

         if (player.hasInvisibilityPotion()) {
            newInvisibility = true;
         }

         return Boolean.toString(newInvisibility);
      });
      this.addUsedPlaceholder("%invisible%");

      for (TabPlayer all : this.nameTags.getOnlinePlayers().getPlayers()) {
         this.onJoin(all);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      if (this.invisibleCondition.isMet(connectedPlayer)) {
         connectedPlayer.teamData.hideNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
         this.nameTags.updateVisibility(connectedPlayer);
      }
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating NameTag visibility";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      if (!p.teamData.isDisabled()) {
         if (this.nameTags.getOnlinePlayers().contains(p)) {
            this.nameTags.updateVisibility(p);
         }
      }
   }

   @NotNull
   @Override
   public ThreadExecutor getCustomThread() {
      return this.nameTags.getCustomThread();
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return this.nameTags.getFeatureName();
   }
}
