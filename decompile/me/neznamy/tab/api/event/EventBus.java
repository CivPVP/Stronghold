package me.neznamy.tab.api.event;

import lombok.NonNull;

public interface EventBus {
   void register(@NonNull Object var1);

   <E extends TabEvent> void register(@NonNull Class<E> var1, @NonNull EventHandler<E> var2);

   void unregister(@NonNull Object var1);

   <E extends TabEvent> void unregister(@NonNull EventHandler<E> var1);
}
