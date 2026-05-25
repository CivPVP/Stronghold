package me.neznamy.tab.libs.redis.clients.jedis.json;

public class Path2 {
   public static final Path2 ROOT_PATH = new Path2("$");
   private final String str;

   public Path2(String str) {
      if (str == null) {
         throw new NullPointerException("Path cannot be null.");
      }

      if (str.isEmpty()) {
         throw new IllegalArgumentException("Path cannot be empty.");
      }

      if (str.charAt(0) == '$') {
         this.str = str;
      } else if (str.charAt(0) == '.') {
         this.str = '$' + str;
      } else {
         this.str = "$." + str;
      }
   }

   @Override
   public String toString() {
      return this.str;
   }

   public static Path2 of(String path) {
      return new Path2(path);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof Path2)) {
         return false;
      } else {
         return obj == this ? true : this.toString().equals(((Path2)obj).toString());
      }
   }

   @Override
   public int hashCode() {
      return this.str.hashCode();
   }
}
