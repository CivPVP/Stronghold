package me.neznamy.tab.libs.net.kyori.event;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

final class SubscriberRegistry<E> {
   private static final LoadingCache<Class<?>, Set<Class<?>>> CLASS_HIERARCHY = CacheBuilder.newBuilder()
      .weakKeys()
      .build(CacheLoader.from(key -> TypeToken.of(key).getTypes().rawTypes()));
   private final SetMultimap<Class<?>, EventSubscriber<?>> subscribers = HashMultimap.create();
   private final LoadingCache<Class<?>, List<EventSubscriber<?>>> cache = CacheBuilder.newBuilder().initialCapacity(85).build(CacheLoader.from(eventClass -> {
      List<EventSubscriber<?>> subscribers = new ArrayList<>();
      Set<? extends Class<?>> types = (Set<? extends Class<?>>)CLASS_HIERARCHY.getUnchecked(eventClass);
      assert types != null;
      synchronized (this.lock) {
         for (Class<?> type : types) {
            subscribers.addAll(this.subscribers.get(type));
         }
      }

      subscribers.sort(Comparator.comparingInt(EventSubscriber::postOrder));
      return subscribers;
   }));
   private final Object lock = new Object();

   <T extends E> void register(final @NonNull Class<T> clazz, final @NonNull EventSubscriber<? super T> subscriber) {
      synchronized (this.lock) {
         this.subscribers.put(clazz, subscriber);
         this.cache.invalidateAll();
      }
   }

   void unregister(final @NonNull EventSubscriber<?> subscriber) {
      this.unregisterMatching(h -> h.equals(subscriber));
   }

   void unregisterMatching(final @NonNull Predicate<EventSubscriber<?>> predicate) {
      synchronized (this.lock) {
         boolean dirty = this.subscribers.values().removeIf(predicate);
         if (dirty) {
            this.cache.invalidateAll();
         }
      }
   }

   void unregisterAll() {
      synchronized (this.lock) {
         this.subscribers.clear();
         this.cache.invalidateAll();
      }
   }

   @NonNull SetMultimap<Class<?>, EventSubscriber<?>> subscribers() {
      synchronized (this.lock) {
         return ImmutableSetMultimap.copyOf(this.subscribers);
      }
   }

   @NonNull List<EventSubscriber<?>> subscribers(final @NonNull Class<?> clazz) {
      return (List<EventSubscriber<?>>)this.cache.getUnchecked(clazz);
   }
}
