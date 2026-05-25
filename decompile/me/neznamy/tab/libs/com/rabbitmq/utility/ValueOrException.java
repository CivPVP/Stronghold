package me.neznamy.tab.libs.com.rabbitmq.utility;

public class ValueOrException<V, E extends Throwable & SensibleClone<E>> {
   private final boolean _useValue;
   private final V _value;
   private final E _exception;

   private ValueOrException(V value, E exception, boolean useValue) {
      this._useValue = useValue;
      if (useValue) {
         this._value = value;
         this._exception = null;
      } else {
         this._value = null;
         this._exception = exception;
      }
   }

   public static <V, E extends Throwable & SensibleClone<E>> ValueOrException<V, E> makeValue(V value) {
      return new ValueOrException<>(value, null, true);
   }

   public static <V, E extends Throwable & SensibleClone<E>> ValueOrException<V, E> makeException(E exception) {
      return new ValueOrException<>(null, exception, false);
   }

   public V getValue() throws E {
      if (this._useValue) {
         return this._value;
      } else {
         throw Utility.fixStackTrace(this._exception);
      }
   }
}
