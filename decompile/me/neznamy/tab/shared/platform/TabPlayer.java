package me.neznamy.tab.shared.platform;

import java.nio.charset.StandardCharsets;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.integration.VanishIntegration;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.event.impl.PlayerLoadEventImpl;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.belowname.BelowNamePlayerData;
import me.neznamy.tab.shared.features.bossbar.BossBarPlayerData;
import me.neznamy.tab.shared.features.header.HeaderFooterPlayerData;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTagPlayerData;
import me.neznamy.tab.shared.features.playerlist.TablistFormattingPlayerData;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectivePlayerData;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardPlayerData;
import me.neznamy.tab.shared.features.sorting.SortingPlayerData;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.hook.FloodgateHook;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TabPlayer implements me.neznamy.tab.api.TabPlayer {
   protected final Platform platform;
   protected Object player;
   private final String name;
   private String nickname;
   private final UUID uniqueId;
   private final UUID tablistId;
   @NotNull
   public World world;
   @NotNull
   public Server server;
   private String permissionGroup = "NONE";
   private String temporaryGroup;
   private final boolean bedrockPlayer;
   private final int versionId;
   protected final ProtocolVersion version;
   private boolean loaded;
   private boolean online = true;
   public final SortingPlayerData sortingData = new SortingPlayerData();
   public final ScoreboardPlayerData scoreboardData = new ScoreboardPlayerData();
   public final NameTagPlayerData teamData = new NameTagPlayerData();
   public final LayoutManagerImpl.PlayerData layoutData = new LayoutManagerImpl.PlayerData();
   public final BossBarPlayerData bossbarData = new BossBarPlayerData();
   public final HeaderFooterPlayerData headerFooterData = new HeaderFooterPlayerData();
   public final PlayerListObjectivePlayerData playerlistObjectiveData = new PlayerListObjectivePlayerData();
   public final BelowNamePlayerData belowNameData = new BelowNamePlayerData();
   public final TablistFormattingPlayerData tablistData = new TablistFormattingPlayerData();
   public final Map<String, String> expansionValues = new HashMap<>();
   @Nullable
   public User luckPermsUser;
   public final Map<PlayerPlaceholder, String> lastPlaceholderValues = new ConcurrentHashMap<>();
   public final Map<RelationalPlaceholder, Map<TabPlayer, String>> lastRelationalValues = new ConcurrentHashMap<>();
   @NotNull
   private final Scoreboard scoreboard;
   @NotNull
   private final BossBar bossBar;
   @NotNull
   private final TabList tabList;

   protected TabPlayer(
      @NotNull Platform platform,
      @NotNull Object player,
      @NotNull UUID uniqueId,
      @NotNull String name,
      @NotNull String server,
      @NotNull String world,
      int protocolVersion,
      boolean useRealId
   ) {
      this.platform = platform;
      this.player = player;
      this.uniqueId = uniqueId;
      this.name = name;
      this.server = Server.byName(server);
      this.world = World.byName(world);
      this.nickname = name;
      this.versionId = protocolVersion;
      this.version = ProtocolVersion.fromNetworkId(protocolVersion);
      this.bedrockPlayer = FloodgateHook.getInstance().isFloodgatePlayer(uniqueId, name);
      this.permissionGroup = TAB.getInstance().getGroupManager().detectPermissionGroup(this);
      this.tablistId = useRealId ? uniqueId : UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      this.scoreboard = platform.createScoreboard(this);
      this.bossBar = platform.createBossBar(this);
      this.tabList = platform.createTabList(this);
   }

   public void markAsLoaded(boolean join) {
      this.loaded = true;
      if (TAB.getInstance().getEventBus() != null) {
         TAB.getInstance().getEventBus().fire(new PlayerLoadEventImpl(this, join));
      }
   }

   public void setGroup(@NotNull String permissionGroup) {
      if (!this.permissionGroup.equals(permissionGroup)) {
         this.permissionGroup = permissionGroup;
         ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%group%")).updateValue(this, this.getGroup());
         TAB.getInstance().getFeatureManager().onGroupChange(this);
      }
   }

   @Override
   public void setTemporaryGroup(@Nullable String group) {
      if (!Objects.equals(group, this.temporaryGroup)) {
         this.temporaryGroup = group;
         ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%group%")).updateValue(this, this.getGroup());
         TAB.getInstance().getFeatureManager().onGroupChange(this);
      }
   }

   @Override
   public boolean hasTemporaryGroup() {
      return this.temporaryGroup != null;
   }

   @Override
   public void setExpectedProfileName(@NonNull String profileName) {
      if (profileName == null) {
         throw new NullPointerException("profileName is marked non-null but is null");
      }

      if (!this.nickname.equals(profileName)) {
         TAB.getInstance()
            .debug("Changing expected profile name of player " + this.name + " from " + this.nickname + " to " + profileName + " as a result of an API call.");
         this.nickname = profileName;
         NickCompatibility nick = TAB.getInstance().getFeatureManager().getFeature("Nick");
         nick.processNameChange(this);
      }
   }

   @NotNull
   @Override
   public String getExpectedProfileName() {
      return this.nickname;
   }

   public void sendMessage(@NotNull String message) {
      if (!message.isEmpty()) {
         this.sendMessage(TabComponent.fromColoredText(message));
      }
   }

   @NotNull
   @Override
   public String getGroup() {
      return this.temporaryGroup != null ? this.temporaryGroup : this.permissionGroup;
   }

   public Property loadPropertyFromConfig(@Nullable RefreshableFeature feature, @NotNull String property, @NotNull String ifNotSet) {
      String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(this.name, property, this.server, this.world);
      if (value.length == 0) {
         value = TAB.getInstance().getConfiguration().getUsers().getProperty(this.uniqueId.toString(), property, this.server, this.world);
      }

      if (value.length == 0) {
         value = TAB.getInstance().getConfiguration().getGroups().getProperty(this.getGroup(), property, this.server, this.world);
      }

      return value.length > 0 ? new Property(property, feature, this, value[0], value[1]) : new Property(property, feature, this, ifNotSet, "None");
   }

   public boolean updatePropertyFromConfig(@NotNull Property property, @NotNull String ifNotSet) {
      String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(this.name, property.getName(), this.server, this.world);
      if (value.length == 0) {
         value = TAB.getInstance().getConfiguration().getUsers().getProperty(this.uniqueId.toString(), property.getName(), this.server, this.world);
      }

      if (value.length == 0) {
         value = TAB.getInstance().getConfiguration().getGroups().getProperty(this.getGroup(), property.getName(), this.server, this.world);
      }

      return value.length > 0 ? property.changeRawValue(value[0], value[1]) : property.changeRawValue(ifNotSet, "None");
   }

   public void ensureLoaded() {
      if (!this.loaded) {
         throw new IllegalStateException("This player is not loaded yet. Try again later");
      }
   }

   public void markOffline() {
      this.online = false;
   }

   public boolean canSee(@NotNull TabPlayer target) {
      if (target == this) {
         return true;
      }

      if (!VanishIntegration.getHandlers().isEmpty()) {
         try {
            for (VanishIntegration i : VanishIntegration.getHandlers()) {
               if (!i.canSee(this, target)) {
                  return false;
               }
            }
         } catch (ConcurrentModificationException e) {
            return this.canSee(target);
         }
      }

      return !target.isVanished() || this.hasPermission("tab.seevanished");
   }

   @NotNull
   @Override
   public String getServer() {
      return this.server.getName();
   }

   @NotNull
   @Override
   public String getWorld() {
      return this.world.getName();
   }

   public abstract boolean isDisguised();

   public abstract boolean hasInvisibilityPotion();

   public abstract boolean isVanished();

   public abstract int getGamemode();

   public abstract int getPing();

   public abstract void sendMessage(@NotNull TabComponent var1);

   public abstract boolean hasPermission(@NotNull String var1);

   public abstract Platform getPlatform();

   @Generated
   public void setPlayer(Object player) {
      this.player = player;
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @Generated
   public String getNickname() {
      return this.nickname;
   }

   @Generated
   public void setNickname(String nickname) {
      this.nickname = nickname;
   }

   @Generated
   @Override
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   public UUID getTablistId() {
      return this.tablistId;
   }

   @Generated
   public String getPermissionGroup() {
      return this.permissionGroup;
   }

   @Generated
   @Override
   public boolean isBedrockPlayer() {
      return this.bedrockPlayer;
   }

   @Generated
   public int getVersionId() {
      return this.versionId;
   }

   @Generated
   public ProtocolVersion getVersion() {
      return this.version;
   }

   @Generated
   @Override
   public boolean isLoaded() {
      return this.loaded;
   }

   @Generated
   public boolean isOnline() {
      return this.online;
   }

   @NotNull
   @Generated
   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   @NotNull
   @Generated
   public BossBar getBossBar() {
      return this.bossBar;
   }

   @NotNull
   @Generated
   public TabList getTabList() {
      return this.tabList;
   }
}
