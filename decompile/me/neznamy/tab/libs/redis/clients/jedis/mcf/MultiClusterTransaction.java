package me.neznamy.tab.libs.redis.clients.jedis.mcf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObjects;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.RedisProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.Response;
import me.neznamy.tab.libs.redis.clients.jedis.TransactionBase;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Experimental;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.graph.ResultSet;
import me.neznamy.tab.libs.redis.clients.jedis.providers.MultiClusterPooledConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

@Experimental
public class MultiClusterTransaction extends TransactionBase {
   private static final Builder<?> NO_OP_BUILDER = BuilderFactory.RAW_OBJECT;
   private static final String GRAPH_COMMANDS_NOT_SUPPORTED_MESSAGE = "Graph commands are not supported.";
   private final CircuitBreakerFailoverConnectionProvider failoverProvider;
   private final AtomicInteger extraCommandCount = new AtomicInteger();
   private final Queue<KeyValue<CommandArguments, Response<?>>> commands = new LinkedList<>();
   private boolean inWatch = false;
   private boolean inMulti = false;

   @Deprecated
   public MultiClusterTransaction(MultiClusterPooledConnectionProvider provider) {
      this(provider, true);
   }

   @Deprecated
   public MultiClusterTransaction(MultiClusterPooledConnectionProvider provider, boolean doMulti) {
      this.failoverProvider = new CircuitBreakerFailoverConnectionProvider(provider);

      try (Connection connection = this.failoverProvider.getConnection()) {
         RedisProtocol proto = connection.getRedisProtocol();
         if (proto != null) {
            this.commandObjects.setProtocol(proto);
         }
      }

      if (doMulti) {
         this.multi();
      }
   }

   public MultiClusterTransaction(MultiClusterPooledConnectionProvider provider, boolean doMulti, CommandObjects commandObjects) {
      super(commandObjects);
      this.failoverProvider = new CircuitBreakerFailoverConnectionProvider(provider);
      if (doMulti) {
         this.multi();
      }
   }

   @Override
   public final void multi() {
      this.appendCommand(new CommandObject<>(new CommandArguments(Protocol.Command.MULTI), NO_OP_BUILDER));
      this.extraCommandCount.incrementAndGet();
      this.inMulti = true;
   }

   @Override
   public final String watch(String... keys) {
      this.appendCommand(this.commandObjects.watch(keys));
      this.extraCommandCount.incrementAndGet();
      this.inWatch = true;
      return null;
   }

   @Override
   public final String watch(byte[]... keys) {
      this.appendCommand(this.commandObjects.watch(keys));
      this.extraCommandCount.incrementAndGet();
      this.inWatch = true;
      return null;
   }

   @Override
   public final String unwatch() {
      this.appendCommand(new CommandObject<>(new CommandArguments(Protocol.Command.UNWATCH), NO_OP_BUILDER));
      this.extraCommandCount.incrementAndGet();
      this.inWatch = false;
      return null;
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
      this.clear();
   }

   private void clear() {
      if (this.inMulti) {
         this.discard();
      } else if (this.inWatch) {
         this.unwatch();
      }
   }

   @Override
   public final List<Object> exec() {
      if (!this.inMulti) {
         throw new IllegalStateException("EXEC without MULTI");
      }

      try (Connection connection = this.failoverProvider.getConnection()) {
         this.commands.forEach(command -> connection.sendCommand(command.getKey()));
         connection.getMany(this.commands.size());

         for (int idx = 0; idx < this.extraCommandCount.get(); idx++) {
            this.commands.poll();
         }

         connection.sendCommand(Protocol.Command.EXEC);
         List<Object> unformatted = connection.getObjectMultiBulkReply();
         if (unformatted == null) {
            this.commands.clear();
            return null;
         }

         List<Object> formatted = new ArrayList<>(unformatted.size() - this.extraCommandCount.get());

         for (Object rawReply : unformatted) {
            try {
               Response<?> response = this.commands.poll().getValue();
               response.set(rawReply);
               formatted.add(response.get());
            } catch (JedisDataException e) {
               formatted.add(e);
            }
         }

         return formatted;
      } finally {
         this.inMulti = false;
         this.inWatch = false;
      }
   }

   @Override
   public final String discard() {
      if (!this.inMulti) {
         throw new IllegalStateException("DISCARD without MULTI");
      }

      try (Connection connection = this.failoverProvider.getConnection()) {
         this.commands.forEach(command -> connection.sendCommand(command.getKey()));
         connection.getMany(this.commands.size());
         connection.sendCommand(Protocol.Command.DISCARD);
         return connection.getStatusCodeReply();
      } finally {
         this.inMulti = false;
         this.inWatch = false;
      }
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
