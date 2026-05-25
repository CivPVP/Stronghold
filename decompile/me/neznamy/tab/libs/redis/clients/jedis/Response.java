package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.function.Supplier;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;

public class Response<T> implements Supplier<T> {
   protected T response = (T)null;
   protected JedisDataException exception = null;
   private boolean building = false;
   private boolean built = false;
   private boolean set = false;
   private Builder<T> builder;
   private Object data;
   private Response<?> dependency = null;

   public Response(Builder<T> b) {
      this.builder = b;
   }

   public void set(Object data) {
      this.data = data;
      this.set = true;
   }

   @Override
   public T get() {
      if (this.dependency != null && this.dependency.set && !this.dependency.built) {
         this.dependency.build();
      }

      if (!this.set) {
         throw new IllegalStateException("Please close pipeline or multi block before calling this method.");
      }

      if (!this.built) {
         this.build();
      }

      if (this.exception != null) {
         throw this.exception;
      } else {
         return this.response;
      }
   }

   public void setDependency(Response<?> dependency) {
      this.dependency = dependency;
   }

   private void build() {
      if (!this.building) {
         this.building = true;

         try {
            if (this.data != null) {
               if (this.data instanceof JedisDataException) {
                  this.exception = (JedisDataException)this.data;
               } else {
                  this.response = this.builder.build(this.data);
               }
            }

            this.data = null;
         } finally {
            this.building = false;
            this.built = true;
         }
      }
   }

   @Override
   public String toString() {
      return "Response " + this.builder.toString();
   }
}
