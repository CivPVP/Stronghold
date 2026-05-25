package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientVersion {
   private static final Logger LOGGER = LoggerFactory.getLogger(ClientVersion.class);
   private static final char[] VERSION_PROPERTY = new char[]{
      'c', 'o', 'm', '.', 'r', 'a', 'b', 'b', 'i', 't', 'm', 'q', '.', 'c', 'l', 'i', 'e', 'n', 't', '.', 'v', 'e', 'r', 's', 'i', 'o', 'n'
   };
   public static final String VERSION;

   private static final String getVersionFromPropertyFile() throws Exception {
      InputStream inputStream = ClientVersion.class.getClassLoader().getResourceAsStream("rabbitmq-amqp-client.properties");
      Properties version = new Properties();

      try {
         version.load(inputStream);
      } finally {
         if (inputStream != null) {
            inputStream.close();
         }
      }

      String propertyName = new String(VERSION_PROPERTY);
      String versionProperty = version.getProperty(propertyName);
      if (versionProperty == null) {
         throw new IllegalStateException("Couldn't find version property in property file");
      } else {
         return versionProperty;
      }
   }

   private static final String getVersionFromPackage() {
      if (ClientVersion.class.getPackage().getImplementationVersion() == null) {
         throw new IllegalStateException("Couldn't get version with Package#getImplementationVersion");
      } else {
         return ClientVersion.class.getPackage().getImplementationVersion();
      }
   }

   private static final String getDefaultVersion() {
      return "0.0.0";
   }

   static {
      String version;
      try {
         version = getVersionFromPropertyFile();
      } catch (Exception e1) {
         LOGGER.warn("Couldn't get version from property file", e1);

         try {
            version = getVersionFromPackage();
         } catch (Exception e2) {
            LOGGER.warn("Couldn't get version with Package#getImplementationVersion", e1);
            version = getDefaultVersion();
         }
      }

      VERSION = version;
   }
}
