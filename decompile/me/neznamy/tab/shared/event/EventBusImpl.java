package me.neznamy.tab.shared.event;

import java.lang.reflect.Method;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.EventHandler;
import me.neznamy.tab.api.event.Subscribe;
import me.neznamy.tab.api.event.TabEvent;
import me.neznamy.tab.libs.net.kyori.event.EventSubscriber;
import me.neznamy.tab.libs.net.kyori.event.PostResult;
import me.neznamy.tab.libs.net.kyori.event.SimpleEventBus;
import me.neznamy.tab.libs.net.kyori.event.method.MethodHandleEventExecutorFactory;
import me.neznamy.tab.libs.net.kyori.event.method.MethodScanner;
import me.neznamy.tab.libs.net.kyori.event.method.MethodSubscriptionAdapter;
import me.neznamy.tab.libs.net.kyori.event.method.SimpleMethodSubscriptionAdapter;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

public class EventBusImpl implements EventBus {
   private final SimpleEventBus<TabEvent> bus = new SimpleEventBus<TabEvent>(TabEvent.class) {
      protected boolean shouldPost(@NotNull TabEvent event, @NotNull EventSubscriber<?> subscriber) {
         return true;
      }
   };
   private final MethodSubscriptionAdapter<Object> methodAdapter = new SimpleMethodSubscriptionAdapter<>(
      this.bus, new MethodHandleEventExecutorFactory<>(), new EventBusImpl.TabMethodScanner()
   );

   public <E extends TabEvent> void fire(E event) {
      if (this.bus.hasSubscribers(event.getClass())) {
         PostResult result = this.bus.post(event);
         if (!result.exceptions().isEmpty()) {
            TAB.getInstance().getErrorManager().errorFiringEvent(event, result.exceptions().values());
         }
      }
   }

   @Override
   public void register(@NonNull Object listener) {
      if (listener == null) {
         throw new NullPointerException("listener is marked non-null but is null");
      }

      this.methodAdapter.register(listener);
   }

   @Override
   public <E extends TabEvent> void register(@NonNull Class<E> type, @NonNull EventHandler<E> handler) {
      if (type == null) {
         throw new NullPointerException("type is marked non-null but is null");
      }

      if (handler == null) {
         throw new NullPointerException("handler is marked non-null but is null");
      }

      this.bus.register(type, new EventBusImpl.HandlerWrapper<>(handler));
   }

   @Override
   public void unregister(@NonNull Object listener) {
      if (listener == null) {
         throw new NullPointerException("listener is marked non-null but is null");
      }

      this.methodAdapter.unregister(listener);
   }

   @Override
   public <E extends TabEvent> void unregister(@NonNull EventHandler<E> handler) {
      if (handler == null) {
         throw new NullPointerException("handler is marked non-null but is null");
      }

      this.bus.unregister(subscriber -> subscriber instanceof EventBusImpl.HandlerWrapper && ((EventBusImpl.HandlerWrapper)subscriber).handler == handler);
   }

   private static class HandlerWrapper<E> implements EventSubscriber<E> {
      private final EventHandler<E> handler;

      @Override
      public void invoke(@NotNull E event) {
         this.handler.handle(event);
      }

      @Generated
      public HandlerWrapper(EventHandler<E> handler) {
         this.handler = handler;
      }
   }

   private static class TabMethodScanner implements MethodScanner<Object> {
      private TabMethodScanner() {
      }

      @Override
      public boolean shouldRegister(@NotNull Object listener, @NotNull Method method) {
         return method.isAnnotationPresent(Subscribe.class);
      }

      @Override
      public int postOrder(@NotNull Object listener, @NotNull Method method) {
         return 0;
      }

      @Override
      public boolean consumeCancelledEvents(@NotNull Object listener, @NotNull Method method) {
         return true;
      }
   }
}
