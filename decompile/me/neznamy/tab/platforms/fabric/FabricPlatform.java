package me.neznamy.tab.platforms.fabric;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.NonNull;
import me.neznamy.tab.platforms.fabric.hook.FabricTabExpansion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_11723;
import net.minecraft.class_11885;
import net.minecraft.class_155;
import net.minecraft.class_2168;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_9296;
import net.minecraft.class_11719.class_11721;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FabricPlatform(MinecraftServer server) implements BackendPlatform {
   private static final UUID NIL_UUID = new UUID(0L, 0L);

   @Override
   public void registerUnknownPlaceholder(@NotNull String identifier) {
      if (!FabricLoader.getInstance().isModLoaded("placeholder-api")) {
         this.registerDummyPlaceholder(identifier);
      } else {
         PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
         manager.registerPlayerPlaceholder(
            identifier, p -> Placeholders.parseText(class_2561.method_43470(identifier), PlaceholderContext.of((class_3222)p.getPlayer())).getString()
         );
      }
   }

   @Override
   public void loadPlayers() {
      for (class_3222 player : this.getOnlinePlayers()) {
         TAB.getInstance().addPlayer(new FabricTabPlayer(this, player));
      }
   }

   private Collection<class_3222> getOnlinePlayers() {
      return this.server.method_3760() == null ? Collections.emptyList() : this.server.method_3760().method_14571();
   }

   @NotNull
   @Override
   public PipelineInjector createPipelineInjector() {
      return new FabricPipelineInjector();
   }

   @NotNull
   @Override
   public TabExpansion createTabExpansion() {
      return FabricLoader.getInstance().isModLoaded("placeholder-api") ? new FabricTabExpansion() : new EmptyTabExpansion();
   }

   @Nullable
   @Override
   public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
      return null;
   }

   @Override
   public void logInfo(@NotNull TabComponent message) {
      LogUtils.getLogger().info("[TAB] {}", message.<class_2561>convert().getString());
   }

   @Override
   public void logWarn(@NotNull TabComponent message) {
      LogUtils.getLogger().warn("[TAB] {}", message.<class_2561>convert().getString());
   }

   @NotNull
   @Override
   public String getServerVersionInfo() {
      return "[Fabric] " + class_155.method_16673().comp_4025();
   }

   @Override
   public void registerListener() {
      new FabricEventListener().register();
   }

   @Override
   public void registerCommand() {
      FabricTAB.COMMAND_DISPATCHER.getRoot().addChild(new FabricTabCommand(this.getCommand()).getCommand());
   }

   @Override
   public void startMetrics() {
   }

   @NotNull
   @Override
   public File getDataFolder() {
      return FabricLoader.getInstance().getConfigDir().resolve("tab").toFile();
   }

   @NotNull
   public class_2561 convertComponent(@NotNull TabComponent component) {
      class_5250 nmsComponent = switch (component) {
         case TabTextComponent text -> class_2561.method_43470(text.getText());
         case TabTranslatableComponent translatable -> class_2561.method_43471(translatable.getKey());
         case TabKeybindComponent keybind -> class_2561.method_43472(keybind.getKeybind());
         case TabObjectComponent object -> {
            switch (object.getContents()) {
               case TabAtlasSprite sprite:
                  yield class_2561.method_74062(new class_11723(class_2960.method_60654(sprite.getAtlas()), class_2960.method_60654(sprite.getSprite())));
               case TabPlayerSprite sprite:
                  yield class_2561.method_74062(new class_11885(this.spriteToProfile(sprite), sprite.isShowHat()));
               default:
                  throw new IllegalStateException("Unexpected object component type: " + object.getContents().getClass().getName());
            }
         }
         default -> throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
      };
      TabStyle modifier = component.getModifier();
      class_2583 style = class_2583.field_24360
         .method_27703(modifier.getColor() == null ? null : class_5251.method_27717(modifier.getColor().getRgb()))
         .method_10982(modifier.getBold())
         .method_10978(modifier.getItalic())
         .method_30938(modifier.getUnderlined())
         .method_36140(modifier.getStrikethrough())
         .method_36141(modifier.getObfuscated())
         .method_27704(modifier.getFont() == null ? null : new class_11721(class_2960.method_60654(modifier.getFont())));
      if (modifier.getShadowColor() != null) {
         style = style.method_65302(modifier.getShadowColor());
      }

      nmsComponent.method_10862(style);

      for (TabComponent extra : component.getExtra()) {
         nmsComponent.method_10855().add(this.convertComponent(extra));
      }

      return nmsComponent;
   }

   @NotNull
   private class_9296 spriteToProfile(@NonNull TabPlayerSprite sprite) {
      if (sprite == null) {
         throw new NullPointerException("sprite is marked non-null but is null");
      } else if (sprite.getId() != null) {
         return class_9296.method_73312(sprite.getId());
      } else if (sprite.getName() != null) {
         return class_9296.method_74889(sprite.getName());
      } else if (sprite.getSkin() != null) {
         Builder<String, Property> builder = ImmutableMultimap.builder();
         builder.put("textures", new Property("textures", sprite.getSkin().getValue(), sprite.getSkin().getSignature()));
         return class_9296.method_73307(new GameProfile(NIL_UUID, "", new PropertyMap(builder.build())));
      } else {
         throw new IllegalStateException("Player head component does not have id, name or skin set");
      }
   }

   @NotNull
   @Override
   public Scoreboard createScoreboard(@NotNull TabPlayer player) {
      return new FabricScoreboard((FabricTabPlayer)player);
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NotNull TabPlayer player) {
      return new FabricBossBar((FabricTabPlayer)player);
   }

   @NotNull
   @Override
   public TabList createTabList(@NotNull TabPlayer player) {
      return new FabricTabList((FabricTabPlayer)player);
   }

   @Override
   public boolean supportsScoreboards() {
      return true;
   }

   @Override
   public void registerCustomCommand(@NotNull String commandName, @NotNull Consumer<TabPlayer> function) {
      FabricCommand command = new FabricCommand(commandName) {
         @Override
         public int execute(@NotNull class_2168 source, @NotNull String[] args) {
            if (source.method_9228() != null) {
               TabPlayer p = TAB.getInstance().getPlayer(source.method_9228().method_5667());
               if (p == null) {
                  return 0;
               }

               function.accept(p);
               return 0;
            } else {
               source.method_45068(TabComponent.fromColoredText(TAB.getInstance().getConfiguration().getMessages().getCommandOnlyFromGame()).convert());
               return 0;
            }
         }
      };
      FabricTAB.COMMAND_DISPATCHER.getRoot().addChild(command.getCommand());
   }

   @Override
   public void unregisterAllCustomCommands() {
   }

   @Override
   public double getTPS() {
      double mspt = this.getMSPT();
      return mspt < 50.0 ? 20.0 : Math.round(1000.0 / mspt);
   }

   @Override
   public double getMSPT() {
      return (float)this.server.method_54834() / 1000000.0F;
   }
}
