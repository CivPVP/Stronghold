package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.Arrays;
import me.neznamy.tab.libs.com.rabbitmq.client.LongString;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslConfig;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslMechanism;

public class CRDemoMechanism implements SaslMechanism {
   private static final String NAME = "RABBIT-CR-DEMO";
   private int round = 0;

   @Override
   public String getName() {
      return "RABBIT-CR-DEMO";
   }

   @Override
   public LongString handleChallenge(LongString challenge, String username, String password) {
      this.round++;
      return this.round == 1 ? LongStringHelper.asLongString(username) : LongStringHelper.asLongString("My password is " + password);
   }

   public static class CRDemoSaslConfig implements SaslConfig {
      @Override
      public SaslMechanism getSaslMechanism(String[] mechanisms) {
         return Arrays.asList(mechanisms).contains("RABBIT-CR-DEMO") ? new CRDemoMechanism() : null;
      }
   }
}
