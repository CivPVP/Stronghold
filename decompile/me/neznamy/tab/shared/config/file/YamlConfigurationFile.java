package me.neznamy.tab.shared.config.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.NonNull;
import me.neznamy.tab.libs.me.neznamy.yamlassist.YamlAssist;
import me.neznamy.tab.libs.org.yaml.snakeyaml.DumperOptions;
import me.neznamy.tab.libs.org.yaml.snakeyaml.LoaderOptions;
import me.neznamy.tab.libs.org.yaml.snakeyaml.Yaml;
import me.neznamy.tab.libs.org.yaml.snakeyaml.error.YAMLException;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import org.jetbrains.annotations.Nullable;

public class YamlConfigurationFile extends ConfigurationFile {
   public YamlConfigurationFile(@Nullable InputStream source, @NonNull File destination) throws IOException {
      super(source, destination);
      if (destination == null) {
         throw new NullPointerException("destination is marked non-null but is null");
      }

      FileInputStream input = null;

      try {
         input = new FileInputStream(this.file);
         LoaderOptions loaderOptions = new LoaderOptions();
         loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
         Yaml yaml = new Yaml(loaderOptions);
         this.values = yaml.load(input);
         if (this.values == null) {
            this.values = new LinkedHashMap<>();
         }

         input.close();
      } catch (YAMLException e) {
         if (input != null) {
            input.close();
         }

         TAB tab = TAB.getInstance();
         tab.setBrokenFile(destination.getName());
         tab.getPlatform().logWarn(new TabTextComponent("File " + destination + " has broken syntax.", TabTextColor.RED));
         tab.getPlatform().logInfo(new TabTextComponent("Error message from yaml parser: " + e.getMessage(), TabTextColor.GOLD));
         List<String> suggestions = YamlAssist.getSuggestions(this.file);
         if (!suggestions.isEmpty()) {
            tab.getPlatform().logInfo(new TabTextComponent("Suggestions to fix yaml syntax:", TabTextColor.LIGHT_PURPLE));

            for (String suggestion : suggestions) {
               tab.getPlatform().logInfo(new TabTextComponent("- " + suggestion, TabTextColor.LIGHT_PURPLE));
            }
         }

         throw e;
      }
   }

   @Override
   public synchronized void save() {
      try {
         Writer writer = new OutputStreamWriter(Files.newOutputStream(this.file.toPath()), StandardCharsets.UTF_8);
         DumperOptions options = new DumperOptions();
         options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
         new Yaml(options).dump(this.values, writer);
         writer.close();
      } catch (IOException e) {
         TAB.getInstance()
            .getPlatform()
            .logWarn(
               new TabTextComponent(
                  String.format("Failed to save yaml file %s: %s: %s", this.file.getPath(), e.getClass().getName(), e.getMessage()), TabTextColor.RED
               )
            );
      }
   }
}
