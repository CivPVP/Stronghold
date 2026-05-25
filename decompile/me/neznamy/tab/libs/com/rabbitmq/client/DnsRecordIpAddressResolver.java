package me.neznamy.tab.libs.com.rabbitmq.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DnsRecordIpAddressResolver implements AddressResolver {
   private final Address address;
   private final boolean ssl;

   public DnsRecordIpAddressResolver(String hostname, int port, boolean ssl) {
      this(new Address(hostname, port), ssl);
   }

   public DnsRecordIpAddressResolver(String hostname, int port) {
      this(new Address(hostname, port), false);
   }

   public DnsRecordIpAddressResolver() {
      this("localhost");
   }

   public DnsRecordIpAddressResolver(String hostname) {
      this(new Address(hostname), false);
   }

   public DnsRecordIpAddressResolver(Address address) {
      this(address, false);
   }

   public DnsRecordIpAddressResolver(Address address, boolean ssl) {
      this.address = address;
      this.ssl = ssl;
   }

   @Override
   public List<Address> getAddresses() throws UnknownHostException {
      String hostName = this.address.getHost();
      int portNumber = ConnectionFactory.portOrDefault(this.address.getPort(), this.ssl);
      InetAddress[] inetAddresses = this.resolveIpAddresses(hostName);
      List<Address> addresses = new ArrayList<>();

      for (InetAddress inetAddress : inetAddresses) {
         addresses.add(new ResolvedInetAddress(hostName, inetAddress, portNumber));
      }

      return addresses;
   }

   protected InetAddress[] resolveIpAddresses(String hostName) throws UnknownHostException {
      return InetAddress.getAllByName(hostName);
   }
}
