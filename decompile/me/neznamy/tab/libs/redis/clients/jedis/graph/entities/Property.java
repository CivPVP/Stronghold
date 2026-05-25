package me.neznamy.tab.libs.redis.clients.jedis.graph.entities;

import java.util.Objects;

@Deprecated
public class Property<T> {
   private final String name;
   private final T value;

   public Property(String name, T value) {
      this.name = name;
      this.value = value;
   }

   public String getName() {
      return this.name;
   }

   public T getValue() {
      return this.value;
   }

   private boolean valueEquals(Object value1, Object value2) {
      if (value1 instanceof Integer) {
         value1 = ((Integer)value1).longValue();
      }

      if (value2 instanceof Integer) {
         value2 = ((Integer)value2).longValue();
      }

      return Objects.equals(value1, value2);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof Property)) {
         return false;
      }

      Property<?> property = (Property<?>)o;
      return Objects.equals(this.name, property.name) && this.valueEquals(this.value, property.value);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.value);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("Property{");
      sb.append("name='").append(this.name).append('\'');
      sb.append(", value=").append(this.value);
      sb.append('}');
      return sb.toString();
   }
}
