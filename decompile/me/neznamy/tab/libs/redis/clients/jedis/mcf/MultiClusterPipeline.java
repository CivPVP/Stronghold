package me.neznamy.tab.libs.redis.clients.jedis.mcf;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObjects;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.PipelineBase;
import me.neznamy.tab.libs.redis.clients.jedis.RedisProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.graph.ResultSet;
import me.neznamy.tab.libs.redis.clients.jedis.providers.MultiClusterPooledConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

@Experimental
public class MultiClusterPipeline extends PipelineBase implements Closeable {
   private final CircuitBreakerFailoverConnectionProvider failoverProvider;
   private final Queue<KeyValue<CommandArguments, Response<?>>> commands = new LinkedList<>();

   @Deprecated
   public MultiClusterPipeline(MultiClusterPooledConnectionProvider pooledProvider) {
      super(new CommandObjects());
      this.failoverProvider = new CircuitBreakerFailoverConnectionProvider(pooledProvider);

      try (Connection connection = this.failoverProvider.getConnection()) {
         RedisProtocol proto = connection.getRedisProtocol();
         if (proto != null) {
            this.commandObjects.setProtocol(proto);
         }
      }
   }

   public MultiClusterPipeline(MultiClusterPooledConnectionProvider pooledProvider, CommandObjects commandObjects) {
      super(commandObjects);
      this.failoverProvider = new CircuitBreakerFailoverConnectionProvider(pooledProvider);
   }

   @Override
   protected final <T> Response<T> appendCommand(CommandObject<T> commandObject) {
      CommandArguments args = commandObject.getArguments();
      Response<T> response = new Response<>(commandObject.getBuilder());
      this.commands.add(KeyValue.of(args, response));
      return response;
   }

   @Override
   public void close() {
      this.sync();
   }

   @Override
   public void sync() {
      if (!this.commands.isEmpty()) {
         try (Connection connection = this.failoverProvider.getConnection()) {
            this.commands.forEach(command -> connection.sendCommand(command.getKey()));
            List<Object> unformatted = connection.getMany(this.commands.size());
            unformatted.forEach(rawReply -> this.commands.poll().getValue().set(rawReply));
         }
      }
   }

   public Response<Long> waitReplicas(int replicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitReplicas(replicas, timeout));
   }

   public Response<KeyValue<Long, Long>> waitAOF(long numLocal, long numReplicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitAOF(numLocal, numReplicas, timeout));
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query, long timeout) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query, long timeout) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query, Map<String, Object> params) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphQuery(String name, String query, Map<String, Object> params, long timeout) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params, long timeout) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<String> graphDelete(String name) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }

   @Override
   public Response<List<String>> graphProfile(String graphName, String query) {
      throw new UnsupportedOperationException("Graph commands are not supported.");
   }
}
