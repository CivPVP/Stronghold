package me.neznamy.tab.libs.net.kyori.event;

import com.google.common.collect.SetMultimap;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface EventBus<E> {
   @NonNull Class<E> eventType();

   @NonNull PostResult post(final @NonNull E event);

   <T extends E> void register(final @NonNull Class<T> clazz, final @NonNull EventSubscriber<? super T> subscriber);

   void unregister(final @NonNull EventSubscriber<?> subscriber);

   void unregister(final @NonNull Predicate<EventSubscriber<?>> predicate);

   void unregisterAll();

   <T extends E> boolean hasSubscribers(final @NonNull Class<T> clazz);

   @NonNull SetMultimap<Class<?>, EventSubscriber<?>> subscribers();
}
