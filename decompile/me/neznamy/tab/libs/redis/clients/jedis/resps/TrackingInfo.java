package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.Collections;
import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class TrackingInfo {
   private final List<String> flags;
   private final long redirect;
   private final List<String> prefixes;
   public static final Builder<TrackingInfo> TRACKING_INFO_BUILDER = new Builder<TrackingInfo>() {
      public TrackingInfo build(Object data) {
         List commandData = (List)data;
         if (commandData.get(0) instanceof KeyValue) {
            List<String> flags = Collections.emptyList();
            long redirect = -1L;
            List<String> prefixes = Collections.emptyList();

            for (KeyValue kv : commandData) {
               switch ((String)BuilderFactory.STRING.build(kv.getKey())) {
                  case "flags":
                     flags = BuilderFactory.STRING_LIST.build(kv.getValue());
                     break;
                  case "redirect":
                     redirect = BuilderFactory.LONG.build(kv.getValue());
                     break;
                  case "prefixes":
                     prefixes = BuilderFactory.STRING_LIST.build(kv.getValue());
               }
            }

            return new TrackingInfo(flags, redirect, prefixes);
         } else {
            List<String> flags = BuilderFactory.STRING_LIST.build(commandData.get(1));
            long redirect = BuilderFactory.LONG.build(commandData.get(3));
            List<String> prefixes = BuilderFactory.STRING_LIST.build(commandData.get(5));
            return new TrackingInfo(flags, redirect, prefixes);
         }
      }
   };

   public TrackingInfo(List<String> flags, long redirect, List<String> prefixes) {
      this.flags = flags;
      this.redirect = redirect;
      this.prefixes = prefixes;
   }

   public List<String> getFlags() {
      return this.flags;
   }

   public long getRedirect() {
      return this.redirect;
   }

   public List<String> getPrefixes() {
      return this.prefixes;
   }
}
