package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class DnsSrvRecordAddressResolver implements AddressResolver {
   private final String service;
   private final String dnsUrls;

   public DnsSrvRecordAddressResolver(String service) {
      this(service, "dns:");
   }

   public DnsSrvRecordAddressResolver(String service, String dnsUrls) {
      this.service = service;
      this.dnsUrls = dnsUrls;
   }

   @Override
   public List<Address> getAddresses() throws IOException {
      List<DnsSrvRecordAddressResolver.SrvRecord> records = this.lookupSrvRecords(this.service, this.dnsUrls);
      records = this.sort(records);
      List<Address> addresses = new ArrayList<>();

      for (DnsSrvRecordAddressResolver.SrvRecord record : records) {
         addresses.add(new Address(record.getHost(), record.getPort()));
      }

      return addresses;
   }

   protected List<DnsSrvRecordAddressResolver.SrvRecord> lookupSrvRecords(String service, String dnsUrls) throws IOException {
      Hashtable<String, String> env = new Hashtable<>();
      env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
      env.put("java.naming.provider.url", dnsUrls);
      List<DnsSrvRecordAddressResolver.SrvRecord> records = new ArrayList<>();

      try {
         DirContext ctx = new InitialDirContext(env);
         Attributes attributes = ctx.getAttributes(service, new String[]{"SRV"});
         NamingEnumeration<?> servers = attributes.get("srv").getAll();

         while (servers.hasMore()) {
            records.add(this.mapSrvRecord((String)servers.next()));
         }

         return records;
      } catch (NamingException e) {
         throw new IOException("Error during DNS SRV query", e);
      }
   }

   protected DnsSrvRecordAddressResolver.SrvRecord mapSrvRecord(String srvResult) {
      return DnsSrvRecordAddressResolver.SrvRecord.fromSrvQueryResult(srvResult);
   }

   protected List<DnsSrvRecordAddressResolver.SrvRecord> sort(List<DnsSrvRecordAddressResolver.SrvRecord> records) {
      Collections.sort(records);
      return records;
   }

   public static class SrvRecord implements Comparable<DnsSrvRecordAddressResolver.SrvRecord> {
      private final int priority;
      private final int weight;
      private final int port;
      private final String host;

      public SrvRecord(int priority, int weight, int port, String host) {
         this.priority = priority;
         this.weight = weight;
         this.port = port;
         int lastDotIndex = host.lastIndexOf(".");
         if (lastDotIndex > 0) {
            this.host = host.substring(0, lastDotIndex);
         } else {
            this.host = host;
         }
      }

      public int getPriority() {
         return this.priority;
      }

      public int getWeight() {
         return this.weight;
      }

      public int getPort() {
         return this.port;
      }

      public String getHost() {
         return this.host;
      }

      public static DnsSrvRecordAddressResolver.SrvRecord fromSrvQueryResult(String srvResult) {
         String[] fields = srvResult.split(" ");
         return new DnsSrvRecordAddressResolver.SrvRecord(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), fields[3]);
      }

      public int compareTo(DnsSrvRecordAddressResolver.SrvRecord o) {
         return this.priority < o.getPriority() ? -1 : (this.priority == o.getPriority() ? 0 : 1);
      }
   }
}
