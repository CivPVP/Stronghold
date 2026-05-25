package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Generated;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

public class SetDisplayObjective implements OutgoingMessage {
   private Scoreboard.DisplaySlot slot;
   private String objective;

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("PacketPlayOutScoreboardDisplayObjective");
      out.writeInt(this.slot.ordinal());
      out.writeUTF(this.objective);
      return out;
   }

   @Generated
   public SetDisplayObjective(Scoreboard.DisplaySlot slot, String objective) {
      this.slot = slot;
      this.objective = objective;
   }
}
