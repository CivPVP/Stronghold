package me.neznamy.tab.libs.net.kyori.event.method;

import com.google.common.base.MoreObjects;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiConsumer;
import me.neznamy.tab.libs.net.kyori.event.EventBus;
import me.neznamy.tab.libs.net.kyori.event.EventSubscriber;
import me.neznamy.tab.libs.net.kyori.event.ReifiedEvent;
import me.neznamy.tab.libs.net.kyori.event.method.annotation.DefaultMethodScanner;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SimpleMethodSubscriptionAdapter<E, L> implements MethodSubscriptionAdapter<L> {
   private final EventBus<E> bus;
   private final EventExecutor.Factory<E, L> factory;
   private final MethodScanner<L> methodScanner;

   public SimpleMethodSubscriptionAdapter(final @NonNull EventBus<E> bus, final EventExecutor.@NonNull Factory<E, L> factory) {
      this(bus, factory, DefaultMethodScanner.get());
   }

   public SimpleMethodSubscriptionAdapter(
      final @NonNull EventBus<E> bus, final EventExecutor.@NonNull Factory<E, L> factory, final @NonNull MethodScanner<L> methodScanner
   ) {
      this.bus = bus;
      this.factory = factory;
      this.methodScanner = methodScanner;
   }

   @Override
   public void register(final @NonNull L listener) {
      this.findSubscribers(listener, this.bus::register);
   }

   @Override
   public void unregister(final @NonNull L listener) {
      this.bus
         .unregister(
            h -> h instanceof SimpleMethodSubscriptionAdapter.MethodEventSubscriber
               && ((SimpleMethodSubscriptionAdapter.MethodEventSubscriber)h).listener() == listener
         );
   }

   private void findSubscribers(final @NonNull L listener, final BiConsumer<@NonNull Class<? extends E>, @NonNull EventSubscriber<E>> consumer) {
      for (Method method : listener.getClass().getDeclaredMethods()) {
         if (this.methodScanner.shouldRegister(listener, method)) {
            if (method.getParameterCount() != 1) {
               throw new SimpleMethodSubscriptionAdapter.SubscriberGenerationException(
                  "Unable to create an event subscriber for method '" + method + "'. Method must have only one parameter."
               );
            }

            Class<?> methodParameterType = method.getParameterTypes()[0];
            if (!this.bus.eventType().isAssignableFrom(methodParameterType)) {
               throw new SimpleMethodSubscriptionAdapter.SubscriberGenerationException(
                  "Unable to create an event subscriber for method '"
                     + method
                     + "'. Method parameter type '"
                     + methodParameterType
                     + "' does not extend event type '"
                     + this.bus.eventType()
                     + '\''
               );
            }

            EventExecutor<E, L> executor;
            try {
               executor = this.factory.create(listener, method);
            } catch (Exception e) {
               throw new SimpleMethodSubscriptionAdapter.SubscriberGenerationException(
                  "Encountered an exception while creating an event subscriber for method '" + method + '\'', e
               );
            }

            Class<? extends E> eventClass = (Class<? extends E>)methodParameterType;
            int postOrder = this.methodScanner.postOrder(listener, method);
            boolean consumeCancelled = this.methodScanner.consumeCancelledEvents(listener, method);
            consumer.accept(
               eventClass, new SimpleMethodSubscriptionAdapter.MethodEventSubscriber<>(eventClass, method, executor, listener, postOrder, consumeCancelled)
            );
         }
      }
   }

   private static final class MethodEventSubscriber<E, L> implements EventSubscriber<E> {
      private final Class<? extends E> event;
      private final @Nullable Type generic;
      private final EventExecutor<E, L> executor;
      private final L listener;
      private final int postOrder;
      private final boolean includeCancelled;

      MethodEventSubscriber(
         final Class<? extends E> eventClass,
         final @NonNull Method method,
         final @NonNull EventExecutor<E, L> executor,
         final @NonNull L listener,
         final int postOrder,
         final boolean includeCancelled
      ) {
         this.event = eventClass;
         this.generic = ReifiedEvent.class.isAssignableFrom(this.event) ? genericType(method.getGenericParameterTypes()[0]) : null;
         this.executor = executor;
         this.listener = listener;
         this.postOrder = postOrder;
         this.includeCancelled = includeCancelled;
      }

      private static @Nullable Type genericType(final Type type) {
         return type instanceof ParameterizedType ? ((ParameterizedType)type).getActualTypeArguments()[0] : null;
      }

      @NonNull L listener() {
         return this.listener;
      }

      @Override
      public void invoke(final @NonNull E event) throws Throwable {
         this.executor.invoke(this.listener, event);
      }

      @Override
      public int postOrder() {
         return this.postOrder;
      }

      @Override
      public boolean consumeCancelledEvents() {
         return this.includeCancelled;
      }

      @Override
      public @Nullable Type genericType() {
         return this.generic;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.event, this.generic, this.executor, this.listener, this.postOrder, this.includeCancelled);
      }

      @Override
      public boolean equals(final Object other) {
         if (this == other) {
            return true;
         } else if (other != null && other instanceof SimpleMethodSubscriptionAdapter.MethodEventSubscriber) {
            SimpleMethodSubscriptionAdapter.MethodEventSubscriber<?, ?> that = (SimpleMethodSubscriptionAdapter.MethodEventSubscriber<?, ?>)other;
            return Objects.equals(this.event, that.event)
               && Objects.equals(this.generic, that.generic)
               && Objects.equals(this.executor, that.executor)
               && Objects.equals(this.listener, that.listener)
               && Objects.equals(this.postOrder, that.postOrder)
               && Objects.equals(this.includeCancelled, that.includeCancelled);
         } else {
            return false;
         }
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this)
            .add("event", this.event)
            .add("generic", this.generic)
            .add("executor", this.executor)
            .add("listener", this.listener)
            .add("priority", this.postOrder)
            .add("includeCancelled", this.includeCancelled)
            .toString();
      }
   }

   public static final class SubscriberGenerationException extends RuntimeException {
      SubscriberGenerationException(final String message) {
         super(message);
      }

      SubscriberGenerationException(final String message, final Throwable cause) {
         super(message, cause);
      }
   }
}
