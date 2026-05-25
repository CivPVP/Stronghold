package me.neznamy.tab.libs.com.rabbitmq.client;

import javax.net.ssl.SSLContext;

public interface SslContextFactory {
   SSLContext create(String var1);
}
