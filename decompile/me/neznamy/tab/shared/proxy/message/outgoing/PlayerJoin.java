package me.neznamy.tab.shared.proxy.message.outgoing;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import org.jetbrains.annotations.NotNull;

public class PlayerJoin implements OutgoingMessage {
   private boolean forwardGroup;
   private Map<String, Integer> placeholders;
   private Map<String, Map<Object, Object>> replacements;

   @NotNull
   @Override
   public ByteArrayDataOutput write() {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("PlayerJoin");
      out.writeInt(0);
      out.writeBoolean(this.forwardGroup);
      out.writeInt(this.placeholders.size());

      for (Entry<String, Integer> entry : this.placeholders.entrySet()) {
         out.writeUTF(entry.getKey());
         out.writeInt(entry.getValue());
      }

      out.writeInt(this.replacements.size());

      for (Entry<String, Map<Object, Object>> entry : this.replacements.entrySet()) {
         out.writeUTF(entry.getKey());
         out.writeInt(entry.getValue().size());

         for (Entry<Object, Object> rule : entry.getValue().entrySet()) {
            out.writeUTF(EnumChatFormat.color(String.valueOf(rule.getKey())));
            out.writeUTF(EnumChatFormat.color(String.valueOf(rule.getValue())));
         }
      }

      out.writeBoolean(false);
      return out;
   }

   @Generated
   public PlayerJoin(boolean forwardGroup, Map<String, Integer> placeholders, Map<String, Map<Object, Object>> replacements) {
      this.forwardGroup = forwardGroup;
      this.placeholders = placeholders;
      this.replacements = replacements;
   }
}
