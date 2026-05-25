package me.neznamy.tab.libs.redis.clients.jedis.providers;

import java.util.Collections;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;

public interface ConnectionProvider extends AutoCloseable {
   Connection getConnection();

   Connection getConnection(CommandArguments var1);

   default Map<?, ?> getConnectionMap() {
      Connection c = this.getConnection();
      return Collections.singletonMap(c.toString(), c);
   }
}
