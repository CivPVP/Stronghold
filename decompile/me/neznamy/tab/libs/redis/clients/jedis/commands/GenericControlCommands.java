package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Module;
import me.neznamy.tab.libs.redis.clients.jedis.params.FailoverParams;

public interface GenericControlCommands extends ConfigCommands, ScriptingControlCommands, SlowlogCommands {
   String failover();

   String failover(FailoverParams var1);

   String failoverAbort();

   List<Module> moduleList();
}
