package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.AMQP;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.StringRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcServer extends StringRpcServer {
   private static final Logger LOGGER = LoggerFactory.getLogger(JsonRpcServer.class);
   private final JsonRpcMapper mapper;
   private ServiceDescription serviceDescription;
   private Object interfaceInstance;

   public JsonRpcServer(Channel channel, Class<?> interfaceClass, Object interfaceInstance, JsonRpcMapper mapper) throws IOException {
      super(channel);
      this.mapper = mapper;
      this.init(interfaceClass, interfaceInstance);
   }

   public JsonRpcServer(Channel channel, Class<?> interfaceClass, Object interfaceInstance) throws IOException {
      this(channel, interfaceClass, interfaceInstance, new DefaultJsonRpcMapper());
   }

   public JsonRpcServer(Channel channel, String queueName, Class<?> interfaceClass, Object interfaceInstance, JsonRpcMapper mapper) throws IOException {
      super(channel, queueName);
      this.mapper = mapper;
      this.init(interfaceClass, interfaceInstance);
   }

   public JsonRpcServer(Channel channel, String queueName, Class<?> interfaceClass, Object interfaceInstance) throws IOException {
      this(channel, queueName, interfaceClass, interfaceInstance, new DefaultJsonRpcMapper());
   }

   private void init(Class<?> interfaceClass, Object interfaceInstance) {
      this.interfaceInstance = interfaceInstance;
      this.serviceDescription = new ServiceDescription(interfaceClass);
   }

   @Override
   public String handleStringCall(String requestBody, AMQP.BasicProperties replyProperties) {
      return this.doCall(requestBody);
   }

   public String doCall(String requestBody) {
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Request: {}", requestBody);
      }

      String response;
      try {
         JsonRpcMapper.JsonRpcRequest request = this.mapper.parse(requestBody, this.serviceDescription);
         if (request == null) {
            response = this.errorResponse(null, 400, "Bad Request", null);
         } else if (!"1.1".equals(request.getVersion())) {
            response = this.errorResponse(null, 505, "JSONRPC version not supported", null);
         } else {
            Object id = request.getId();
            String method = request.getMethod();
            Object[] params = request.getParameters();
            if (request.isSystemDescribe()) {
               response = this.resultResponse(id, this.serviceDescription);
            } else if (request.isSystem()) {
               response = this.errorResponse(id, 403, "System methods forbidden", null);
            } else {
               try {
                  Method matchingMethod = this.matchingMethod(method, params);
                  if (LOGGER.isDebugEnabled()) {
                     Collection<String> parametersValuesAndTypes = new ArrayList<>();
                     if (params != null) {
                        for (Object param : params) {
                           parametersValuesAndTypes.add(String.format("%s (%s)", param, param == null ? "?" : param.getClass()));
                        }
                     }

                     LOGGER.debug("About to invoke {} method with parameters {}", matchingMethod, parametersValuesAndTypes);
                  }

                  Object result = matchingMethod.invoke(this.interfaceInstance, params);
                  if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Invocation returned {} ({})", result, result == null ? "?" : result.getClass());
                  }

                  response = this.resultResponse(id, result);
               } catch (Throwable t) {
                  LOGGER.info("Error while processing JSON RPC request", t);
                  response = this.errorResponse(id, 500, "Internal Server Error", t);
               }
            }
         }
      } catch (ClassCastException cce) {
         response = this.errorResponse(null, 400, "Bad Request", null);
      }

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Response: {}", response);
      }

      return response;
   }

   public Method matchingMethod(String methodName, Object[] params) {
      ProcedureDescription proc = this.serviceDescription.getProcedure(methodName, params.length);
      return proc.internal_getMethod();
   }

   private String errorResponse(Object id, int code, String message, Object errorArg) {
      Map<String, Object> err = new HashMap<>();
      err.put("name", "JSONRPCError");
      err.put("code", code);
      err.put("message", message);
      err.put("error", errorArg);
      return this.response(id, "error", err);
   }

   private String resultResponse(Object id, Object result) {
      return this.response(id, "result", result);
   }

   private String response(Object id, String label, Object value) {
      Map<String, Object> resp = new HashMap<>();
      resp.put("version", "1.1");
      if (id != null) {
         resp.put("id", id);
      }

      resp.put(label, value);
      return this.mapper.write(resp);
   }

   public ServiceDescription getServiceDescription() {
      return this.serviceDescription;
   }
}
