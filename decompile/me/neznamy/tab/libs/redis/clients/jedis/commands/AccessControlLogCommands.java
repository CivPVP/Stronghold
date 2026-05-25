package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlLogEntry;
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlUser;

public interface AccessControlLogCommands {
   String aclWhoAmI();

   String aclGenPass();

   String aclGenPass(int var1);

   List<String> aclList();

   List<String> aclUsers();

   AccessControlUser aclGetUser(String var1);

   String aclSetUser(String var1);

   String aclSetUser(String var1, String... var2);

   long aclDelUser(String... var1);

   List<String> aclCat();

   List<String> aclCat(String var1);

   List<AccessControlLogEntry> aclLog();

   List<AccessControlLogEntry> aclLog(int var1);

   String aclLogReset();

   String aclLoad();

   String aclSave();

   String aclDryRun(String var1, String var2, String... var3);

   String aclDryRun(String var1, CommandArguments var2);
}
