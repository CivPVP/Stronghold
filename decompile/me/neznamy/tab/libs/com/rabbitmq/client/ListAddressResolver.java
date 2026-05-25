package me.neznamy.tab.libs.com.rabbitmq.client;

import java.util.List;

public class ListAddressResolver implements AddressResolver {
   private final List<Address> addresses;

   public ListAddressResolver(List<Address> addresses) {
      this.addresses = addresses;
   }

   @Override
   public List<Address> getAddresses() {
      return this.addresses;
   }
}
