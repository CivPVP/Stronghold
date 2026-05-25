package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class SetObjective implements OutgoingMessage {
   private String objectiveName;
   private int action;
   private String title;
   private int display;
   private String numberFormat;

   public SetObjective(String objectiveName) {
      this.objectiveName = objectiveName;
      this.action = 1;
   }

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("PacketPlayOutScoreboardObjective");
      out.writeUTF(this.objectiveName);
      out.writeInt(this.action);
      if (this.action == 0 || this.action == 2) {
         out.writeUTF(this.title);
         out.writeInt(this.display);
         out.writeBoolean(this.numberFormat != null);
         if (this.numberFormat != null) {
            out.writeUTF(this.numberFormat);
         }
      }

      return out;
   }

   @Generated
   public SetObjective(String objectiveName, int action, String title, int display, String numberFormat) {
      this.objectiveName = objectiveName;
      this.action = action;
      this.title = title;
      this.display = display;
      this.numberFormat = numberFormat;
   }
}
