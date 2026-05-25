package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

import java.util.Collection;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.tools.json.JSONUtil;

public class ParameterDescription {
   private String name;
   private String type;

   public ParameterDescription() {
   }

   public ParameterDescription(Map<String, Object> pm) {
      JSONUtil.tryFill(this, pm);
   }

   public ParameterDescription(int index, Class<?> c) {
      this.name = "param" + index;
      this.type = lookup(c);
   }

   public static String lookup(Class<?> c) {
      if (c == Void.class) {
         return "nil";
      } else if (c == Boolean.class) {
         return "bit";
      } else if (c == Integer.class) {
         return "num";
      } else if (c == Double.class) {
         return "num";
      } else if (c == String.class) {
         return "str";
      } else if (c.isArray()) {
         return "arr";
      } else if (Map.class.isAssignableFrom(c)) {
         return "obj";
      } else {
         return Collection.class.isAssignableFrom(c) ? "arr" : "any";
      }
   }

   public String getName() {
      return this.name;
   }

   public String getType() {
      return this.type;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setType(String type) {
      this.type = type;
   }
}
