package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.DataInputStream;
import java.io.IOException;
import me.neznamy.tab.libs.com.rabbitmq.client.BasicProperties;

public abstract class AMQBasicProperties extends AMQContentHeader implements BasicProperties {
   protected AMQBasicProperties() {
   }

   protected AMQBasicProperties(DataInputStream in) throws IOException {
      super(in);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}
