package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

public class JsonRpcException extends Exception {
   private static final long serialVersionUID = 1L;
   private final String name;
   private final int code;
   private final String message;
   private final Object error;

   public JsonRpcException() {
      this.name = null;
      this.code = -1;
      this.message = null;
      this.error = null;
   }

   public JsonRpcException(String detailMessage, String name, int code, String message, Object error) {
      super(detailMessage);
      this.name = name;
      this.code = code;
      this.message = message;
      this.error = error;
   }

   public String getName() {
      return this.name;
   }

   public int getCode() {
      return this.code;
   }

   @Override
   public String getMessage() {
      return this.message;
   }

   public Object getError() {
      return this.error;
   }
}
