package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

public interface JsonRpcMapper {
   JsonRpcMapper.JsonRpcRequest parse(String var1, ServiceDescription var2);

   JsonRpcMapper.JsonRpcResponse parse(String var1, Class<?> var2);

   String write(Object var1);

   class JsonRpcRequest {
      private final Object id;
      private final String version;
      private final String method;
      private final Object[] parameters;

      public JsonRpcRequest(Object id, String version, String method, Object[] parameters) {
         this.id = id;
         this.version = version;
         this.method = method;
         this.parameters = parameters;
      }

      public Object getId() {
         return this.id;
      }

      public String getVersion() {
         return this.version;
      }

      public String getMethod() {
         return this.method;
      }

      public Object[] getParameters() {
         return this.parameters;
      }

      public boolean isSystem() {
         return this.method.startsWith("system.");
      }

      public boolean isSystemDescribe() {
         return "system.describe".equals(this.method);
      }
   }

   class JsonRpcResponse {
      private final Object result;
      private final Object error;
      private final JsonRpcException exception;

      public JsonRpcResponse(Object result, Object error, JsonRpcException exception) {
         this.result = result;
         this.error = error;
         this.exception = exception;
      }

      public Object getError() {
         return this.error;
      }

      public Object getResult() {
         return this.result;
      }

      public JsonRpcException getException() {
         return this.exception;
      }
   }
}
