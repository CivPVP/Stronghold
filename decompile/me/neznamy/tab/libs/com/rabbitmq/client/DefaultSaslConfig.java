package me.neznamy.tab.libs.com.rabbitmq.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.AnonymousMechanism;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ExternalMechanism;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.PlainMechanism;

public class DefaultSaslConfig implements SaslConfig {
   private final String mechanism;
   public static final DefaultSaslConfig PLAIN = new DefaultSaslConfig("PLAIN");
   public static final DefaultSaslConfig EXTERNAL = new DefaultSaslConfig("EXTERNAL");
   public static final DefaultSaslConfig ANONYMOUS = new DefaultSaslConfig("ANONYMOUS");

   private DefaultSaslConfig(String mechanism) {
      this.mechanism = mechanism;
   }

   @Override
   public SaslMechanism getSaslMechanism(String[] serverMechanisms) {
      Set<String> server = new HashSet<>(Arrays.asList(serverMechanisms));
      if (server.contains(this.mechanism)) {
         if (this.mechanism.equals("PLAIN")) {
            return new PlainMechanism();
         }

         if (this.mechanism.equals("EXTERNAL")) {
            return new ExternalMechanism();
         }

         if (this.mechanism.equals("ANONYMOUS")) {
            return new AnonymousMechanism();
         }
      }

      return null;
   }
}
