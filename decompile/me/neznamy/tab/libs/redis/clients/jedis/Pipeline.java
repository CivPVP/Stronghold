package me.neznamy.tab.libs.redis.clients.jedis;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import me.neznamy.tab.libs.redis.clients.jedis.commands.DatabasePipelineCommands;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.graph.GraphCommandObjects;
import me.neznamy.tab.libs.redis.clients.jedis.params.MigrateParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class Pipeline extends PipelineBase implements DatabasePipelineCommands, Closeable {
   private final Queue<Response<?>> pipelinedResponses = new LinkedList<>();
   protected final Connection connection;
   private final boolean closeConnection;

   public Pipeline(Jedis jedis) {
      this(jedis.getConnection(), false);
   }

   public Pipeline(Connection connection) {
      this(connection, false);
   }

   public Pipeline(Connection connection, boolean closeConnection) {
      this(connection, closeConnection, createCommandObjects(connection));
   }

   private static CommandObjects createCommandObjects(Connection connection) {
      CommandObjects commandObjects = new CommandObjects();
      RedisProtocol proto = connection.getRedisProtocol();
      if (proto != null) {
         commandObjects.setProtocol(proto);
      }

      return commandObjects;
   }

   Pipeline(Connection connection, boolean closeConnection, CommandObjects commandObjects) {
      super(commandObjects);
      this.connection = connection;
      this.closeConnection = closeConnection;
      GraphCommandObjects graphCommandObjects = new GraphCommandObjects(this.connection);
      graphCommandObjects.setBaseCommandArgumentsCreator(protocolCommand -> commandObjects.commandArguments(protocolCommand));
      this.setGraphCommands(graphCommandObjects);
   }

   @Override
   public final <T> Response<T> appendCommand(CommandObject<T> commandObject) {
      this.connection.sendCommand(commandObject.getArguments());
      Response<T> response = new Response<>(commandObject.getBuilder());
      this.pipelinedResponses.add(response);
      return response;
   }

   @Override
   public void close() {
      try {
         this.sync();
      } finally {
         if (this.closeConnection) {
            IOUtils.closeQuietly(this.connection);
         }
      }
   }

   @Override
   public void sync() {
      if (this.hasPipelinedResponse()) {
         for (Object rawReply : this.connection.getMany(this.pipelinedResponses.size())) {
            this.pipelinedResponses.poll().set(rawReply);
         }
      }
   }

   public List<Object> syncAndReturnAll() {
      if (!this.hasPipelinedResponse()) {
         return Collections.emptyList();
      }

      List<Object> unformatted = this.connection.getMany(this.pipelinedResponses.size());
      List<Object> formatted = new ArrayList<>();

      for (Object rawReply : unformatted) {
         try {
            Response<?> response = this.pipelinedResponses.poll();
            response.set(rawReply);
            formatted.add(response.get());
         } catch (JedisDataException e) {
            formatted.add(e);
         }
      }

      return formatted;
   }

   public final boolean hasPipelinedResponse() {
      return this.pipelinedResponses.size() > 0;
   }

   public Response<Long> waitReplicas(int replicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitReplicas(replicas, timeout));
   }

   public Response<KeyValue<Long, Long>> waitAOF(long numLocal, long numReplicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitAOF(numLocal, numReplicas, timeout));
   }

   public Response<List<String>> time() {
      return this.appendCommand(new CommandObject<>(this.commandObjects.commandArguments(Protocol.Command.TIME), BuilderFactory.STRING_LIST));
   }

   @Override
   public Response<String> select(int index) {
      return this.appendCommand(new CommandObject<>(this.commandObjects.commandArguments(Protocol.Command.SELECT).add(index), BuilderFactory.STRING));
   }

   @Override
   public Response<Long> dbSize() {
      return this.appendCommand(new CommandObject<>(this.commandObjects.commandArguments(Protocol.Command.DBSIZE), BuilderFactory.LONG));
   }

   @Override
   public Response<String> swapDB(int index1, int index2) {
      return this.appendCommand(
         new CommandObject<>(this.commandObjects.commandArguments(Protocol.Command.SWAPDB).add(index1).add(index2), BuilderFactory.STRING)
      );
   }

   @Override
   public Response<Long> move(String key, int dbIndex) {
      return this.appendCommand(new CommandObject<>(this.commandObjects.commandArguments(Protocol.Command.MOVE).key(key).add(dbIndex), BuilderFactory.LONG));
   }

   @Override
   public Response<Long> move(byte[] key, int dbIndex) {
      return this.appendCommand(new CommandObject<>(this.commandObjects.commandArguments(Protocol.Command.MOVE).key(key).add(dbIndex), BuilderFactory.LONG));
   }

   @Override
   public Response<Boolean> copy(String srcKey, String dstKey, int db, boolean replace) {
      return this.appendCommand(this.commandObjects.copy(srcKey, dstKey, db, replace));
   }

   @Override
   public Response<Boolean> copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
      return this.appendCommand(this.commandObjects.copy(srcKey, dstKey, db, replace));
   }

   @Override
   public Response<String> migrate(String host, int port, byte[] key, int destinationDB, int timeout) {
      return this.appendCommand(this.commandObjects.migrate(host, port, key, destinationDB, timeout));
   }

   @Override
   public Response<String> migrate(String host, int port, String key, int destinationDB, int timeout) {
      return this.appendCommand(this.commandObjects.migrate(host, port, key, destinationDB, timeout));
   }

   @Override
   public Response<String> migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
      return this.appendCommand(this.commandObjects.migrate(host, port, destinationDB, timeout, params, keys));
   }

   @Override
   public Response<String> migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
      return this.appendCommand(this.commandObjects.migrate(host, port, destinationDB, timeout, params, keys));
   }
}
