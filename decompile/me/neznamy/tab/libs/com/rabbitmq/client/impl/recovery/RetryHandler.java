package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

public interface RetryHandler {
   RetryResult retryQueueRecovery(RetryContext var1) throws Exception;

   RetryResult retryExchangeRecovery(RetryContext var1) throws Exception;

   RetryResult retryBindingRecovery(RetryContext var1) throws Exception;

   RetryResult retryConsumerRecovery(RetryContext var1) throws Exception;
}
