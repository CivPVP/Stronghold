package me.neznamy.tab.platforms.bukkit.features;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitTabExpansion extends PlaceholderExpansion implements TabExpansion {
   @NotNull
   private final List<String> placeholders = Collections.unmodifiableList(
      Arrays.asList(
         "%tab_tabprefix%",
         "%tab_tabsuffix%",
         "%tab_tagprefix%",
         "%tab_tagsuffix%",
         "%tab_customtabname%",
         "%tab_tabprefix_raw%",
         "%tab_tabsuffix_raw%",
         "%tab_tagprefix_raw%",
         "%tab_tagsuffix_raw%",
         "%tab_customtabname_raw%",
         "%tab_scoreboard_name%",
         "%tab_scoreboard_visible%",
         "%tab_bossbar_visible%",
         "%tab_nametag_visibility%",
         "%tab_replace_<placeholder>%",
         "%tab_placeholder_<placeholder>%"
      )
   );

   public boolean persist() {
      return true;
   }

   @NotNull
   public String getAuthor() {
      return "NEZNAMY";
   }

   @NotNull
   public String getIdentifier() {
      return "tab";
   }

   @NotNull
   public String getVersion() {
      return "5.4.0";
   }

   @NotNull
   public List<String> getPlaceholders() {
      return this.placeholders;
   }

   @Nullable
   public String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
      if (!identifier.startsWith("replace_")) {
         if (player == null) {
            return "<Player cannot be null>";
         } else {
            TabPlayer p = TAB.getInstance().getPlayer(player.getUniqueId());
            if (p == null) {
               return "<Player is not loaded>";
            } else if (identifier.startsWith("placeholder_")) {
               String requestedPlaceholder = "%" + identifier.substring("placeholder_".length()) + "%";
               PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
               pm.addUsedPlaceholder(requestedPlaceholder, pm);
               return pm.getPlaceholder(requestedPlaceholder).getLastValue(p);
            } else {
               return p.expansionValues.get(identifier);
            }
         }
      } else {
         String text = "%" + identifier.substring(8) + "%";

         String textBefore;
         do {
            textBefore = text;

            for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(text)) {
               text = text.replace(
                  placeholder, TAB.getInstance().getPlaceholderManager().findReplacement(placeholder, PlaceholderAPI.setPlaceholders(player, placeholder))
               );
            }
         } while (!textBefore.equals(text));

         return text;
      }
   }

   @Override
   public void setPlaceholderValue(@NotNull TabPlayer player, @NotNull String placeholder, @NotNull String value) {
   }

   @Override
   public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
      player.expansionValues.put(key, value);
   }

   @Override
   public void unregisterExpansion() {
      if (ReflectionUtils.methodExists(PlaceholderExpansion.class, "unregister")) {
         this.unregister();
      } else {
         PlaceholderAPI.unregisterExpansion(this);
      }
   }
}
