package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.resps.AccessControlUser;

public interface AccessControlLogBinaryCommands {
   byte[] aclWhoAmIBinary();

   byte[] aclGenPassBinary();

   byte[] aclGenPassBinary(int var1);

   List<byte[]> aclListBinary();

   List<byte[]> aclUsersBinary();

   AccessControlUser aclGetUser(byte[] var1);

   String aclSetUser(byte[] var1);

   String aclSetUser(byte[] var1, byte[]... var2);

   long aclDelUser(byte[]... var1);

   List<byte[]> aclCatBinary();

   List<byte[]> aclCat(byte[] var1);

   List<byte[]> aclLogBinary();

   List<byte[]> aclLogBinary(int var1);

   String aclLogReset();

   String aclLoad();

   String aclSave();

   byte[] aclDryRunBinary(byte[] var1, byte[] var2, byte[]... var3);

   byte[] aclDryRunBinary(byte[] var1, CommandArguments var2);
}
