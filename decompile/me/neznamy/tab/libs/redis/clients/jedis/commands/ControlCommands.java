package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import java.util.Map;

public interface ControlCommands extends AccessControlLogCommands, ClientCommands {
   List<Object> role();

   Long objectRefcount(String var1);

   String objectEncoding(String var1);

   Long objectIdletime(String var1);

   List<String> objectHelp();

   Long objectFreq(String var1);

   String memoryDoctor();

   Long memoryUsage(String var1);

   Long memoryUsage(String var1, int var2);

   String memoryPurge();

   Map<String, Object> memoryStats();
}
