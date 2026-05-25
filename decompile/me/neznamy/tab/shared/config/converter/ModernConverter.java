package me.neznamy.tab.shared.config.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.placeholders.conditions.ConditionsSection;

public class ModernConverter {
   private final Map<Integer, Consumer<ConfigurationFile>> converters = new HashMap<>();

   public ModernConverter() {
      this.converters.put(0, config -> {
         Map<String, Object> components = new LinkedHashMap<>();
         components.put("minimessage-support", true);
         components.put("disable-shadow-for-heads", true);
         config.set("components", components);
      });
      this.converters
         .put(
            1,
            config -> {
               TAB.getInstance()
                  .getPlatform()
                  .logWarn(
                     new TabTextComponent(
                        "Please note that header/footer conversion may not be 100% accurate and will not convert per-group settings and per-user settings. Review your config to make sure it is set up the way you want.",
                        TabTextColor.RED
                     )
                  );
               ConfigurationSection headerFooter = config.getConfigurationSection("header-footer");
               boolean enabled = headerFooter.getBoolean("enabled", true);
               List<String> defaultHeader = headerFooter.getStringList("header", new ArrayList<>());
               List<String> defaultFooter = headerFooter.getStringList("footer", new ArrayList<>());
               String disableCondition = headerFooter.getString("disable-condition", "%world%=disabledworld");
               ConfigurationSection perWorld = headerFooter.getConfigurationSection("per-world");
               ConfigurationSection perServer = headerFooter.getConfigurationSection("per-server");
               Map<String, Object> designs = new LinkedHashMap<>();

               for (Object world : perWorld.getKeys()) {
                  ConfigurationSection worldSection = perWorld.getConfigurationSection(world.toString());
                  Map<String, Object> design = new LinkedHashMap<>();
                  design.put("display-condition", Arrays.stream(world.toString().split(";")).map(part -> {
                     if (part.endsWith("*")) {
                        return "%world%|-" + part.substring(0, part.length() - 1);
                     } else {
                        return part.startsWith("*") ? "%world%-|" + part.substring(1) : "%world%=" + part;
                     }
                  }).collect(Collectors.joining("|")));
                  List<String> header = worldSection.getStringList("header");
                  if (header == null) {
                     header = new ArrayList<>();
                  }

                  List<String> footer = worldSection.getStringList("footer");
                  if (footer == null) {
                     footer = new ArrayList<>();
                  }

                  design.put("header", header);
                  design.put("footer", footer);
                  designs.put("world-" + world, design);
               }

               for (Object server : perServer.getKeys()) {
                  ConfigurationSection serverSection = perServer.getConfigurationSection(server.toString());
                  Map<String, Object> design = new LinkedHashMap<>();
                  design.put("display-condition", Arrays.stream(server.toString().split(";")).map(part -> {
                     if (part.endsWith("*")) {
                        return "%server%|-" + part.substring(0, part.length() - 1);
                     } else {
                        return part.startsWith("*") ? "%server%-|" + part.substring(1) : "%server%=" + part;
                     }
                  }).collect(Collectors.joining("|")));
                  List<String> header = serverSection.getStringList("header");
                  if (header == null) {
                     header = new ArrayList<>();
                  }

                  List<String> footer = serverSection.getStringList("footer");
                  if (footer == null) {
                     footer = new ArrayList<>();
                  }

                  design.put("header", header);
                  design.put("footer", footer);
                  designs.put("server-" + server, design);
               }

               Map<String, Object> defaultDesign = new LinkedHashMap<>();
               if (!disableCondition.isEmpty()) {
                  ConditionsSection conditions = ConditionsSection.fromSection(config.getConfigurationSection("conditions"));
                  ConditionsSection.ConditionDefinition namedCondition = conditions.getConditions().get(disableCondition);
                  if (namedCondition != null) {
                     defaultDesign.put("display-condition", "%condition:" + namedCondition.getName() + "%=" + namedCondition.getNo());
                  } else {
                     defaultDesign.put("display-condition", new Condition(disableCondition).invert().toShortFormat());
                  }
               }

               defaultDesign.put("header", defaultHeader);
               defaultDesign.put("footer", defaultFooter);
               designs.put("default", defaultDesign);
               Map<String, Object> newHeaderFooter = new LinkedHashMap<>();
               newHeaderFooter.put("enabled", enabled);
               newHeaderFooter.put("designs", designs);
               config.set("header-footer", newHeaderFooter);
            }
         );
   }

   public void convert(@NonNull ConfigurationFile config) {
      if (config == null) {
         throw new NullPointerException("config is marked non-null but is null");
      }

      int configVersion = config.getInt("config-version", 0);

      while (this.converters.containsKey(configVersion)) {
         TAB.getInstance()
            .getPlatform()
            .logInfo(
               new TabTextComponent(
                  "Performing configuration conversion from config version " + configVersion + " to " + (configVersion + 1), TabTextColor.YELLOW
               )
            );
         this.converters.get(configVersion).accept(config);
         config.set("config-version", ++configVersion);
      }
   }
}
