package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.LoggerFactory;

class JedisMetaInfo {
   private static final String groupId;
   private static final String artifactId;
   private static final String version;

   public static String getGroupId() {
      return groupId;
   }

   public static String getArtifactId() {
      return artifactId;
   }

   public static String getVersion() {
      return version;
   }

   static {
      Properties p = new Properties();

      try (InputStream in = JedisMetaInfo.class.getClassLoader().getResourceAsStream("me/neznamy/tab/libs/redis/clients/jedis/pom.properties")) {
         p.load(in);
      } catch (Exception e) {
         LoggerFactory.getLogger(JedisMetaInfo.class).error("Load Jedis meta info from pom.properties failed", e);
      }

      groupId = p.getProperty("groupId", null);
      artifactId = p.getProperty("artifactId", null);
      version = p.getProperty("version", null);
   }
}
