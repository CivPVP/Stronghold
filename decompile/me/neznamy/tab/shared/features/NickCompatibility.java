package me.neznamy.tab.shared.features;

import java.util.Collections;
import java.util.UUID;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.belowname.BelowName;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.playerlistobjective.YellowNumber;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.EntryAddListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NickCompatibility extends TabFeature implements EntryAddListener {
   @Nullable
   private final NameTag nameTags = TAB.getInstance().getNameTagManager();
   @Nullable
   private final BelowName belowname = TAB.getInstance().getFeatureManager().getFeature("BelowName");
   @Nullable
   private final YellowNumber yellownumber = TAB.getInstance().getFeatureManager().getFeature("YellowNumber");
   @Nullable
   private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");

   @Override
   public synchronized void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
      TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(id);
      if (packetPlayer != null && packetPlayer == packetReceiver && !packetPlayer.getNickname().equals(name)) {
         packetPlayer.setNickname(name);
         TAB.getInstance().debug("Processing name change of player " + packetPlayer.getName() + " to " + name);
         this.processNameChange(packetPlayer);
      }

      if (this.proxy != null) {
         ProxyPlayer proxyPlayer = this.proxy.getProxyPlayers().get(id);
         if (proxyPlayer == null) {
            return;
         }

         if (!proxyPlayer.getNickname().equals(name)) {
            proxyPlayer.setNickname(name);
            TAB.getInstance().debug("[Proxy Support] Processing name change of proxy player " + proxyPlayer.getName() + " to " + name);
            this.processNameChange(proxyPlayer);
         }
      }
   }

   public void processNameChange(@NotNull TabPlayer player) {
      CpuManager cpu = TAB.getInstance().getCpu();
      cpu.getProcessingThread()
         .execute(
            new TimedCaughtTask(
               cpu,
               () -> {
                  if (this.nameTags != null && !player.teamData.isDisabled()) {
                     for (TabPlayer viewer : this.nameTags.getOnlinePlayers().getPlayers()) {
                        viewer.getScoreboard().unregisterTeam(player.sortingData.getShortTeamName());
                        viewer.getScoreboard()
                           .registerTeam(
                              player.sortingData.getShortTeamName(),
                              this.nameTags.getPrefixCache().get(player.teamData.prefix.getFormat(viewer)),
                              this.nameTags.getSuffixCache().get(player.teamData.suffix.getFormat(viewer)),
                              this.nameTags.getTeamVisibility(player, viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER,
                              player.teamData.getCollisionRule() ? Scoreboard.CollisionRule.ALWAYS : Scoreboard.CollisionRule.NEVER,
                              Collections.singletonList(player.getNickname()),
                              this.nameTags.getTeamOptions(),
                              this.nameTags.getLastColorCache().get(player.teamData.prefix.getFormat(viewer)).getLastStyle().toEnumChatFormat()
                           );
                     }
                  }

                  if (this.belowname != null) {
                     this.belowname.processNicknameChange(player);
                  }

                  if (this.yellownumber != null) {
                     this.yellownumber.processNicknameChange(player);
                  }
               },
               this.getFeatureName(),
               "Compatibility with nick plugins"
            )
         );
   }

   private void processNameChange(ProxyPlayer player) {
      CpuManager cpu = TAB.getInstance().getCpu();
      cpu.getProcessingThread()
         .execute(
            new TimedCaughtTask(
               cpu,
               () -> {
                  if (this.nameTags != null && player.getNametag() != null) {
                     String teamName = player.getNametag().getResolvedTeamName();

                     for (TabPlayer viewer : this.nameTags.getOnlinePlayers().getPlayers()) {
                        viewer.getScoreboard().unregisterTeam(teamName);
                        viewer.getScoreboard()
                           .registerTeam(
                              teamName,
                              player.getNametag().getFeature().getPrefixCache().get(player.getNametag().getPrefix()),
                              player.getNametag().getFeature().getSuffixCache().get(player.getNametag().getSuffix()),
                              player.getNametag().getNameVisibility(),
                              Scoreboard.CollisionRule.ALWAYS,
                              Collections.singletonList(player.getNickname()),
                              this.nameTags.getTeamOptions(),
                              player.getNametag().getFeature().getLastColorCache().get(player.getNametag().getPrefix()).getLastStyle().toEnumChatFormat()
                           );
                     }
                  }

                  if (this.belowname != null && player.getBelowname() != null) {
                     for (TabPlayer all : this.belowname.getOnlinePlayers().getPlayers()) {
                        all.getScoreboard()
                           .setScore(
                              "TAB-BelowName",
                              player.getNickname(),
                              player.getBelowname().getValue(),
                              null,
                              player.getBelowname().getFeature().getCache().get(player.getBelowname().getFancyValue())
                           );
                     }
                  }

                  if (this.yellownumber != null && player.getPlayerlist() != null) {
                     for (TabPlayer all : this.yellownumber.getOnlinePlayers().getPlayers()) {
                        all.getScoreboard()
                           .setScore(
                              "TAB-PlayerList",
                              player.getNickname(),
                              player.getPlayerlist().getValue(),
                              null,
                              player.getPlayerlist().getFeature().getCache().get(player.getPlayerlist().getFancyValue())
                           );
                     }
                  }
               },
               this.getFeatureName(),
               "Compatibility with nick plugins"
            )
         );
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "Nick compatibility";
   }
}
