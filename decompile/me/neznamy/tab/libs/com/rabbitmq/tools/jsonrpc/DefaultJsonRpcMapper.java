package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.tools.json.JSONReader;
import me.neznamy.tab.libs.com.rabbitmq.tools.json.JSONWriter;

/** @deprecated */
public class DefaultJsonRpcMapper implements JsonRpcMapper {
   @Override
   public JsonRpcMapper.JsonRpcRequest parse(String requestBody, ServiceDescription description) {
      Map<String, Object> request = (Map<String, Object>)new JSONReader().read(requestBody);
      return new JsonRpcMapper.JsonRpcRequest(
         request.get("id"), request.get("version").toString(), request.get("method").toString(), ((List)request.get("params")).toArray()
      );
   }

   @Override
   public JsonRpcMapper.JsonRpcResponse parse(String responseBody, Class<?> expectedType) {
      var map = (Map<String, Object> & Map)new JSONReader().read(responseBody);
      JsonRpcException exception = null;
      if (map.containsKey("error")) {
         Map<String, Object> error = (Map<String, Object>)map.get("error");
         exception = new JsonRpcException(
            new JSONWriter().write(error),
            (String)error.get("name"),
            error.get("code") == null ? 0 : (Integer)error.get("code"),
            (String)error.get("message"),
            error
         );
      }

      return new JsonRpcMapper.JsonRpcResponse(map.get("result"), map.get("error"), exception);
   }

   @Override
   public String write(Object input) {
      return new JSONWriter().write(input);
   }
}
