package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReloadCommand extends SubCommand {
   public ReloadCommand() {
      super("reload", "tab.reload");
   }

   @Override
   public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
      TAB.getInstance().unload();
      this.sendMessage(sender, TAB.getInstance().load());
   }
}
