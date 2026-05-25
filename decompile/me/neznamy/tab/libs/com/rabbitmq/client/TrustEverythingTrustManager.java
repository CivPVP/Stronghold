package me.neznamy.tab.libs.com.rabbitmq.client;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.slf4j.LoggerFactory;

public class TrustEverythingTrustManager implements X509TrustManager {
   public TrustEverythingTrustManager() {
      LoggerFactory.getLogger(TrustEverythingTrustManager.class)
         .warn(
            "SECURITY ALERT: this trust manager trusts every certificate, effectively disabling peer verification. This is convenient for local development but offers no protection against man-in-the-middle attacks. Please see https://www.rabbitmq.com/ssl.html to learn more about peer certificate verification."
         );
   }

   @Override
   public void checkClientTrusted(X509Certificate[] chain, String authType) {
   }

   @Override
   public void checkServerTrusted(X509Certificate[] chain, String authType) {
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
   }
}
