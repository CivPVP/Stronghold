package me.neznamy.tab.libs.net.kyori.event.method;

import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MethodScanner<L> {
   boolean shouldRegister(final @NonNull L listener, final @NonNull Method method);

   int postOrder(final @NonNull L listener, final @NonNull Method method);

   boolean consumeCancelledEvents(final @NonNull L listener, final @NonNull Method method);
}
