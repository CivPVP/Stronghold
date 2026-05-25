package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.tools.json.JSONUtil;

public class ProcedureDescription {
   private String name;
   private String summary;
   private String help;
   private boolean idempotent;
   private ParameterDescription[] params;
   private String returnType;
   private String javaReturnType;
   private Class<?> _javaReturnTypeAsClass;
   private Method method;

   public ProcedureDescription(Map<String, Object> pm) {
      JSONUtil.tryFill(this, pm);
      List<Map<String, Object>> p = (List<Map<String, Object>>)pm.get("params");
      this.params = new ParameterDescription[p.size()];
      int count = 0;

      for (Map<String, Object> param_map : p) {
         ParameterDescription param = new ParameterDescription(param_map);
         this.params[count++] = param;
      }
   }

   public ProcedureDescription(Method m) {
      this.method = m;
      this.name = m.getName();
      this.summary = "";
      this.help = "";
      this.idempotent = false;
      Class<?>[] parameterTypes = m.getParameterTypes();
      this.params = new ParameterDescription[parameterTypes.length];

      for (int i = 0; i < parameterTypes.length; i++) {
         this.params[i] = new ParameterDescription(i, parameterTypes[i]);
      }

      this.returnType = ParameterDescription.lookup(m.getReturnType());
      this.javaReturnType = m.getReturnType().getName();
   }

   public ProcedureDescription() {
   }

   public String getReturn() {
      return this.returnType;
   }

   public void setReturn(String value) {
      this.returnType = value;
   }

   public Method internal_getMethod() {
      return this.method;
   }

   public String getJavaReturnType() {
      return this.javaReturnType;
   }

   public void setJavaReturnType(String javaReturnType) {
      this.javaReturnType = javaReturnType;
      this._javaReturnTypeAsClass = this.computeReturnTypeAsJavaClass();
   }

   public Class<?> getReturnType() {
      return this._javaReturnTypeAsClass;
   }

   private Class<?> computeReturnTypeAsJavaClass() {
      try {
         if ("int".equals(this.javaReturnType)) {
            return int.class;
         } else if ("double".equals(this.javaReturnType)) {
            return double.class;
         } else if ("long".equals(this.javaReturnType)) {
            return long.class;
         } else if ("boolean".equals(this.javaReturnType)) {
            return boolean.class;
         } else if ("char".equals(this.javaReturnType)) {
            return char.class;
         } else if ("byte".equals(this.javaReturnType)) {
            return byte.class;
         } else if ("short".equals(this.javaReturnType)) {
            return short.class;
         } else if ("float".equals(this.javaReturnType)) {
            return float.class;
         } else {
            return "void".equals(this.javaReturnType) ? void.class : Class.forName(this.javaReturnType);
         }
      } catch (ClassNotFoundException e) {
         throw new IllegalStateException("Unable to load class: " + this.javaReturnType, e);
      }
   }

   public ParameterDescription[] internal_getParams() {
      return this.params;
   }

   public int arity() {
      return this.params == null ? 0 : this.params.length;
   }

   public ParameterDescription[] getParams() {
      return this.params;
   }

   public String getName() {
      return this.name;
   }

   public String getSummary() {
      return this.summary;
   }

   public String getHelp() {
      return this.help;
   }

   public boolean isIdempotent() {
      return this.idempotent;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setSummary(String summary) {
      this.summary = summary;
   }

   public void setHelp(String help) {
      this.help = help;
   }

   public void setIdempotent(boolean idempotent) {
      this.idempotent = idempotent;
   }
}
