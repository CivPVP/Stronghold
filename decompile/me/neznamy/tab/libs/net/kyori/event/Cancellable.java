package me.neznamy.tab.libs.net.kyori.event;

public interface Cancellable {
   boolean cancelled();

   void cancelled(final boolean cancelled);

   abstract class Impl implements Cancellable {
      protected boolean cancelled;

      @Override
      public boolean cancelled() {
         return this.cancelled;
      }

      @Override
      public void cancelled(final boolean cancelled) {
         this.cancelled = cancelled;
      }
   }
}
