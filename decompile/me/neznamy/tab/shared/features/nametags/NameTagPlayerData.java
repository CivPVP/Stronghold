package me.neznamy.tab.shared.features.nametags;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameTagPlayerData {
   public String teamName;
   public Property prefix;
   public Property suffix;
   public final AtomicBoolean disabled = new AtomicBoolean();
   public boolean teamHandlingPaused;
   public boolean invisibleNameTagView;
   public final Set<UUID> vanishedFor = new HashSet<>();
   public boolean collisionRule;
   @Nullable
   public Boolean forcedCollision;
   @NotNull
   private final EnumSet<NameTagInvisibilityReason> nameTagInvisibilityReasons = EnumSet.noneOf(NameTagInvisibilityReason.class);
   @NotNull
   private final Map<TabPlayer, EnumSet<NameTagInvisibilityReason>> nameTagInvisibilityReasonsRelational = new WeakHashMap<>();

   public boolean getCollisionRule() {
      return this.forcedCollision != null ? this.forcedCollision : this.collisionRule;
   }

   public boolean isDisabled() {
      return this.disabled.get() || this.teamHandlingPaused;
   }

   public boolean hideNametag(@NotNull NameTagInvisibilityReason reason) {
      return this.nameTagInvisibilityReasons.add(reason);
   }

   public boolean showNametag(@NotNull NameTagInvisibilityReason reason) {
      return this.nameTagInvisibilityReasons.remove(reason);
   }

   public boolean hasHiddenNametag(@NotNull NameTagInvisibilityReason reason) {
      return this.nameTagInvisibilityReasons.contains(reason);
   }

   public boolean hasHiddenNametag() {
      return !this.nameTagInvisibilityReasons.isEmpty();
   }

   public boolean hideNametag(@NotNull TabPlayer viewer, @NotNull NameTagInvisibilityReason reason) {
      return this.nameTagInvisibilityReasonsRelational.computeIfAbsent(viewer, v -> EnumSet.noneOf(NameTagInvisibilityReason.class)).add(reason);
   }

   public boolean showNametag(@NotNull TabPlayer viewer, @NotNull NameTagInvisibilityReason reason) {
      return this.nameTagInvisibilityReasonsRelational.computeIfAbsent(viewer, v -> EnumSet.noneOf(NameTagInvisibilityReason.class)).remove(reason);
   }

   public boolean hasHiddenNametag(@NotNull TabPlayer viewer, @NotNull NameTagInvisibilityReason reason) {
      return !this.nameTagInvisibilityReasonsRelational.containsKey(viewer) ? false : this.nameTagInvisibilityReasonsRelational.get(viewer).contains(reason);
   }

   public boolean hasHiddenNametag(@NotNull TabPlayer viewer) {
      return !this.nameTagInvisibilityReasonsRelational.containsKey(viewer) ? false : !this.nameTagInvisibilityReasonsRelational.get(viewer).isEmpty();
   }
}
