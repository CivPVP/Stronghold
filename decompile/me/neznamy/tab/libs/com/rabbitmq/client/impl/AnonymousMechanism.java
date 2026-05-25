package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import me.neznamy.tab.libs.com.rabbitmq.client.LongString;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslMechanism;

public class AnonymousMechanism implements SaslMechanism {
   @Override
   public String getName() {
      return "ANONYMOUS";
   }

   @Override
   public LongString handleChallenge(LongString challenge, String username, String password) {
      return LongStringHelper.asLongString("");
   }
}
