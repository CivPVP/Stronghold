package me.neznamy.tab.shared;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.libs.com.saicone.delivery4j.broker.RabbitMQBroker;
import me.neznamy.tab.libs.com.saicone.delivery4j.broker.RedisBroker;
import me.neznamy.tab.shared.config.files.Config;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.belowname.BelowName;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.header.HeaderFooter;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.pingspoof.PingSpoof;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.features.playerlistobjective.YellowNumber;
import me.neznamy.tab.shared.features.proxy.ProxyMessengerSupport;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.DisplayObjectiveListener;
import me.neznamy.tab.shared.features.types.EntryAddListener;
import me.neznamy.tab.shared.features.types.GameModeListener;
import me.neznamy.tab.shared.features.types.GroupListener;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.ObjectiveListener;
import me.neznamy.tab.shared.features.types.ProxyFeature;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.TabListClearListener;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.features.types.WorldSwitchListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.Unload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeatureManager {
   private final Map<String, TabFeature> features = new LinkedHashMap<>();
   @NotNull
   private TabFeature[] values = new TabFeature[0];

   public void load() {
      for (TabFeature f : this.values) {
         if (f instanceof Loadable) {
            ((Loadable)f).load();
         }
      }

      if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
         MySQLUserConfiguration users = (MySQLUserConfiguration)TAB.getInstance().getConfiguration().getUsers();

         for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            users.load(p);
         }
      }
   }

   public void unload() {
      for (TabFeature f : this.values) {
         if (f instanceof CustomThreaded) {
            ((CustomThreaded)f).getCustomThread().shutdown();
         }

         if (f instanceof UnLoadable) {
            ((UnLoadable)f).unload();
         }
      }

      for (TabFeature f : this.values) {
         f.deactivate();
      }

      long time = System.currentTimeMillis();

      for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
         player.getScoreboard().clear();
         player.getBossBar().clear();
      }

      TAB.getInstance().debug("Unregistered all scoreboard teams, objectives and boss bars for all players in " + (System.currentTimeMillis() - time) + "ms");
      TAB.getInstance().getPlaceholderManager().getTabExpansion().unregisterExpansion();
      if (TAB.getInstance().getPlatform() instanceof ProxyPlatform) {
         for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            ((ProxyTabPlayer)player).sendPluginMessage(new Unload());
         }
      }

      TAB.getInstance().getPlatform().unregisterAllCustomCommands();
   }

   public void onGroupChange(@NotNull TabPlayer player) {
      for (TabFeature f : this.values) {
         if (f instanceof GroupListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((GroupListener)f).onGroupChange(player), f.getFeatureName(), "Processing group change"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onGameModeChange(@NotNull TabPlayer player) {
      for (TabFeature f : this.values) {
         if (f instanceof GameModeListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((GameModeListener)f).onGameModeChange(player), f.getFeatureName(), "Processing gamemode change"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onQuit(@Nullable TabPlayer disconnectedPlayer) {
      if (disconnectedPlayer != null) {
         disconnectedPlayer.markOffline();
         long millis = System.currentTimeMillis();

         for (TabFeature f : this.values) {
            if (f instanceof QuitListener) {
               TimedCaughtTask task = new TimedCaughtTask(
                  TAB.getInstance().getCpu(), () -> ((QuitListener)f).onQuit(disconnectedPlayer), f.getFeatureName(), "Player Quit"
               );
               if (f instanceof CustomThreaded) {
                  ((CustomThreaded)f).getCustomThread().execute(task);
               } else {
                  task.run();
               }
            }
         }

         TAB.getInstance().removePlayer(disconnectedPlayer);
         this.removeforcedDisplayName(disconnectedPlayer.getTablistId());
         TAB.getInstance().debug("Player quit of " + disconnectedPlayer.getName() + " processed in " + (System.currentTimeMillis() - millis) + "ms");
         ProxySupport proxy = this.getFeature("ProxySupport");
         if (proxy != null) {
            ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(disconnectedPlayer.getUniqueId());
            if (proxyPlayer != null && proxyPlayer.getConnectionState() == ProxyPlayer.ConnectionState.QUEUED) {
               this.onJoin(proxyPlayer);
            }
         }
      }
   }

   private void removeforcedDisplayName(@NotNull UUID id) {
      ProxySupport proxy = this.getFeature("ProxySupport");
      if (proxy != null) {
         ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(id);
         if (proxyPlayer != null) {
            return;
         }
      }

      if (TAB.getInstance().getPlayer(id) == null) {
         for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList)all.getTabList()).getForcedDisplayNames().remove(id);
            ((TrackedTabList)all.getTabList()).getBlockedSpectators().remove(id);
         }
      }
   }

   public void onJoin(@NotNull TabPlayer connectedPlayer) {
      long millis = System.currentTimeMillis();
      TAB.getInstance().addPlayer(connectedPlayer);

      for (TabFeature f : this.values) {
         if (f instanceof JoinListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((JoinListener)f).onJoin(connectedPlayer), f.getFeatureName(), "Player Join"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }

      connectedPlayer.markAsLoaded(true);
      TAB.getInstance().debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis() - millis) + "ms");
      if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
         MySQLUserConfiguration users = (MySQLUserConfiguration)TAB.getInstance().getConfiguration().getUsers();
         users.load(connectedPlayer);
      }
   }

   public void onWorldChange(@NotNull UUID playerUUID, @NotNull World to) {
      TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
      if (changed != null) {
         World from = changed.world;
         changed.world = to;

         for (TabFeature f : this.values) {
            if (f instanceof WorldSwitchListener) {
               TimedCaughtTask task = new TimedCaughtTask(
                  TAB.getInstance().getCpu(), () -> ((WorldSwitchListener)f).onWorldChange(changed, from, to), f.getFeatureName(), "World Switch"
               );
               if (f instanceof CustomThreaded) {
                  ((CustomThreaded)f).getCustomThread().execute(task);
               } else {
                  task.run();
               }
            }
         }

         ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%world%")).updateValue(changed, to.getName());
      }
   }

   public void onServerChange(@NotNull UUID playerUUID, @NotNull Server to) {
      TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
      if (changed != null) {
         Server from = changed.server;
         changed.server = to;
         ((ProxyTabPlayer)changed).sendJoinPluginMessage();
         ((TrackedTabList)changed.getTabList()).resendHeaderFooter();

         for (TabFeature f : this.values) {
            if (f instanceof ServerSwitchListener) {
               TimedCaughtTask task = new TimedCaughtTask(
                  TAB.getInstance().getCpu(), () -> ((ServerSwitchListener)f).onServerChange(changed, from, to), f.getFeatureName(), "Server Switch"
               );
               if (f instanceof CustomThreaded) {
                  ((CustomThreaded)f).getCustomThread().execute(task);
               } else {
                  task.run();
               }
            }
         }

         ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%server%")).updateValue(changed, to.getName());
      }
   }

   public void onDisplayObjective(@NotNull TabPlayer packetReceiver, int slot, @NotNull String objective) {
      for (TabFeature f : this.values) {
         if (f instanceof DisplayObjectiveListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> ((DisplayObjectiveListener)f).onDisplayObjective(packetReceiver, slot, objective),
               f.getFeatureName(),
               "Checking for other plugins"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onObjective(@NotNull TabPlayer packetReceiver, int action, @NotNull String objective) {
      for (TabFeature f : this.values) {
         if (f instanceof ObjectiveListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> ((ObjectiveListener)f).onObjective(packetReceiver, action, objective),
               f.getFeatureName(),
               "Checking for other plugins"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onVanishStatusChange(@NotNull TabPlayer player) {
      for (TabFeature f : this.values) {
         if (f instanceof VanishListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((VanishListener)f).onVanishStatusChange(player), f.getFeatureName(), "Vanish status change"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
      for (TabFeature f : this.values) {
         if (f instanceof EntryAddListener) {
            long time = System.nanoTime();
            ((EntryAddListener)f).onEntryAdd(packetReceiver, id, name);
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), "Compatibility with nick plugins", System.nanoTime() - time);
         }
      }
   }

   public void onTabListClear(TabPlayer packetReceiver) {
      for (TabFeature f : this.values) {
         if (f instanceof TabListClearListener) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((TabListClearListener)f).onTabListClear(packetReceiver), f.getFeatureName(), "TabList entry re-add"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onProxyLoadRequest() {
      for (TabFeature f : this.values) {
         if (f instanceof ProxyFeature) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((ProxyFeature)f).onProxyLoadRequest(), f.getFeatureName(), "Processing reload from another proxy"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onJoin(@NotNull ProxyPlayer connectedPlayer) {
      TAB.getInstance()
         .getCpu()
         .getProcessingThread()
         .executeLater(
            new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> {
                  if (connectedPlayer.getConnectionState() != ProxyPlayer.ConnectionState.DISCONNECTED) {
                     connectedPlayer.setConnectionState(ProxyPlayer.ConnectionState.CONNECTED);

                     for (TabFeature f : this.values) {
                        if (f instanceof ProxyFeature) {
                           TimedCaughtTask task = new TimedCaughtTask(
                              TAB.getInstance().getCpu(), () -> ((ProxyFeature)f).onJoin(connectedPlayer), f.getFeatureName(), "Player Join"
                           );
                           if (f instanceof CustomThreaded) {
                              ((CustomThreaded)f).getCustomThread().execute(task);
                           } else {
                              task.run();
                           }
                        }
                     }

                     if (connectedPlayer.isVanished()) {
                        this.onVanishStatusChange(connectedPlayer);
                     }
                  }
               },
               this.<TabFeature>getFeature("ProxySupport").getFeatureName(),
               "Player Join"
            ),
            200
         );
   }

   public void onServerSwitch(@NotNull ProxyPlayer player) {
      for (TabFeature f : this.values) {
         if (f instanceof ProxyFeature) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((ProxyFeature)f).onServerSwitch(player), f.getFeatureName(), "Server Switch"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public void onQuit(@NotNull ProxyPlayer disconnectedPlayer) {
      disconnectedPlayer.setConnectionState(ProxyPlayer.ConnectionState.DISCONNECTED);

      for (TabFeature f : this.values) {
         if (f instanceof ProxyFeature) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((ProxyFeature)f).onQuit(disconnectedPlayer), f.getFeatureName(), "Player Quit"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }

      this.removeforcedDisplayName(disconnectedPlayer.getTablistId());
   }

   public void onVanishStatusChange(@NotNull ProxyPlayer player) {
      for (TabFeature f : this.values) {
         if (f instanceof ProxyFeature) {
            TimedCaughtTask task = new TimedCaughtTask(
               TAB.getInstance().getCpu(), () -> ((ProxyFeature)f).onVanishStatusChange(player), f.getFeatureName(), "Vanish status change"
            );
            if (f instanceof CustomThreaded) {
               ((CustomThreaded)f).getCustomThread().execute(task);
            } else {
               task.run();
            }
         }
      }
   }

   public synchronized void registerFeature(@NotNull String featureName, @NotNull TabFeature featureHandler) {
      this.features.put(featureName, featureHandler);
      this.values = this.features.values().toArray(new TabFeature[0]);
      if (featureHandler instanceof VanishListener) {
         TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%vanished%");
      }

      if (featureHandler instanceof GameModeListener) {
         TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%gamemode%");
      }
   }

   public void unregisterFeature(@NotNull String featureName) {
      this.features.remove(featureName);
      this.values = this.features.values().toArray(new TabFeature[0]);
   }

   public boolean isFeatureEnabled(@NotNull String name) {
      return this.features.containsKey(name);
   }

   public <T extends TabFeature> T getFeature(@NotNull String name) {
      return (T)this.features.get(name);
   }

   public void loadFeaturesFromConfig() {
      Config config = TAB.getInstance().getConfiguration().getConfig();
      FeatureManager featureManager = TAB.getInstance().getFeatureManager();
      if (config.isEnableProxySupport()) {
         String type = config.getConfig().getString("proxy-support.type", "PLUGIN");
         ProxySupport proxy = null;
         if (type.equalsIgnoreCase("PLUGIN")) {
            String plugin = config.getConfig().getString("proxy-support.plugin.name", "RedisBungee");
            proxy = TAB.getInstance().getPlatform().getProxySupport(plugin);
         } else if (type.equalsIgnoreCase("REDIS")) {
            String url = config.getConfig().getString("proxy-support.redis.url", "redis://:password@localhost:6379/0");
            proxy = new ProxyMessengerSupport("Redis", () -> RedisBroker.of(url));
         } else if (type.equalsIgnoreCase("RABBITMQ")) {
            String exchange = config.getConfig().getString("proxy-support.rabbitmq.exchange", "plugin");
            String url = config.getConfig().getString("proxy-support.rabbitmq.url", "amqp://guest:guest@localhost:5672/%2F");
            proxy = new ProxyMessengerSupport("RabbitMQ", () -> RabbitMQBroker.of(url, exchange));
         }

         if (proxy != null) {
            TAB.getInstance().getFeatureManager().registerFeature("ProxySupport", proxy);
         }
      }

      if (config.isPipelineInjection()) {
         PipelineInjector inj = TAB.getInstance().getPlatform().createPipelineInjector();
         if (inj != null) {
            featureManager.registerFeature("injection", inj);
         }
      }

      if (config.getPerWorldPlayerList() != null) {
         TabFeature pwp = TAB.getInstance().getPlatform().getPerWorldPlayerList(config.getPerWorldPlayerList());
         if (pwp != null) {
            featureManager.registerFeature("PerWorldPlayerList", pwp);
         }
      }

      if (config.getBossbar() != null) {
         featureManager.registerFeature("BossBar", new BossBarManagerImpl(config.getBossbar()));
      }

      if (config.getPingSpoof() != null) {
         featureManager.registerFeature("PingSpoof", new PingSpoof(config.getPingSpoof()));
      }

      if (config.getHeaderFooter() != null) {
         featureManager.registerFeature("HeaderFooter", new HeaderFooter(config.getHeaderFooter()));
      }

      if (config.isPreventSpectatorEffect()) {
         featureManager.registerFeature("SpectatorFix", new SpectatorFix());
      }

      if (config.getScoreboard() != null) {
         featureManager.registerFeature("ScoreBoard", new ScoreboardManagerImpl(config.getScoreboard()));
      }

      if (config.getPlayerlistObjective() != null) {
         featureManager.registerFeature("YellowNumber", new YellowNumber(config.getPlayerlistObjective()));
      }

      if (config.getBelowname() != null) {
         featureManager.registerFeature("BelowName", new BelowName(config.getBelowname()));
      }

      if (config.getSorting() != null) {
         featureManager.registerFeature("sorting", new Sorting(config.getSorting()));
      }

      if (config.getTablistFormatting() != null) {
         featureManager.registerFeature("PlayerList", new PlayerList(config.getTablistFormatting()));
      }

      if (config.getTeams() != null) {
         featureManager.registerFeature("NameTag16", new NameTag(config.getTeams()));
      }

      if (config.getLayout() != null) {
         featureManager.registerFeature("layout", new LayoutManagerImpl(config.getLayout()));
      }

      if (config.getGlobalPlayerList() != null) {
         featureManager.registerFeature("GlobalPlayerList", new GlobalPlayerList(config.getGlobalPlayerList()));
      }

      featureManager.registerFeature("Nick", new NickCompatibility());
   }
}
