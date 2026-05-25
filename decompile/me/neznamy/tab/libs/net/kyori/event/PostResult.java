package me.neznamy.tab.libs.net.kyori.event;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PostResult {
   private static final PostResult SUCCESS = new PostResult(Collections.emptyMap());
   private final Map<EventSubscriber<?>, Throwable> exceptions;

   public static @NonNull PostResult success() {
      return SUCCESS;
   }

   public static @NonNull PostResult failure(final @NonNull Map<EventSubscriber<?>, Throwable> exceptions) {
      Preconditions.checkState(!exceptions.isEmpty(), "no exceptions present");
      return new PostResult(ImmutableMap.copyOf(exceptions));
   }

   private PostResult(final @NonNull Map<EventSubscriber<?>, Throwable> exceptions) {
      this.exceptions = exceptions;
   }

   public boolean wasSuccessful() {
      return this.exceptions.isEmpty();
   }

   public @NonNull Map<EventSubscriber<?>, Throwable> exceptions() {
      return this.exceptions;
   }

   public void raise() throws PostResult.CompositeException {
      if (!this.wasSuccessful()) {
         throw new PostResult.CompositeException(this);
      }
   }

   @Override
   public String toString() {
      return this.wasSuccessful()
         ? MoreObjects.toStringHelper(this).add("type", "success").toString()
         : MoreObjects.toStringHelper(this).add("type", "failure").add("exceptions", this.exceptions().values()).toString();
   }

   public static final class CompositeException extends Exception {
      private final PostResult result;

      CompositeException(final @NonNull PostResult result) {
         super("Exceptions occurred whilst posting to subscribers");
         this.result = result;
      }

      public @NonNull PostResult result() {
         return this.result;
      }

      public void printAllStackTraces() {
         this.printStackTrace();

         for (Throwable exception : this.result.exceptions().values()) {
            exception.printStackTrace();
         }
      }
   }
}
