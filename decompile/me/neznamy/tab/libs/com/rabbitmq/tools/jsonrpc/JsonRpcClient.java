package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import me.neznamy.tab.libs.com.rabbitmq.client.Channel;
import me.neznamy.tab.libs.com.rabbitmq.client.RpcClient;
import me.neznamy.tab.libs.com.rabbitmq.client.RpcClientParams;
import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;
import me.neznamy.tab.libs.com.rabbitmq.tools.json.JSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcClient extends RpcClient implements InvocationHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(JsonRpcClient.class);
   private final JsonRpcMapper mapper;
   private ServiceDescription serviceDescription;

   public JsonRpcClient(RpcClientParams rpcClientParams, JsonRpcMapper mapper) throws IOException, JsonRpcException, TimeoutException {
      super(rpcClientParams);
      this.mapper = mapper;
      this.retrieveServiceDescription();
   }

   public JsonRpcClient(Channel channel, String exchange, String routingKey, int timeout, JsonRpcMapper mapper) throws IOException, JsonRpcException, TimeoutException {
      super(new RpcClientParams().channel(channel).exchange(exchange).routingKey(routingKey).timeout(timeout));
      this.mapper = mapper;
      this.retrieveServiceDescription();
   }

   public JsonRpcClient(Channel channel, String exchange, String routingKey, int timeout) throws IOException, JsonRpcException, TimeoutException {
      this(channel, exchange, routingKey, timeout, new DefaultJsonRpcMapper());
   }

   public JsonRpcClient(Channel channel, String exchange, String routingKey) throws IOException, JsonRpcException, TimeoutException {
      this(channel, exchange, routingKey, -1);
   }

   @Deprecated
   public static Object coerce(String val, String type) throws NumberFormatException {
      if ("bit".equals(type)) {
         return Boolean.getBoolean(val) ? Boolean.TRUE : Boolean.FALSE;
      }

      if ("num".equals(type)) {
         try {
            return Integer.valueOf(val);
         } catch (NumberFormatException nfe) {
            return Double.valueOf(val);
         }
      } else if ("str".equals(type)) {
         return val;
      } else if ("arr".equals(type) || "obj".equals(type) || "any".equals(type)) {
         return new JSONReader().read(val);
      } else if ("nil".equals(type)) {
         return null;
      } else {
         throw new IllegalArgumentException("Bad type: " + type);
      }
   }

   private Object checkReply(JsonRpcMapper.JsonRpcResponse reply) throws JsonRpcException {
      if (reply.getError() != null) {
         throw reply.getException();
      } else {
         return reply.getResult();
      }
   }

   public Object call(String method, Object[] params) throws IOException, JsonRpcException, TimeoutException {
      Map<String, Object> request = new HashMap<>();
      request.put("id", null);
      request.put("method", method);
      request.put("version", "1.1");
      params = params == null ? new Object[0] : params;
      request.put("params", params);
      String requestStr = this.mapper.write(request);

      try {
         String replyStr = this.stringCall(requestStr);
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Reply string: {}", replyStr);
         }

         Class<?> expectedType;
         if ("system.describe".equals(method) && params.length == 0) {
            expectedType = Map.class;
         } else {
            ProcedureDescription proc = this.serviceDescription.getProcedure(method, params.length);
            expectedType = proc.getReturnType();
         }

         JsonRpcMapper.JsonRpcResponse reply = this.mapper.parse(replyStr, expectedType);
         return this.checkReply(reply);
      } catch (ShutdownSignalException ex) {
         throw new IOException(ex.getMessage());
      }
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return this.call(method.getName(), args);
   }

   public <T> T createProxy(Class<T> klass) throws IllegalArgumentException {
      return (T)Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, this);
   }

   @Deprecated
   public Object call(String[] args) throws NumberFormatException, IOException, JsonRpcException, TimeoutException {
      if (args.length == 0) {
         throw new IllegalArgumentException("First string argument must be method name");
      }

      String method = args[0];
      int arity = args.length - 1;
      ProcedureDescription proc = this.serviceDescription.getProcedure(method, arity);
      ParameterDescription[] params = proc.getParams();
      Object[] actuals = new Object[arity];

      for (int count = 0; count < params.length; count++) {
         actuals[count] = coerce(args[count + 1], params[count].getType());
      }

      return this.call(method, actuals);
   }

   public ServiceDescription getServiceDescription() {
      return this.serviceDescription;
   }

   private void retrieveServiceDescription() throws IOException, JsonRpcException, TimeoutException {
      Map<String, Object> rawServiceDescription = (Map<String, Object>)this.call("system.describe", null);
      this.serviceDescription = new ServiceDescription(rawServiceDescription);
   }
}
