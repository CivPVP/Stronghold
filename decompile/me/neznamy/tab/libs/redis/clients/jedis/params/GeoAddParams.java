package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class GeoAddParams implements IParams {
   private boolean nx = false;
   private boolean xx = false;
   private boolean ch = false;

   public static GeoAddParams geoAddParams() {
      return new GeoAddParams();
   }

   public GeoAddParams nx() {
      this.nx = true;
      return this;
   }

   public GeoAddParams xx() {
      this.xx = true;
      return this;
   }

   public GeoAddParams ch() {
      this.ch = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.nx) {
         args.add(Protocol.Keyword.NX);
      } else if (this.xx) {
         args.add(Protocol.Keyword.XX);
      }

      if (this.ch) {
         args.add(Protocol.Keyword.CH);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         GeoAddParams that = (GeoAddParams)o;
         return this.nx == that.nx && this.xx == that.xx && this.ch == that.ch;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.nx, this.xx, this.ch);
   }
}
