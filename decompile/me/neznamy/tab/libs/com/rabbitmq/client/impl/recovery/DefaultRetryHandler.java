package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.util.Objects;
import java.util.function.BiPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRetryHandler implements RetryHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRetryHandler.class);
   protected final BiPredicate<? super RecordedQueue, Exception> queueRecoveryRetryCondition;
   protected final BiPredicate<? super RecordedExchange, Exception> exchangeRecoveryRetryCondition;
   protected final BiPredicate<? super RecordedBinding, Exception> bindingRecoveryRetryCondition;
   protected final BiPredicate<? super RecordedConsumer, Exception> consumerRecoveryRetryCondition;
   protected final DefaultRetryHandler.RetryOperation<?> queueRecoveryRetryOperation;
   protected final DefaultRetryHandler.RetryOperation<?> exchangeRecoveryRetryOperation;
   protected final DefaultRetryHandler.RetryOperation<?> bindingRecoveryRetryOperation;
   protected final DefaultRetryHandler.RetryOperation<?> consumerRecoveryRetryOperation;
   protected final int retryAttempts;
   protected final BackoffPolicy backoffPolicy;

   public DefaultRetryHandler(
      BiPredicate<? super RecordedQueue, Exception> queueRecoveryRetryCondition,
      BiPredicate<? super RecordedExchange, Exception> exchangeRecoveryRetryCondition,
      BiPredicate<? super RecordedBinding, Exception> bindingRecoveryRetryCondition,
      BiPredicate<? super RecordedConsumer, Exception> consumerRecoveryRetryCondition,
      DefaultRetryHandler.RetryOperation<?> queueRecoveryRetryOperation,
      DefaultRetryHandler.RetryOperation<?> exchangeRecoveryRetryOperation,
      DefaultRetryHandler.RetryOperation<?> bindingRecoveryRetryOperation,
      DefaultRetryHandler.RetryOperation<?> consumerRecoveryRetryOperation,
      int retryAttempts,
      BackoffPolicy backoffPolicy
   ) {
      this.queueRecoveryRetryCondition = queueRecoveryRetryCondition;
      this.exchangeRecoveryRetryCondition = exchangeRecoveryRetryCondition;
      this.bindingRecoveryRetryCondition = bindingRecoveryRetryCondition;
      this.consumerRecoveryRetryCondition = consumerRecoveryRetryCondition;
      this.queueRecoveryRetryOperation = queueRecoveryRetryOperation;
      this.exchangeRecoveryRetryOperation = exchangeRecoveryRetryOperation;
      this.bindingRecoveryRetryOperation = bindingRecoveryRetryOperation;
      this.consumerRecoveryRetryOperation = consumerRecoveryRetryOperation;
      this.backoffPolicy = backoffPolicy;
      if (retryAttempts <= 0) {
         throw new IllegalArgumentException("Number of retry attempts must be greater than 0");
      }

      this.retryAttempts = retryAttempts;
   }

   @Override
   public RetryResult retryQueueRecovery(RetryContext context) throws Exception {
      return this.doRetry(this.queueRecoveryRetryCondition, this.queueRecoveryRetryOperation, context.queue(), context);
   }

   @Override
   public RetryResult retryExchangeRecovery(RetryContext context) throws Exception {
      return this.doRetry(this.exchangeRecoveryRetryCondition, this.exchangeRecoveryRetryOperation, context.exchange(), context);
   }

   @Override
   public RetryResult retryBindingRecovery(RetryContext context) throws Exception {
      return this.doRetry(this.bindingRecoveryRetryCondition, this.bindingRecoveryRetryOperation, context.binding(), context);
   }

   @Override
   public RetryResult retryConsumerRecovery(RetryContext context) throws Exception {
      return this.doRetry(this.consumerRecoveryRetryCondition, this.consumerRecoveryRetryOperation, context.consumer(), context);
   }

   protected RetryResult doRetry(
      BiPredicate<RecordedEntity, Exception> condition, DefaultRetryHandler.RetryOperation<?> operation, RecordedEntity entity, RetryContext context
   ) throws Exception {
      int attempts = 0;
      Exception exception = context.exception();

      while (attempts < this.retryAttempts) {
         if (!condition.test(entity, exception)) {
            throw exception;
         }

         this.log(entity, exception, attempts);
         this.backoffPolicy.backoff(attempts + 1);

         try {
            Object result = operation.call(context);
            return new RetryResult(entity, result == null ? null : result.toString());
         } catch (Exception e) {
            exception = e;
            attempts++;
         }
      }

      throw exception;
   }

   protected void log(RecordedEntity entity, Exception exception, int attempts) {
      LOGGER.info("Error while recovering {}, retrying with {} more attempt(s).", new Object[]{entity, this.retryAttempts - attempts, exception});
   }

   public interface RetryOperation<T> {
      T call(RetryContext var1) throws Exception;

      default <V> DefaultRetryHandler.RetryOperation<V> andThen(DefaultRetryHandler.RetryOperation<V> after) {
         Objects.requireNonNull(after);
         return context -> {
            this.call(context);
            return after.call(context);
         };
      }
   }
}
