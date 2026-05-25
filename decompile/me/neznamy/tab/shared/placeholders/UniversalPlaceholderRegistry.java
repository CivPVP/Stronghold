package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.animation.Animation;
import me.neznamy.tab.shared.placeholders.animation.AnimationConfiguration;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.placeholders.conditions.ConditionsSection;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

public class UniversalPlaceholderRegistry {
   private final DecimalFormat decimal2;

   public UniversalPlaceholderRegistry() {
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      this.decimal2 = new DecimalFormat("#.##", symbols);
   }

   public void registerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
      this.registerConstants(manager);
      this.registerServerPlaceholders(manager);
      this.registerPlayerPlaceholders(manager);
   }

   private void registerConstants(@NotNull PlaceholderManagerImpl manager) {
      manager.registerInternalPlayerPlaceholder("%bedrock%", -1, p -> Boolean.toString(p.isBedrockPlayer()));
      manager.registerInternalPlayerPlaceholder("%player%", -1, TabPlayer::getName);
      manager.registerInternalPlayerPlaceholder("%uuid%", -1, p -> p.getUniqueId().toString());
      manager.registerInternalPlayerPlaceholder("%world%", -1, p -> ((me.neznamy.tab.shared.platform.TabPlayer)p).world.getName());
      manager.registerInternalPlayerPlaceholder("%server%", -1, p -> ((me.neznamy.tab.shared.platform.TabPlayer)p).server.getName());
      manager.registerInternalPlayerPlaceholder("%player-version%", -1, p -> ((me.neznamy.tab.shared.platform.TabPlayer)p).getVersion().getFriendlyName());
      manager.registerInternalPlayerPlaceholder(
         "%player-version-id%", -1, p -> PerformanceUtil.toString(((me.neznamy.tab.shared.platform.TabPlayer)p).getVersionId())
      );
      manager.registerInternalServerPlaceholder("%%", -1, () -> "%");
      manager.registerInternalServerPlaceholder("%memory-max%", -1, () -> PerformanceUtil.toString((int)(Runtime.getRuntime().maxMemory() / 1024L / 1024L)));
      manager.registerInternalServerPlaceholder(
         "%memory-max-gb%", -1, () -> this.decimal2.format((float)Runtime.getRuntime().maxMemory() / 1024.0F / 1024.0F / 1024.0F)
      );
      if (!LuckPermsHook.getInstance().isInstalled()) {
         manager.registerInternalServerPlaceholder("%luckperms-prefix%", -1, () -> "");
         manager.registerInternalServerPlaceholder("%luckperms-prefixes%", -1, () -> "");
         manager.registerInternalServerPlaceholder("%luckperms-suffix%", -1, () -> "");
         manager.registerInternalServerPlaceholder("%luckperms-suffixes%", -1, () -> "");
         manager.registerInternalServerPlaceholder("%luckperms-weight%", -1, () -> "");
      }
   }

   private void registerServerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
      PlaceholdersConfiguration placeholders = TAB.getInstance().getConfiguration().getConfig().getPlaceholders();
      manager.registerInternalServerPlaceholder(
         "%time%", 500, () -> placeholders.getTimeFormat().format(new Date(System.currentTimeMillis() + (int)(placeholders.getTimeOffset() * 3600000.0)))
      );
      manager.registerInternalServerPlaceholder(
         "%date%", 60000, () -> placeholders.getDateFormat().format(new Date(System.currentTimeMillis() + (int)(placeholders.getTimeOffset() * 3600000.0)))
      );
      manager.registerInternalServerPlaceholder(
         "%memory-used%", 200, () -> PerformanceUtil.toString((int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L))
      );
      manager.registerInternalServerPlaceholder(
         "%memory-used-gb%",
         200,
         () -> this.decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0F / 1024.0F / 1024.0F)
      );
      manager.registerInternalServerPlaceholder("%online%", 1000, () -> {
         int count = 0;

         for (me.neznamy.tab.shared.platform.TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            if (!player.isVanished()) {
               count++;
            }
         }

         ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
         if (proxy != null) {
            for (ProxyPlayer player : proxy.getProxyPlayers().values()) {
               if (!player.isVanished()) {
                  count++;
               }
            }
         }

         return PerformanceUtil.toString(count);
      });
      manager.registerInternalServerPlaceholder("%staffonline%", 2000, () -> {
         int count = 0;

         for (me.neznamy.tab.shared.platform.TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            if (!player.isVanished() && player.hasPermission("tab.staff")) {
               count++;
            }
         }

         ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
         if (proxy != null) {
            for (ProxyPlayer player : proxy.getProxyPlayers().values()) {
               if (!player.isVanished() && player.isStaff()) {
                  count++;
               }
            }
         }

         return PerformanceUtil.toString(count);
      });
      manager.registerInternalServerPlaceholder("%nonstaffonline%", 2000, () -> {
         int count = 0;

         for (me.neznamy.tab.shared.platform.TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            if (!player.hasPermission("tab.staff") && !player.isVanished()) {
               count++;
            }
         }

         return PerformanceUtil.toString(count);
      });
   }

   private void registerPlayerPlaceholders(@NotNull PlaceholderManagerImpl manager) {
      boolean proxy = TAB.getInstance().getPlatform().isProxy();
      manager.registerInternalPlayerPlaceholder("%group%", -1, TabPlayer::getGroup);
      manager.registerInternalPlayerPlaceholder("%ping%", 500, p -> PerformanceUtil.toString(((me.neznamy.tab.shared.platform.TabPlayer)p).getPing()));
      manager.registerInternalPlayerPlaceholder("%vanished%", 1000, p -> Boolean.toString(((me.neznamy.tab.shared.platform.TabPlayer)p).isVanished()));
      manager.registerInternalPlayerPlaceholder("%worldonline%", 1000, p -> {
         int count = 0;

         for (me.neznamy.tab.shared.platform.TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            if (((me.neznamy.tab.shared.platform.TabPlayer)p).world == player.world && !player.isVanished()) {
               count++;
            }
         }

         return PerformanceUtil.toString(count);
      });
      manager.registerInternalPlayerPlaceholder("%serveronline%", 1000, p -> {
         int count = 0;

         for (me.neznamy.tab.shared.platform.TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            if (((me.neznamy.tab.shared.platform.TabPlayer)p).server == player.server && !player.isVanished()) {
               count++;
            }
         }

         ProxySupport proxySupport = TAB.getInstance().getFeatureManager().getFeature("ProxySupport");
         if (proxySupport != null) {
            for (ProxyPlayer player : proxySupport.getProxyPlayers().values()) {
               if (((me.neznamy.tab.shared.platform.TabPlayer)p).server == player.server && !player.isVanished()) {
                  count++;
               }
            }
         }

         return PerformanceUtil.toString(count);
      });
      manager.registerInternalPlayerPlaceholder(
         "%gamemode%", proxy ? -1 : 100, p -> PerformanceUtil.toString(((me.neznamy.tab.shared.platform.TabPlayer)p).getGamemode())
      );
      if (LuckPermsHook.getInstance().isInstalled()) {
         manager.registerInternalPlayerPlaceholder(
            "%luckperms-prefix%", 1000, p -> LuckPermsHook.getInstance().getPrefix((me.neznamy.tab.shared.platform.TabPlayer)p)
         );
         manager.registerInternalPlayerPlaceholder(
            "%luckperms-prefixes%", 1000, p -> LuckPermsHook.getInstance().getPrefixes((me.neznamy.tab.shared.platform.TabPlayer)p)
         );
         manager.registerInternalPlayerPlaceholder(
            "%luckperms-suffix%", 1000, p -> LuckPermsHook.getInstance().getSuffix((me.neznamy.tab.shared.platform.TabPlayer)p)
         );
         manager.registerInternalPlayerPlaceholder(
            "%luckperms-suffixes%", 1000, p -> LuckPermsHook.getInstance().getSuffixes((me.neznamy.tab.shared.platform.TabPlayer)p)
         );
         manager.registerInternalPlayerPlaceholder(
            "%luckperms-weight%", 1000, p -> PerformanceUtil.toString(LuckPermsHook.getInstance().getWeight((me.neznamy.tab.shared.platform.TabPlayer)p))
         );
      }

      for (Entry<String, AnimationConfiguration.AnimationDefinition> entry : TAB.getInstance()
         .getConfiguration()
         .getAnimations()
         .getAnimations()
         .getAnimations()
         .entrySet()) {
         Animation a = new Animation(manager, entry.getKey(), entry.getValue());
         manager.registerInternalPlayerPlaceholder(TabConstants.Placeholder.animation(a.getName()), a.getRefresh(), p -> a.getMessage());
      }

      for (Entry<String, ConditionsSection.ConditionDefinition> condition : TAB.getInstance()
         .getConfiguration()
         .getConfig()
         .getConditions()
         .getConditions()
         .entrySet()) {
         Condition c = new Condition(condition.getValue());
         manager.registerInternalPlayerPlaceholder(
            TabConstants.Placeholder.condition(c.getName()), c.getRefresh(), p -> c.getText((me.neznamy.tab.shared.platform.TabPlayer)p)
         );
         TAB.getInstance().getPlaceholderManager().getConditionManager().registerCondition(c);
      }

      manager.getConditionManager().finishSetups();
   }

   @Generated
   public DecimalFormat getDecimal2() {
      return this.decimal2;
   }
}
