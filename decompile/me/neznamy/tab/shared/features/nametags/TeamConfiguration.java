package me.neznamy.tab.shared.features.nametags;

import java.util.Arrays;
import lombok.Generated;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class TeamConfiguration {
   @NotNull
   private final String enableCollision;
   @NotNull
   private final String invisibleNameTags;
   private final boolean canSeeFriendlyInvisibles;
   @NotNull
   private final String disableCondition;

   @NotNull
   public static TeamConfiguration fromSection(@NotNull ConfigurationSection section) {
      section.checkForUnknownKey(
         Arrays.asList(
            "enabled", "enable-collision", "invisible-nametags", "sorting-types", "case-sensitive-sorting", "can-see-friendly-invisibles", "disable-condition"
         )
      );
      return new TeamConfiguration(
         section.getObject("enable-collision", "true").toString(),
         section.getObject("invisible-nametags", "false").toString(),
         section.getBoolean("can-see-friendly-invisibles", false),
         section.getString("disable-condition", "%world%=disabledworld")
      );
   }

   @NotNull
   @Generated
   public String getEnableCollision() {
      return this.enableCollision;
   }

   @NotNull
   @Generated
   public String getInvisibleNameTags() {
      return this.invisibleNameTags;
   }

   @Generated
   public boolean isCanSeeFriendlyInvisibles() {
      return this.canSeeFriendlyInvisibles;
   }

   @NotNull
   @Generated
   public String getDisableCondition() {
      return this.disableCondition;
   }

   @Generated
   public TeamConfiguration(
      @NotNull String enableCollision, @NotNull String invisibleNameTags, boolean canSeeFriendlyInvisibles, @NotNull String disableCondition
   ) {
      if (enableCollision == null) {
         throw new NullPointerException("enableCollision is marked non-null but is null");
      }

      if (invisibleNameTags == null) {
         throw new NullPointerException("invisibleNameTags is marked non-null but is null");
      }

      if (disableCondition == null) {
         throw new NullPointerException("disableCondition is marked non-null but is null");
      }

      this.enableCollision = enableCollision;
      this.invisibleNameTags = invisibleNameTags;
      this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
      this.disableCondition = disableCondition;
   }
}
