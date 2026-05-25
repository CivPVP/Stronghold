package me.neznamy.tab.platforms.bukkit.platform;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FoliaPlatform extends BukkitPlatform {
   public FoliaPlatform(@NotNull JavaPlugin plugin) {
      super(plugin);
   }

   @Override
   public void loadPlayers() {
      super.loadPlayers();
      TAB.getInstance()
         .getCpu()
         .getProcessingThread()
         .repeatTask(
            new TimedCaughtTask(
               TAB.getInstance().getCpu(),
               () -> {
                  for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                     World actualWorld = World.byName(((Player)player.getPlayer()).getWorld().getName());
                     if (player.world != actualWorld) {
                        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), actualWorld);
                        PerWorldPlayerList pwp = TAB.getInstance().getFeatureManager().getFeature("PerWorldPlayerList");
                        if (pwp != null) {
                           this.runSync(
                              (Entity)player.getPlayer(),
                              () -> pwp.onWorldChange(new PlayerChangedWorldEvent((Player)player.getPlayer(), ((Player)player.getPlayer()).getWorld()))
                           );
                        }
                     }
                  }
               },
               "Folia compatibility",
               "Refreshing world"
            ),
            100
         );
   }

   @Override
   public void registerPlaceholders() {
      super.registerPlaceholders();
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      DecimalFormat decimal2 = new DecimalFormat("#.##", symbols);
      this.registerInternalSyncPlaceholder("%mspt%", 1000, p -> decimal2.format(Bukkit.getAverageTickTime()));
      this.registerInternalSyncPlaceholder("%tps%", 1000, p -> decimal2.format(Math.min(20.0, Bukkit.getTPS()[0])));
   }

   @Override
   public void registerSyncPlaceholder(@NotNull String identifier) {
      String syncedPlaceholder = "%" + identifier.substring(6);
      PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
      ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
         this.runSync((Entity)p.getPlayer(), () -> {
            long time = System.nanoTime();
            String output = this.isPlaceholderAPI() ? PlaceholderAPI.setPlaceholders((Player)p.getPlayer(), syncedPlaceholder) : identifier;
            long totalTime = System.nanoTime() - time;
            TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
            TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), "Phase #2 - Requesting new values", totalTime);
            TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output));
         });
         return null;
      });
   }

   private void registerInternalSyncPlaceholder(@NonNull String identifier, int refresh, @NonNull Function<TabPlayer, String> function) {
      if (identifier == null) {
         throw new NullPointerException("identifier is marked non-null but is null");
      }

      if (function == null) {
         throw new NullPointerException("function is marked non-null but is null");
      }

      PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
      ppl[0] = TAB.getInstance().getPlaceholderManager().registerInternalPlayerPlaceholder(identifier, refresh, p -> {
         this.runSync((Entity)p.getPlayer(), () -> {
            long time = System.nanoTime();
            String output = function.apply((TabPlayer)p);
            long totalTime = System.nanoTime() - time;
            TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
            TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), "Phase #2 - Requesting new values", totalTime);
            TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output));
         });
         return null;
      });
   }

   @Override
   public double getMSPT() {
      return -1.0;
   }

   @Override
   public void runSync(@NotNull Entity entity, @NotNull Runnable task) {
      try {
         Object entityScheduler = Entity.class.getMethod("getScheduler").invoke(entity);
         Consumer<?> consumer = $ -> task.run();
         entityScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class).invoke(entityScheduler, this.getPlugin(), consumer, null);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }
}
