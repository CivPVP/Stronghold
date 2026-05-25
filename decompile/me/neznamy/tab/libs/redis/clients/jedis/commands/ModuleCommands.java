package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.Module;
import me.neznamy.tab.libs.redis.clients.jedis.params.ModuleLoadExParams;

public interface ModuleCommands {
   String moduleLoad(String var1);

   String moduleLoad(String var1, String... var2);

   String moduleLoadEx(String var1, ModuleLoadExParams var2);

   String moduleUnload(String var1);

   List<Module> moduleList();
}
