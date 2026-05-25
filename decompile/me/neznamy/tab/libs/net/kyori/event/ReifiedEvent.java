package me.neznamy.tab.libs.net.kyori.event;

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ReifiedEvent<T> {
   @NonNull TypeToken<T> type();
}
