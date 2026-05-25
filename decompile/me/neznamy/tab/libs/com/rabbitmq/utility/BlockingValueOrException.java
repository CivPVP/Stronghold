package me.neznamy.tab.libs.com.rabbitmq.utility;

import java.util.concurrent.TimeoutException;

public class BlockingValueOrException<V, E extends Throwable & SensibleClone<E>> extends BlockingCell<ValueOrException<V, E>> {
   public void setValue(V v) {
      super.set(ValueOrException.makeValue(v));
   }

   public void setException(E e) {
      super.set(ValueOrException.makeException(e));
   }

   public V uninterruptibleGetValue() throws E {
      return this.uninterruptibleGet().getValue();
   }

   public V uninterruptibleGetValue(int timeout) throws E, TimeoutException {
      return this.uninterruptibleGet(timeout).getValue();
   }
}
