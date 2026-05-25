package me.neznamy.tab.libs.net.kyori.event.method.annotation;

import java.lang.reflect.Method;
import me.neznamy.tab.libs.net.kyori.event.method.MethodScanner;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DefaultMethodScanner<L> implements MethodScanner<L> {
   private static final DefaultMethodScanner INSTANCE = new DefaultMethodScanner();

   public static <L> @NonNull MethodScanner<L> get() {
      return INSTANCE;
   }

   protected DefaultMethodScanner() {
   }

   @Override
   public boolean shouldRegister(final @NonNull L listener, final @NonNull Method method) {
      return method.getAnnotation(Subscribe.class) != null;
   }

   @Override
   public int postOrder(final @NonNull L listener, final @NonNull Method method) {
      return method.isAnnotationPresent(PostOrder.class) ? method.getAnnotation(PostOrder.class).value() : 0;
   }

   @Override
   public boolean consumeCancelledEvents(final @NonNull L listener, final @NonNull Method method) {
      return !method.isAnnotationPresent(IgnoreCancelled.class);
   }
}
