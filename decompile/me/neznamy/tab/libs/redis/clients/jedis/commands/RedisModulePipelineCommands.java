package me.neznamy.tab.libs.redis.clients.jedis.commands;

import me.neznamy.tab.libs.redis.clients.jedis.bloom.commands.RedisBloomPipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.graph.RedisGraphPipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.json.commands.RedisJsonPipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.search.RediSearchPipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.timeseries.RedisTimeSeriesPipelineCommands;

public interface RedisModulePipelineCommands
   extends RediSearchPipelineCommands,
   RedisJsonPipelineCommands,
   RedisTimeSeriesPipelineCommands,
   RedisBloomPipelineCommands,
   RedisGraphPipelineCommands {
}
