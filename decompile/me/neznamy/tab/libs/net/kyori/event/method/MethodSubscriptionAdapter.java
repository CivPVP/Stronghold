package me.neznamy.tab.libs.net.kyori.event.method;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface MethodSubscriptionAdapter<L> {
   void register(final @NonNull L listener);

   void unregister(final @NonNull L listener);
}
