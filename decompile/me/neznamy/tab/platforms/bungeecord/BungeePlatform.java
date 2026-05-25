package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import me.neznamy.tab.libs.org.bstats.bungeecord.Metrics;
import me.neznamy.tab.libs.org.bstats.charts.SimplePie;
import me.neznamy.tab.platforms.bungeecord.features.BungeeRedisSupport;
import me.neznamy.tab.platforms.bungeecord.hook.BungeePremiumVanishHook;
import me.neznamy.tab.platforms.bungeecord.injection.BungeePipelineInjector;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1193;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList17;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList18;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.ObjectInfo;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.DummyBossBar;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.PerformanceUtil;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.ObjectComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.objects.PlayerObject;
import net.md_5.bungee.api.chat.objects.SpriteObject;
import net.md_5.bungee.api.chat.player.Profile;
import net.md_5.bungee.api.chat.player.Property;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BungeePlatform extends ProxyPlatform {
   @NotNull
   private final BungeeTAB plugin;
   private final List<Command> customCommands = new ArrayList<>();

   public BungeePlatform(@NotNull BungeeTAB plugin) {
      this.plugin = plugin;
      if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null) {
         new BungeePremiumVanishHook(this).register();
      }
   }

   @Override
   public void loadPlayers() {
      for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
         TAB.getInstance().addPlayer(new BungeeTabPlayer(this, p));
      }
   }

   @Override
   public void registerPlaceholders() {
      super.registerPlaceholders();

      for (String serverName : ProxyServer.getInstance().getConfig().getServers().keySet()) {
         Server server = Server.byName(serverName);
         TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder("%online_" + serverName + "%", 1000, () -> {
            int count = 0;

            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
               if (player.server == server && !player.isVanished()) {
                  count++;
               }
            }

            ProxySupport proxySupport = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
            if (proxySupport != null) {
               for (ProxyPlayer player : proxySupport.getProxyPlayers().values()) {
                  if (player.server == server && !player.isVanished()) {
                     count++;
                  }
               }
            }

            return PerformanceUtil.toString(count);
         });
      }
   }

   @Nullable
   @Override
   public ProxySupport getProxySupport(@NotNull String plugin) {
      return plugin.equalsIgnoreCase("RedisBungee")
            && ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI")
            && RedisBungeeAPI.getRedisBungeeApi() != null
         ? new BungeeRedisSupport(this.plugin)
         : null;
   }

   @Override
   public void logInfo(@NotNull TabComponent message) {
      this.plugin.getLogger().info(message.toLegacyText());
   }

   @Override
   public void logWarn(@NotNull TabComponent message) {
      this.plugin.getLogger().warning("§c" + message.toLegacyText());
   }

   @NotNull
   @Override
   public String getServerVersionInfo() {
      return "[BungeeCord] " + this.plugin.getProxy().getName() + " - " + this.plugin.getProxy().getVersion();
   }

   @Override
   public void registerListener() {
      ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new BungeeEventListener());
   }

   @Override
   public void registerCommand() {
      ProxyServer.getInstance().getPluginManager().registerCommand(this.plugin, new BungeeTabCommand(this.getCommand()));
   }

   @Override
   public void startMetrics() {
      new Metrics(this.plugin, 10535)
         .addCustomChart(
            new SimplePie("global_playerlist_enabled", () -> TAB.getInstance().getFeatureManager().isFeatureEnabled("GlobalPlayerList") ? "Yes" : "No")
         );
   }

   @NotNull
   @Override
   public File getDataFolder() {
      return this.plugin.getDataFolder();
   }

   @Nullable
   @Override
   public PipelineInjector createPipelineInjector() {
      return new BungeePipelineInjector();
   }

   @Override
   public void registerChannel() {
      ProxyServer.getInstance().registerChannel("tab:bridge-6");
   }

   @NotNull
   public BaseComponent[] convertComponent(@NotNull TabComponent component) {
      return new BaseComponent[]{
         this.createComponent(component, ProtocolVersion.V1_21_9),
         this.createComponent(component, ProtocolVersion.V1_16),
         this.createComponent(component, ProtocolVersion.V1_8)
      };
   }

   @NotNull
   public BaseComponent transformComponent(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
      return this.pickCorrectComponent(component.convert(), version);
   }

   @NotNull
   public BaseComponent pickCorrectComponent(@NotNull BaseComponent[] components, @NotNull ProtocolVersion version) {
      if (version.getNetworkId() >= ProtocolVersion.V1_21_9.getNetworkId()) {
         return components[0];
      } else {
         return version.getNetworkId() >= ProtocolVersion.V1_16.getNetworkId() ? components[1] : components[2];
      }
   }

   @NotNull
   private BaseComponent createComponent(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
      BaseComponent bComponent;
      if (component instanceof TabTextComponent) {
         bComponent = new TextComponent(((TabTextComponent)component).getText());
      } else if (component instanceof TabTranslatableComponent) {
         bComponent = new TranslatableComponent(((TabTranslatableComponent)component).getKey(), new Object[0]);
      } else if (component instanceof TabKeybindComponent) {
         bComponent = new KeybindComponent(((TabKeybindComponent)component).getKeybind());
      } else {
         if (!(component instanceof TabObjectComponent)) {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
         }

         if (version.getNetworkId() >= ProtocolVersion.V1_21_9.getNetworkId()) {
            ObjectInfo info = ((TabObjectComponent)component).getContents();
            if (info instanceof TabAtlasSprite) {
               bComponent = new ObjectComponent(new SpriteObject(((TabAtlasSprite)info).getAtlas(), ((TabAtlasSprite)info).getSprite()));
            } else {
               if (!(info instanceof TabPlayerSprite)) {
                  throw new IllegalStateException("Unexpected object component type: " + info.getClass().getName());
               }

               bComponent = new ObjectComponent(
                  new PlayerObject(
                     new Profile(
                        ((TabPlayerSprite)info).getName(),
                        ((TabPlayerSprite)info).getId(),
                        ((TabPlayerSprite)info).getSkin() == null
                           ? new Property[0]
                           : new Property[]{
                              new Property("textures", ((TabPlayerSprite)info).getSkin().getValue(), ((TabPlayerSprite)info).getSkin().getSignature())
                           }
                     ),
                     ((TabPlayerSprite)info).isShowHat()
                  )
               );
            }
         } else {
            bComponent = new TextComponent(component.toLegacyText());
         }
      }

      TabStyle modifier = component.getModifier();
      if (modifier.getColor() != null) {
         if (version.getMinorVersion() >= 16) {
            bComponent.setColor(ChatColor.of("#" + modifier.getColor().getHexCode()));
         } else {
            bComponent.setColor(ChatColor.of(modifier.getColor().getLegacyColor().name()));
         }
      }

      bComponent.setShadowColor(
         modifier.getShadowColor() == null
            ? null
            : new Color(
               modifier.getShadowColor() >> 16 & 0xFF,
               modifier.getShadowColor() >> 8 & 0xFF,
               modifier.getShadowColor() & 0xFF,
               modifier.getShadowColor() >> 24 & 0xFF
            )
      );
      bComponent.setBold(modifier.getBold());
      bComponent.setItalic(modifier.getItalic());
      bComponent.setObfuscated(modifier.getObfuscated());
      bComponent.setStrikethrough(modifier.getStrikethrough());
      bComponent.setUnderlined(modifier.getUnderlined());
      bComponent.setFont(modifier.getFont());

      for (TabComponent extra : component.getExtra()) {
         bComponent.addExtra(this.createComponent(extra, version));
      }

      return bComponent;
   }

   @NotNull
   @Override
   public Scoreboard createScoreboard(@NotNull TabPlayer player) {
      return new BungeeScoreboard((BungeeTabPlayer)player);
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NotNull TabPlayer player) {
      return player.getVersion().getMinorVersion() >= 9 ? new BungeeBossBar((BungeeTabPlayer)player) : new DummyBossBar();
   }

   @NotNull
   @Override
   public TabList createTabList(@NotNull TabPlayer player) {
      if (player.getVersionId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
         return new BungeeTabList1193((BungeeTabPlayer)player);
      } else {
         return player.getVersionId() >= ProtocolVersion.V1_8.getNetworkId()
            ? new BungeeTabList18((BungeeTabPlayer)player)
            : new BungeeTabList17((BungeeTabPlayer)player);
      }
   }

   @Override
   public boolean supportsScoreboards() {
      return true;
   }

   @NotNull
   @Override
   public String getCommand() {
      return "btab";
   }

   @Override
   public void registerCustomCommand(@NotNull String commandName, @NotNull final Consumer<TabPlayer> function) {
      Command cmd = new Command(commandName) {
         public void execute(CommandSender commandSender, String[] strings) {
            if (commandSender instanceof ConsoleCommandSender) {
               commandSender.sendMessage(
                  BungeePlatform.this.createComponent(
                     TabComponent.fromColoredText(TAB.getInstance().getConfiguration().getMessages().getCommandOnlyFromGame()), ProtocolVersion.values()[1]
                  )
               );
            } else {
               TabPlayer p = TAB.getInstance().getPlayer(((ProxiedPlayer)commandSender).getUniqueId());
               if (p != null) {
                  function.accept(p);
               }
            }
         }
      };
      this.customCommands.add(cmd);
      ProxyServer.getInstance().getPluginManager().registerCommand(this.plugin, cmd);
   }

   @Override
   public void unregisterAllCustomCommands() {
      for (Command cmd : this.customCommands) {
         ProxyServer.getInstance().getPluginManager().unregisterCommand(cmd);
      }
   }
}
