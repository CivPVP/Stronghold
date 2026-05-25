package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public class StringRpcServer extends RpcServer {
   public static final String STRING_ENCODING = "UTF-8";

   public StringRpcServer(Channel channel) throws IOException {
      super(channel);
   }

   public StringRpcServer(Channel channel, String queueName) throws IOException {
      super(channel, queueName);
   }

   @Override
   public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
      String request;
      try {
         request = new String(requestBody, "UTF-8");
      } catch (IOException _e) {
         request = new String(requestBody);
      }

      String reply = this.handleStringCall(request, replyProperties);

      try {
         return reply.getBytes("UTF-8");
      } catch (IOException _e) {
         return reply.getBytes();
      }
   }

   public String handleStringCall(String request, AMQP.BasicProperties replyProperties) {
      return this.handleStringCall(request);
   }

   public String handleStringCall(String request) {
      return "";
   }

   @Override
   public void handleCast(byte[] requestBody) {
      try {
         this.handleStringCast(new String(requestBody, "UTF-8"));
      } catch (IOException _e) {
         this.handleStringCast(new String(requestBody));
      }
   }

   public void handleStringCast(String requestBody) {
   }
}
