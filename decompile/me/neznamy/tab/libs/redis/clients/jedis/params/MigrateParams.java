package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class MigrateParams implements IParams {
   private boolean copy = false;
   private boolean replace = false;
   private String username = null;
   private String password = null;

   public static MigrateParams migrateParams() {
      return new MigrateParams();
   }

   public MigrateParams copy() {
      this.copy = true;
      return this;
   }

   public MigrateParams replace() {
      this.replace = true;
      return this;
   }

   public MigrateParams auth(String password) {
      this.password = password;
      return this;
   }

   public MigrateParams auth2(String username, String password) {
      this.username = username;
      this.password = password;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.copy) {
         args.add(Protocol.Keyword.COPY);
      }

      if (this.replace) {
         args.add(Protocol.Keyword.REPLACE);
      }

      if (this.username != null) {
         args.add(Protocol.Keyword.AUTH2).add(this.username).add(this.password);
      } else if (this.password != null) {
         args.add(Protocol.Keyword.AUTH).add(this.password);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MigrateParams that = (MigrateParams)o;
         return this.copy == that.copy
            && this.replace == that.replace
            && Objects.equals(this.username, that.username)
            && Objects.equals(this.password, that.password);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.copy, this.replace, this.username, this.password);
   }
}
