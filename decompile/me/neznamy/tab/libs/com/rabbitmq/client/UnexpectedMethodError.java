package me.neznamy.tab.libs.com.rabbitmq.client;

public class UnexpectedMethodError extends RuntimeException {
   private static final long serialVersionUID = 1L;
   private final Method _method;

   public UnexpectedMethodError(Method method) {
      this._method = method;
   }

   @Override
   public String toString() {
      return super.toString() + ": " + this._method;
   }

   public Method getMethod() {
      return this._method;
   }
}
