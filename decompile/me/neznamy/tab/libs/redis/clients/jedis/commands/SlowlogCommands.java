package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.resps.Slowlog;

public interface SlowlogCommands {
   String slowlogReset();

   long slowlogLen();

   List<Slowlog> slowlogGet();

   List<Object> slowlogGetBinary();

   List<Slowlog> slowlogGet(long var1);

   List<Object> slowlogGetBinary(long var1);
}
