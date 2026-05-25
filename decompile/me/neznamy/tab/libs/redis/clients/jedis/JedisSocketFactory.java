package me.neznamy.tab.libs.redis.clients.jedis;

import java.net.Socket;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;

public interface JedisSocketFactory {
   Socket createSocket() throws JedisConnectionException;
}
