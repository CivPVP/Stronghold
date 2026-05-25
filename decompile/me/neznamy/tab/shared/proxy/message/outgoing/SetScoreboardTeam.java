package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Collection;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class SetScoreboardTeam implements OutgoingMessage {
   private String name;
   private int action;
   private String prefix;
   private String suffix;
   private int options;
   private String visibility;
   private String collision;
   private int color;
   private Collection<String> players;

   public SetScoreboardTeam(String name) {
      this.name = name;
      this.action = 1;
   }

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("PacketPlayOutScoreboardTeam");
      out.writeUTF(this.name);
      out.writeInt(this.action);
      if (this.action == 0 || this.action == 2) {
         out.writeUTF(this.prefix);
         out.writeUTF(this.suffix);
         out.writeInt(this.options);
         out.writeUTF(this.visibility);
         out.writeUTF(this.collision);
         out.writeInt(this.color);
      }

      if (this.action == 0) {
         out.writeInt(this.players.size());

         for (String player : this.players) {
            out.writeUTF(player);
         }
      }

      return out;
   }

   @Generated
   public SetScoreboardTeam(
      String name, int action, String prefix, String suffix, int options, String visibility, String collision, int color, Collection<String> players
   ) {
      this.name = name;
      this.action = action;
      this.prefix = prefix;
      this.suffix = suffix;
      this.options = options;
      this.visibility = visibility;
      this.collision = collision;
      this.color = color;
      this.players = players;
   }
}
