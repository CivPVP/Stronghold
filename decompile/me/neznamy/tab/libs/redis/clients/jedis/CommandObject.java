package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Iterator;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;

public class CommandObject<T> {
   private final CommandArguments arguments;
   private final Builder<T> builder;

   public CommandObject(CommandArguments args, Builder<T> builder) {
      this.arguments = args;
      this.builder = builder;
   }

   public CommandArguments getArguments() {
      return this.arguments;
   }

   public Builder<T> getBuilder() {
      return this.builder;
   }

   @Override
   public int hashCode() {
      int hashCode = 1;

      for (Rawable e : this.arguments) {
         hashCode = 31 * hashCode + e.hashCode();
      }

      return 31 * hashCode + this.builder.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof CommandObject)) {
         return false;
      }

      Iterator<Rawable> e1 = this.arguments.iterator();
      Iterator<Rawable> e2 = ((CommandObject)o).arguments.iterator();

      while (e1.hasNext() && e2.hasNext()) {
         Rawable o1 = e1.next();
         Rawable o2 = e2.next();
         if (o1 == null ? o2 != null : !o1.equals(o2)) {
            return false;
         }
      }

      return !e1.hasNext() && !e2.hasNext() ? this.builder == ((CommandObject)o).builder : false;
   }
}
