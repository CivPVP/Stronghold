package me.neznamy.tab.libs.redis.clients.jedis;

import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;

@Experimental
public interface CommandKeyArgumentPreProcessor {
   Object actualKey(Object var1);
}
