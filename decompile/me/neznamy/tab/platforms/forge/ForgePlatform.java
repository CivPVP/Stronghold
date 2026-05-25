package me.neznamy.tab.platforms.forge;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.NonNull;
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
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ForgePlatform(MinecraftServer server) implements BackendPlatform {
   private static final UUID NIL_UUID = new UUID(0L, 0L);

   @Override
   public void registerUnknownPlaceholder(@NotNull String identifier) {
      this.registerDummyPlaceholder(identifier);
   }

   @Override
   public void loadPlayers() {
      for (ServerPlayer player : this.getOnlinePlayers()) {
         TAB.getInstance().addPlayer(new ForgeTabPlayer(this, player));
      }
   }

   private Collection<ServerPlayer> getOnlinePlayers() {
      return this.server.getPlayerList() == null ? Collections.emptyList() : this.server.getPlayerList().getPlayers();
   }

   @NotNull
   @Override
   public PipelineInjector createPipelineInjector() {
      return new ForgePipelineInjector();
   }

   @Nullable
   @Override
   public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
      return null;
   }

   @Override
   public void logInfo(@NotNull TabComponent message) {
      LogUtils.getLogger().info("[TAB] {}", message.<Component>convert().getString());
   }

   @Override
   public void logWarn(@NotNull TabComponent message) {
      LogUtils.getLogger().warn("[TAB] {}", message.<Component>convert().getString());
   }

   @NotNull
   @Override
   public String getServerVersionInfo() {
      return "[Forge] " + SharedConstants.getCurrentVersion().name();
   }

   @Override
   public void registerListener() {
      new ForgeEventListener().register();
   }

   @Override
   public void registerCommand() {
      ForgeTAB.COMMAND_DISPATCHER.getRoot().addChild(new ForgeTabCommand(this.getCommand()).getCommand());
   }

   @Override
   public void startMetrics() {
   }

   @NotNull
   @Override
   public File getDataFolder() {
      return FMLPaths.CONFIGDIR.get().resolve("tab").toFile();
   }

   @NotNull
   public Component convertComponent(@NotNull TabComponent component) {
      MutableComponent nmsComponent = switch (component) {
         case TabTextComponent text -> Component.literal(text.getText());
         case TabTranslatableComponent translatable -> Component.translatable(translatable.getKey());
         case TabKeybindComponent keybind -> Component.keybind(keybind.getKeybind());
         case TabObjectComponent object -> {
            switch (object.getContents()) {
               case TabAtlasSprite sprite:
                  yield Component.object(new AtlasSprite(ResourceLocation.parse(sprite.getAtlas()), ResourceLocation.parse(sprite.getSprite())));
               case TabPlayerSprite sprite:
                  yield Component.object(new PlayerSprite(this.spriteToProfile(sprite), sprite.isShowHat()));
               default:
                  throw new IllegalStateException("Unexpected object component type: " + object.getContents().getClass().getName());
            }
         }
         default -> throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
      };
      TabStyle modifier = component.getModifier();
      Style style = Style.EMPTY
         .withColor(modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()))
         .withBold(modifier.getBold())
         .withItalic(modifier.getItalic())
         .withUnderlined(modifier.getUnderlined())
         .withStrikethrough(modifier.getStrikethrough())
         .withObfuscated(modifier.getObfuscated())
         .withFont(modifier.getFont() == null ? null : new Resource(ResourceLocation.parse(modifier.getFont())));
      if (modifier.getShadowColor() != null) {
         style = style.withShadowColor(modifier.getShadowColor());
      }

      nmsComponent.setStyle(style);

      for (TabComponent extra : component.getExtra()) {
         nmsComponent.getSiblings().add(this.convertComponent(extra));
      }

      return nmsComponent;
   }

   @NotNull
   private ResolvableProfile spriteToProfile(@NonNull TabPlayerSprite sprite) {
      if (sprite == null) {
         throw new NullPointerException("sprite is marked non-null but is null");
      } else if (sprite.getId() != null) {
         return ResolvableProfile.createUnresolved(sprite.getId());
      } else if (sprite.getName() != null) {
         return ResolvableProfile.createUnresolved(sprite.getName());
      } else if (sprite.getSkin() != null) {
         Builder<String, Property> builder = ImmutableMultimap.builder();
         builder.put("textures", new Property("textures", sprite.getSkin().getValue(), sprite.getSkin().getSignature()));
         return ResolvableProfile.createResolved(new GameProfile(NIL_UUID, "", new PropertyMap(builder.build())));
      } else {
         throw new IllegalStateException("Player head component does not have id, name or skin set");
      }
   }

   @NotNull
   @Override
   public Scoreboard createScoreboard(@NotNull TabPlayer player) {
      return new ForgeScoreboard((ForgeTabPlayer)player);
   }

   @NotNull
   @Override
   public BossBar createBossBar(@NotNull TabPlayer player) {
      return new ForgeBossBar((ForgeTabPlayer)player);
   }

   @NotNull
   @Override
   public TabList createTabList(@NotNull TabPlayer player) {
      return new ForgeTabList((ForgeTabPlayer)player);
   }

   @Override
   public boolean supportsScoreboards() {
      return true;
   }

   @Override
   public void registerCustomCommand(@NotNull String commandName, @NotNull Consumer<TabPlayer> function) {
      ForgeCommand command = new ForgeCommand(commandName) {
         @Override
         public int execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
            if (source.getEntity() != null) {
               TabPlayer p = TAB.getInstance().getPlayer(source.getEntity().getUUID());
               if (p == null) {
                  return 0;
               }

               function.accept(p);
               return 0;
            } else {
               source.sendSystemMessage(TabComponent.fromColoredText(TAB.getInstance().getConfiguration().getMessages().getCommandOnlyFromGame()).convert());
               return 0;
            }
         }
      };
      ForgeTAB.COMMAND_DISPATCHER.getRoot().addChild(command.getCommand());
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
      return (float)this.server.getAverageTickTimeNanos() / 1000000.0F;
   }
}
