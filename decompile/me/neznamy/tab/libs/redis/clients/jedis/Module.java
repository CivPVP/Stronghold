package me.neznamy.tab.libs.redis.clients.jedis;

public class Module {
   private final String name;
   private final int version;

   public Module(String name, int version) {
      this.name = name;
      this.version = version;
   }

   public String getName() {
      return this.name;
   }

   public int getVersion() {
      return this.version;
   }

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (o == this) {
         return true;
      } else if (!(o instanceof Module)) {
         return false;
      } else {
         Module module = (Module)o;
         if (this.version != module.version) {
            return false;
         } else {
            return this.name != null ? this.name.equals(module.name) : module.name == null;
         }
      }
   }

   @Override
   public int hashCode() {
      int result = this.name != null ? this.name.hashCode() : 0;
      return 31 * result + this.version;
   }
}
