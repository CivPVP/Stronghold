package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroupsCommand extends SubCommand {
   public GroupsCommand() {
      super("groups", "tab.grouplist");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      this.sendMessage(sender, "&3Configured groups:");
      this.sendMessage(sender, "&9" + String.join(", &9", TAB.getInstance().getConfiguration().getGroups().getAllEntries()));
   }
}
