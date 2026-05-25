package me.neznamy.tab.shared.config.files;

import java.io.File;
import java.io.IOException;
import lombok.Generated;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.converter.LegacyConverter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.placeholders.animation.AnimationConfiguration;
import org.jetbrains.annotations.NotNull;

public class Animations {
   private final ConfigurationFile animationFile = new YamlConfigurationFile(
      this.getClass().getClassLoader().getResourceAsStream("config/animations.yml"), new File(TAB.getInstance().getDataFolder(), "animations.yml")
   );
   @NotNull
   private final AnimationConfiguration animations;

   public Animations() throws IOException {
      new LegacyConverter().convert2810to290(this.animationFile);
      this.animations = AnimationConfiguration.fromSection(this.animationFile.getConfigurationSection(""));
   }

   @Generated
   public ConfigurationFile getAnimationFile() {
      return this.animationFile;
   }

   @NotNull
   @Generated
   public AnimationConfiguration getAnimations() {
      return this.animations;
   }
}
