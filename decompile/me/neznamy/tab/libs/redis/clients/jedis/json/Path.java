package me.neznamy.tab.libs.redis.clients.jedis.json;

@Deprecated
public class Path {
   public static final Path ROOT_PATH = new Path(".");
   private final String strPath;

   public Path(String strPath) {
      this.strPath = strPath;
   }

   @Override
   public String toString() {
      return this.strPath;
   }

   public static Path of(String strPath) {
      return new Path(strPath);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof Path)) {
         return false;
      } else {
         return obj == this ? true : this.toString().equals(((Path)obj).toString());
      }
   }

   @Override
   public int hashCode() {
      return this.strPath.hashCode();
   }
}
