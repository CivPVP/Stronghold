package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class SetScore implements OutgoingMessage {
   private String objective;
   private int action;
   private String scoreHolder;
   private int score;
   private String displayName;
   private String numberFormat;

   public SetScore(String objective, String scoreHolder) {
      this.objective = objective;
      this.scoreHolder = scoreHolder;
      this.action = 1;
   }

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("PacketPlayOutScoreboardScore");
      out.writeUTF(this.objective);
      out.writeInt(this.action);
      out.writeUTF(this.scoreHolder);
      if (this.action == 0) {
         out.writeInt(this.score);
         out.writeBoolean(this.displayName != null);
         if (this.displayName != null) {
            out.writeUTF(this.displayName);
         }

         out.writeBoolean(this.numberFormat != null);
         if (this.numberFormat != null) {
            out.writeUTF(this.numberFormat);
         }
      }

      return out;
   }

   @Generated
   public SetScore(String objective, int action, String scoreHolder, int score, String displayName, String numberFormat) {
      this.objective = objective;
      this.action = action;
      this.scoreHolder = scoreHolder;
      this.score = score;
      this.displayName = displayName;
      this.numberFormat = numberFormat;
   }
}
