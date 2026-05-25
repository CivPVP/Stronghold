package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class Load extends ProxyMessage {
   @NotNull
   private final List<PlayerJoin> decodedPlayers;

   public Load(@NotNull TabPlayer[] players) {
      this.decodedPlayers = Arrays.stream(players).map(PlayerJoin::new).collect(Collectors.toList());
   }

   public Load(@NotNull ByteArrayDataInput in) {
      this.decodedPlayers = new ArrayList<>();
      int count = in.readInt();

      for (int i = 0; i < count; i++) {
         this.decodedPlayers.add(new PlayerJoin(in));
      }
   }

   @Override
   public void write(@NotNull ByteArrayDataOutput out) {
      out.writeInt(this.decodedPlayers.size());

      for (PlayerJoin player : this.decodedPlayers) {
         player.write(out);
      }
   }

   @Override
   public void process(@NotNull ProxySupport proxySupport) {
      for (PlayerJoin join : this.decodedPlayers) {
         join.process(proxySupport);
      }
   }

   @Generated
   @Override
   public String toString() {
      return "Load(decodedPlayers=" + this.decodedPlayers + ")";
   }
}
