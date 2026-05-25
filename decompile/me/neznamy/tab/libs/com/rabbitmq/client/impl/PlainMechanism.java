package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import me.neznamy.tab.libs.com.rabbitmq.client.LongString;
import me.neznamy.tab.libs.com.rabbitmq.client.SaslMechanism;

public class PlainMechanism implements SaslMechanism {
   @Override
   public String getName() {
      return "PLAIN";
   }

   @Override
   public LongString handleChallenge(LongString challenge, String username, String password) {
      return LongStringHelper.asLongString("\u0000" + username + "\u0000" + password);
   }
}
