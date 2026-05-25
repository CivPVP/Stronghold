package me.neznamy.tab.shared.features.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.proxy.message.Load;
import me.neznamy.tab.shared.features.proxy.message.LoadRequest;
import me.neznamy.tab.shared.features.proxy.message.PlayerJoin;
import me.neznamy.tab.shared.features.proxy.message.PlayerQuit;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.features.proxy.message.ServerSwitch;
import me.neznamy.tab.shared.features.proxy.message.UpdateVanishStatus;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.features.types.ServerSwitchListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ProxySupport extends TabFeature implements JoinListener, QuitListener, Loadable, UnLoadable, ServerSwitchListener, VanishListener {
   @NotNull
   protected final Map<UUID, ProxyPlayer> proxyPlayers = new ConcurrentHashMap<>();
   @NotNull
   private final Map<UUID, QueuedData> queuedData = new ConcurrentHashMap<>();
   @NotNull
   private final UUID proxy = UUID.randomUUID();
   @NotNull
   private final Map<String, Function<ByteArrayDataInput, ProxyMessage>> stringToClass = new HashMap<>();
   @NotNull
   private final Map<Class<? extends ProxyMessage>, String> classToString = new HashMap<>();
   private final AtomicLong idCounter = new AtomicLong(0L);

   protected ProxySupport() {
      this.registerMessage(Load.class, Load::new);
      this.registerMessage(LoadRequest.class, in -> new LoadRequest());
      this.registerMessage(PlayerJoin.class, PlayerJoin::new);
      this.registerMessage(PlayerQuit.class, PlayerQuit::new);
      this.registerMessage(ServerSwitch.class, ServerSwitch::new);
      this.registerMessage(UpdateVanishStatus.class, UpdateVanishStatus::new);
      TAB.getInstance().debug("[Proxy Support] Using channel name: TAB-2");
   }

   @NotNull
   @Override
   public String getFeatureName() {
      return "ProxySupport";
   }

   public synchronized void processMessage(@NotNull String msg) {
      ByteArrayDataInput in = ByteStreams.newDataInput(Base64.getDecoder().decode(msg));
      String proxy = in.readUTF();
      if (!proxy.equals(this.proxy.toString())) {
         String action = in.readUTF();
         Function<ByteArrayDataInput, ProxyMessage> function = this.stringToClass.get(action);
         if (function == null) {
            TAB.getInstance().getErrorManager().unknownProxyMessage(action);
         } else {
            ProxyMessage proxyMessage;
            try {
               proxyMessage = function.apply(in);
               TAB.getInstance().debug("[Proxy Support] Decoded message " + proxyMessage);
            } catch (Exception e) {
               TAB.getInstance().getErrorManager().printError("Failed to decode proxy message \"" + new String(Base64.getDecoder().decode(msg)) + "\" ", e);
               return;
            }

            TAB.getInstance()
               .getCpu()
               .runMeasuredTask(
                  this.getFeatureName(),
                  "Proxy Message processing",
                  () -> {
                     if (proxyMessage.getCustomThread() != null) {
                        proxyMessage.getCustomThread()
                           .execute(
                              new TimedCaughtTask(
                                 TAB.getInstance().getCpu(), () -> proxyMessage.process(this), this.getFeatureName(), "Proxy Message processing"
                              )
                           );
                     } else {
                        proxyMessage.process(this);
                     }
                  }
               );
         }
      }
   }

   public abstract void sendMessage(@NotNull String var1);

   public abstract void register();

   public abstract void unregister();

   @Override
   public void load() {
      this.register();

      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onJoin(p);
      }

      this.sendMessage(new LoadRequest());
   }

   @Override
   public void unload() {
      for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
         this.onQuit(p);
      }

      this.unregister();
   }

   @Override
   public void onJoin(@NotNull TabPlayer p) {
      this.sendMessage(new PlayerJoin(p));
   }

   @Override
   public void onServerChange(@NotNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
      this.sendMessage(new ServerSwitch(p.getUniqueId(), to));
   }

   @Override
   public void onQuit(@NotNull TabPlayer p) {
      this.sendMessage(new PlayerQuit(p.getUniqueId()));
   }

   public void sendMessage(@NotNull ProxyMessage message) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF(this.proxy.toString());
      out.writeUTF(this.classToString.get(message.getClass()));
      TAB.getInstance().debug("[Proxy Support] Encoding message " + message);
      message.write(out);
      this.sendMessage(Base64.getEncoder().encodeToString(out.toByteArray()));
   }

   public void registerMessage(@NotNull Class<? extends ProxyMessage> clazz, @NotNull Function<ByteArrayDataInput, ProxyMessage> function) {
      this.stringToClass.put(clazz.getSimpleName(), function);
      this.classToString.put(clazz, clazz.getSimpleName());
   }

   @Override
   public void onVanishStatusChange(@NotNull TabPlayer player) {
      this.sendMessage(new UpdateVanishStatus(player.getUniqueId(), player.isVanished()));
   }

   @NotNull
   @Generated
   public Map<UUID, ProxyPlayer> getProxyPlayers() {
      return this.proxyPlayers;
   }

   @NotNull
   @Generated
   public Map<UUID, QueuedData> getQueuedData() {
      return this.queuedData;
   }

   @NotNull
   @Generated
   public UUID getProxy() {
      return this.proxy;
   }

   @NotNull
   @Generated
   public Map<String, Function<ByteArrayDataInput, ProxyMessage>> getStringToClass() {
      return this.stringToClass;
   }

   @NotNull
   @Generated
   public Map<Class<? extends ProxyMessage>, String> getClassToString() {
      return this.classToString;
   }

   @Generated
   public AtomicLong getIdCounter() {
      return this.idCounter;
   }
}
