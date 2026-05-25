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

public class CollisionManager extends RefreshableFeature implements JoinListener, Loadable, CustomThreaded {
   @NotNull
   private final NameTag nameTags;
   @NotNull
   private final Condition enableCollision;

   public CollisionManager(@NotNull NameTag nameTags) {
      this.nameTags = nameTags;
      this.enableCollision = TAB.getInstance()
         .getPlaceholderManager()
         .getConditionManager()
         .getByNameOrExpression(nameTags.getConfiguration().getEnableCollision());
   }

   @Override
   public void load() {
      TAB.getInstance().getPlaceholderManager().registerInternalPlayerPlaceholder("%collision%", 500, p -> {
         TabPlayer player = (TabPlayer)p;
         if (player.teamData.forcedCollision != null) {
            return Boolean.toString(player.teamData.forcedCollision);
         }

         boolean newCollision = !((TabPlayer)p).isDisguised() && this.enableCollision.isMet((TabPlayer)p);
         player.teamData.collisionRule = newCollision;
         return Boolean.toString(newCollision);
      });
      this.addUsedPlaceholder("%collision%");

      for (TabPlayer all : this.nameTags.getOnlinePlayers().getPlayers()) {
         this.onJoin(all);
      }
   }

   @Override
   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      connectedPlayer.teamData.collisionRule = this.enableCollision.isMet(connectedPlayer);
   }

   @NotNull
   @Override
   public String getRefreshDisplayName() {
      return "Updating collision";
   }

   @Override
   public void refresh(@NotNull TabPlayer p, boolean force) {
      if (!p.teamData.isDisabled()) {
         if (this.nameTags.getOnlinePlayers().contains(p)) {
            this.nameTags.updateCollision(p, false);
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
