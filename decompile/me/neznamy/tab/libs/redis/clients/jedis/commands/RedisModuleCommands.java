package me.neznamy.tab.libs.redis.clients.jedis.commands;

import me.neznamy.tab.libs.redis.clients.jedis.bloom.commands.RedisBloomCommands;
import me.neznamy.tab.libs.redis.clients.jedis.gears.RedisGearsCommands;
import me.neznamy.tab.libs.redis.clients.jedis.graph.RedisGraphCommands;
import me.neznamy.tab.libs.redis.clients.jedis.json.commands.RedisJsonCommands;
import me.neznamy.tab.libs.redis.clients.jedis.search.RediSearchCommands;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.RedisTimeSeriesCommands;

public interface RedisModuleCommands
   extends RediSearchCommands,
   RedisJsonCommands,
   RedisTimeSeriesCommands,
   RedisBloomCommands,
   RedisGraphCommands,
   RedisGearsCommands {
}
