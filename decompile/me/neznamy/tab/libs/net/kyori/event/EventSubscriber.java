package me.neznamy.tab.libs.net.kyori.event;

import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface EventSubscriber<E> {
   void invoke(final @NonNull E event) throws Throwable;

   default int postOrder() {
      return 0;
   }

   default boolean consumeCancelledEvents() {
      return true;
   }

   default @Nullable Type genericType() {
      return null;
   }
}
