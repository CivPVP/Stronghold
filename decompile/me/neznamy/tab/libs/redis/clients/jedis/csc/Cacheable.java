package me.neznamy.tab.libs.redis.clients.jedis.csc;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;

public interface Cacheable {
   boolean isCacheable(ProtocolCommand var1, List<Object> var2);
}
