package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.params.CommandListFilterByParams;
import me.neznamy.tab.libs.redis.clients.jedis.resps.CommandDocument;
import me.neznamy.tab.libs.redis.clients.jedis.resps.CommandInfo;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public interface CommandCommands {
   long commandCount();

   Map<String, CommandDocument> commandDocs(String... var1);

   List<String> commandGetKeys(String... var1);

   List<KeyValue<String, List<String>>> commandGetKeysAndFlags(String... var1);

   Map<String, CommandInfo> commandInfo(String... var1);

   List<String> commandList();

   List<String> commandListFilterBy(CommandListFilterByParams var1);
}
