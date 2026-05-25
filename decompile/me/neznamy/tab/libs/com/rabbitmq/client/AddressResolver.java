package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface AddressResolver {
   List<Address> getAddresses() throws IOException;

   default List<Address> maybeShuffle(List<Address> input) {
      List<Address> list = new ArrayList<>(input);
      Collections.shuffle(list);
      return list;
   }
}
