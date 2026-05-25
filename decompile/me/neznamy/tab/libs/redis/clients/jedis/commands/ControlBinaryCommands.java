package me.neznamy.tab.libs.redis.clients.jedis.commands;

import java.util.List;

public interface ControlBinaryCommands extends AccessControlLogBinaryCommands, ClientBinaryCommands {
   List<Object> roleBinary();

   Long objectRefcount(byte[] var1);

   byte[] objectEncoding(byte[] var1);

   Long objectIdletime(byte[] var1);

   List<byte[]> objectHelpBinary();

   Long objectFreq(byte[] var1);

   byte[] memoryDoctorBinary();

   Long memoryUsage(byte[] var1);

   Long memoryUsage(byte[] var1, int var2);
}
