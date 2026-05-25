package me.neznamy.tab.libs.net.kyori.event.method;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MethodHandleEventExecutorFactory<E, L> implements EventExecutor.Factory<E, L> {
   @Override
   public @NonNull EventExecutor<E, L> create(final @NonNull Object object, final @NonNull Method method) throws Exception {
      MethodHandle handle = MethodHandles.publicLookup().unreflect(method).bindTo(object);
      return (listener, event) -> (Object)handle.invoke((Object)event);
   }
}
