package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.MethodArgumentReader;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.MethodArgumentWriter;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ValueReader;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.ValueWriter;

public class MapRpcServer extends RpcServer {
   public MapRpcServer(Channel channel) throws IOException {
      super(channel);
   }

   public MapRpcServer(Channel channel, String queueName) throws IOException {
      super(channel, queueName);
   }

   @Override
   public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
      try {
         return encode(this.handleMapCall(decode(requestBody), replyProperties));
      } catch (IOException ioe) {
         return new byte[0];
      }
   }

   public static Map<String, Object> decode(byte[] requestBody) throws IOException {
      MethodArgumentReader reader = new MethodArgumentReader(new ValueReader(new DataInputStream(new ByteArrayInputStream(requestBody))));
      return reader.readTable();
   }

   public static byte[] encode(Map<String, Object> reply) throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      MethodArgumentWriter writer = new MethodArgumentWriter(new ValueWriter(new DataOutputStream(buffer)));
      writer.writeTable(reply);
      writer.flush();
      return buffer.toByteArray();
   }

   public Map<String, Object> handleMapCall(Map<String, Object> request, AMQP.BasicProperties replyProperties) {
      return this.handleMapCall(request);
   }

   public Map<String, Object> handleMapCall(Map<String, Object> request) {
      return new HashMap<>();
   }

   @Override
   public void handleCast(byte[] requestBody) {
      try {
         this.handleMapCast(decode(requestBody));
      } catch (IOException var3) {
      }
   }

   public void handleMapCast(Map<String, Object> requestBody) {
   }
}
