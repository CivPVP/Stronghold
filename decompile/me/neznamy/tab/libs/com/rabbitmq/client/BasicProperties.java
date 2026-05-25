package me.neznamy.tab.libs.com.rabbitmq.client;

import java.util.Date;
import java.util.Map;

public interface BasicProperties {
   String getContentType();

   String getContentEncoding();

   Map<String, Object> getHeaders();

   Integer getDeliveryMode();

   Integer getPriority();

   String getCorrelationId();

   String getReplyTo();

   String getExpiration();

   String getMessageId();

   Date getTimestamp();

   String getType();

   String getUserId();

   String getAppId();
}
