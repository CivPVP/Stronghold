package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface CredentialsRefreshService {
   String register(CredentialsProvider var1, Callable<Boolean> var2);

   void unregister(CredentialsProvider var1, String var2);

   boolean isApproachingExpiration(Duration var1);
}
