package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import me.neznamy.tab.libs.com.rabbitmq.client.Command;
import me.neznamy.tab.libs.com.rabbitmq.client.TrafficListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTrafficListener implements TrafficListener {
   private static final Logger LOGGER = LoggerFactory.getLogger(LogTrafficListener.class);

   @Override
   public void write(Command outboundCommand) {
      if (this.shouldLog(outboundCommand)) {
         LOGGER.trace("Outbound command: {}", outboundCommand);
      }
   }

   @Override
   public void read(Command inboundCommand) {
      if (this.shouldLog(inboundCommand)) {
         LOGGER.trace("Inbound command: {}", inboundCommand);
      }
   }

   protected boolean shouldLog(Command command) {
      return LOGGER.isTraceEnabled();
   }
}
