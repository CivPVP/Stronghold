package me.neznamy.tab.libs.redis.clients.jedis.util;

import java.io.IOException;
import java.net.Socket;

public class IOUtils {
   public static void closeQuietly(Socket sock) {
      if (sock != null) {
         try {
            sock.close();
         } catch (IOException var2) {
         }
      }
   }

   public static void closeQuietly(AutoCloseable resource) {
      if (resource != null) {
         try {
            resource.close();
         } catch (Exception var2) {
         }
      }
   }

   private IOUtils() {
      throw new InstantiationError("Must not instantiate this class");
   }
}
