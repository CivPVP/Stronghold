package me.neznamy.tab.libs.com.rabbitmq.client;

import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Address {
   private static final Logger LOGGER = LoggerFactory.getLogger(Address.class);
   private final String _host;
   private final int _port;

   public Address(String host, int port) {
      this._host = host;
      this._port = port;
   }

   public Address(String host) {
      this._host = host;
      this._port = -1;
   }

   public String getHost() {
      return this._host;
   }

   public int getPort() {
      return this._port;
   }

   public static String parseHost(String addressString) {
      int lastColon = addressString.lastIndexOf(":");
      int lastClosingSquareBracket = addressString.lastIndexOf("]");
      if (lastClosingSquareBracket == -1) {
         String[] parts = addressString.split(":");
         if (parts.length > 2) {
            String msg = "Address "
               + addressString
               + " seems to contain an unquoted IPv6 address. Make sure you quote IPv6 addresses like so: [2001:db8:85a3:8d3:1319:8a2e:370:7348]";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
         } else {
            return parts[0];
         }
      } else {
         return lastClosingSquareBracket < lastColon ? addressString.substring(0, lastColon) : addressString;
      }
   }

   public static int parsePort(String addressString) {
      int lastColon = addressString.lastIndexOf(":");
      int lastClosingSquareBracket = addressString.lastIndexOf("]");
      if (lastClosingSquareBracket == -1) {
         String[] parts = addressString.split(":");
         if (parts.length > 2) {
            String msg = "Address "
               + addressString
               + " seems to contain an unquoted IPv6 address. Make sure you quote IPv6 addresses like so: [2001:db8:85a3:8d3:1319:8a2e:370:7348]";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
         } else {
            return parts.length == 2 ? Integer.parseInt(parts[1]) : -1;
         }
      } else {
         return lastClosingSquareBracket < lastColon ? Integer.parseInt(addressString.substring(lastColon + 1)) : -1;
      }
   }

   public static boolean isHostWithPort(String addressString) {
      int lastColon = addressString.lastIndexOf(":");
      int lastClosingSquareBracket = addressString.lastIndexOf("]");
      return lastClosingSquareBracket == -1 ? addressString.contains(":") : lastClosingSquareBracket < lastColon;
   }

   public static Address parseAddress(String addressString) {
      return isHostWithPort(addressString) ? new Address(parseHost(addressString), parsePort(addressString)) : new Address(addressString);
   }

   public InetSocketAddress toInetSocketAddress(int port) {
      return new InetSocketAddress(this.getHost(), port);
   }

   public static Address[] parseAddresses(String addresses) {
      String[] addrs = addresses.split(" *, *");
      Address[] res = new Address[addrs.length];

      for (int i = 0; i < addrs.length; i++) {
         res[i] = parseAddress(addrs[i]);
      }

      return res;
   }

   @Override
   public int hashCode() {
      return 31 * this._host.hashCode() + this._port;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Address addr = (Address)obj;
         return this._host.equals(addr._host) && this._port == addr._port;
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return this._port == -1 ? this._host : this._host + ":" + this._port;
   }
}
