package me.neznamy.tab.libs.com.rabbitmq.client;

public interface SaslMechanism {
   String getName();

   LongString handleChallenge(LongString var1, String var2, String var3);
}
