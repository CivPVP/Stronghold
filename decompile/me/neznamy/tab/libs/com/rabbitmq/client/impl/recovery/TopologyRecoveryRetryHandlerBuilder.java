package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import java.util.function.BiPredicate;

public class TopologyRecoveryRetryHandlerBuilder {
   protected BiPredicate<? super RecordedQueue, Exception> queueRecoveryRetryCondition = (q, e) -> false;
   protected BiPredicate<? super RecordedExchange, Exception> exchangeRecoveryRetryCondition = (ex, e) -> false;
   protected BiPredicate<? super RecordedBinding, Exception> bindingRecoveryRetryCondition = (b, e) -> false;
   protected BiPredicate<? super RecordedConsumer, Exception> consumerRecoveryRetryCondition = (c, e) -> false;
   protected DefaultRetryHandler.RetryOperation<?> queueRecoveryRetryOperation = context -> null;
   protected DefaultRetryHandler.RetryOperation<?> exchangeRecoveryRetryOperation = context -> null;
   protected DefaultRetryHandler.RetryOperation<?> bindingRecoveryRetryOperation = context -> null;
   protected DefaultRetryHandler.RetryOperation<?> consumerRecoveryRetryOperation = context -> null;
   protected int retryAttempts = 2;
   protected BackoffPolicy backoffPolicy = nbAttempts -> {};

   public static TopologyRecoveryRetryHandlerBuilder builder() {
      return new TopologyRecoveryRetryHandlerBuilder();
   }

   public TopologyRecoveryRetryHandlerBuilder queueRecoveryRetryCondition(BiPredicate<? super RecordedQueue, Exception> queueRecoveryRetryCondition) {
      this.queueRecoveryRetryCondition = queueRecoveryRetryCondition;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder exchangeRecoveryRetryCondition(BiPredicate<? super RecordedExchange, Exception> exchangeRecoveryRetryCondition) {
      this.exchangeRecoveryRetryCondition = exchangeRecoveryRetryCondition;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder bindingRecoveryRetryCondition(BiPredicate<? super RecordedBinding, Exception> bindingRecoveryRetryCondition) {
      this.bindingRecoveryRetryCondition = bindingRecoveryRetryCondition;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder consumerRecoveryRetryCondition(BiPredicate<? super RecordedConsumer, Exception> consumerRecoveryRetryCondition) {
      this.consumerRecoveryRetryCondition = consumerRecoveryRetryCondition;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder queueRecoveryRetryOperation(DefaultRetryHandler.RetryOperation<?> queueRecoveryRetryOperation) {
      this.queueRecoveryRetryOperation = queueRecoveryRetryOperation;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder exchangeRecoveryRetryOperation(DefaultRetryHandler.RetryOperation<?> exchangeRecoveryRetryOperation) {
      this.exchangeRecoveryRetryOperation = exchangeRecoveryRetryOperation;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder bindingRecoveryRetryOperation(DefaultRetryHandler.RetryOperation<?> bindingRecoveryRetryOperation) {
      this.bindingRecoveryRetryOperation = bindingRecoveryRetryOperation;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder consumerRecoveryRetryOperation(DefaultRetryHandler.RetryOperation<?> consumerRecoveryRetryOperation) {
      this.consumerRecoveryRetryOperation = consumerRecoveryRetryOperation;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder backoffPolicy(BackoffPolicy backoffPolicy) {
      this.backoffPolicy = backoffPolicy;
      return this;
   }

   public TopologyRecoveryRetryHandlerBuilder retryAttempts(int retryAttempts) {
      this.retryAttempts = retryAttempts;
      return this;
   }

   public RetryHandler build() {
      return new DefaultRetryHandler(
         this.queueRecoveryRetryCondition,
         this.exchangeRecoveryRetryCondition,
         this.bindingRecoveryRetryCondition,
         this.consumerRecoveryRetryCondition,
         this.queueRecoveryRetryOperation,
         this.exchangeRecoveryRetryOperation,
         this.bindingRecoveryRetryOperation,
         this.consumerRecoveryRetryOperation,
         this.retryAttempts,
         this.backoffPolicy
      );
   }
}
