package me.neznamy.tab.libs.com.rabbitmq.client;

import java.net.ProtocolException;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Version;

public class ProtocolVersionMismatchException extends ProtocolException {
   private static final long serialVersionUID = 1L;
   private final Version clientVersion;
   private final Version serverVersion;

   public ProtocolVersionMismatchException(Version clientVersion, Version serverVersion) {
      super("Protocol version mismatch: expected " + clientVersion + ", got " + serverVersion);
      this.clientVersion = clientVersion;
      this.serverVersion = serverVersion;
   }

   public Version getClientVersion() {
      return this.clientVersion;
   }

   public Version getServerVersion() {
      return this.serverVersion;
   }

   public int getClientMajor() {
      return this.clientVersion.getMajor();
   }

   public int getClientMinor() {
      return this.clientVersion.getMinor();
   }

   public int getServerMajor() {
      return this.serverVersion.getMajor();
   }

   public int getServerMinor() {
      return this.serverVersion.getMinor();
   }
}
