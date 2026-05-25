package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.graph.GraphCommandObjects;

public class ReliableTransaction extends TransactionBase {
   private static final String QUEUED_STR = "QUEUED";
   private final Queue<Response<?>> pipelinedResponses = new LinkedList<>();
   protected final Connection connection;
   private final boolean closeConnection;
   private boolean broken = false;
   private boolean inWatch = false;
   private boolean inMulti = false;

   public ReliableTransaction(Connection connection) {
      this(connection, true);
   }

   public ReliableTransaction(Connection connection, boolean doMulti) {
      this(connection, doMulti, false);
   }

   public ReliableTransaction(Connection connection, boolean doMulti, boolean closeConnection) {
      this(connection, doMulti, closeConnection, createCommandObjects(connection));
   }

   ReliableTransaction(Connection connection, boolean doMulti, boolean closeConnection, CommandObjects commandObjects) {
      super(commandObjects);
      this.connection = connection;
      this.closeConnection = closeConnection;
      GraphCommandObjects graphCommandObjects = new GraphCommandObjects(this.connection);
      graphCommandObjects.setBaseCommandArgumentsCreator(protocolCommand -> commandObjects.commandArguments(protocolCommand));
      this.setGraphCommands(graphCommandObjects);
      if (doMulti) {
         this.multi();
      }
   }

   private static CommandObjects createCommandObjects(Connection connection) {
      CommandObjects commandObjects = new CommandObjects();
      RedisProtocol proto = connection.getRedisProtocol();
      if (proto != null) {
         commandObjects.setProtocol(proto);
      }

      return commandObjects;
   }

   @Override
   public final void multi() {
      this.connection.sendCommand(Protocol.Command.MULTI);
      String status = this.connection.getStatusCodeReply();
      if (!"OK".equals(status)) {
         throw new JedisException("MULTI command failed. Received response: " + status);
      }

      this.inMulti = true;
   }

   @Override
   public String watch(String... keys) {
      String status = this.connection.executeCommand(this.commandObjects.watch(keys));
      this.inWatch = true;
      return status;
   }

   @Override
   public String watch(byte[]... keys) {
      String status = this.connection.executeCommand(this.commandObjects.watch(keys));
      this.inWatch = true;
      return status;
   }

   @Override
   public String unwatch() {
      this.connection.sendCommand(Protocol.Command.UNWATCH);
      String status = this.connection.getStatusCodeReply();
      this.inWatch = false;
      return status;
   }

   @Override
   protected final <T> Response<T> appendCommand(CommandObject<T> commandObject) {
      this.connection.sendCommand(commandObject.getArguments());
      String status = this.connection.getStatusCodeReply();
      if (!"QUEUED".equals(status)) {
         throw new JedisException(status);
      }

      Response<T> response = new Response<>(commandObject.getBuilder());
      this.pipelinedResponses.add(response);
      return response;
   }

   @Override
   public final void close() {
      try {
         this.clear();
      } finally {
         if (this.closeConnection) {
            this.connection.close();
         }
      }
   }

   @Deprecated
   public final void clear() {
      if (!this.broken) {
         if (this.inMulti) {
            this.discard();
         } else if (this.inWatch) {
            this.unwatch();
         }
      }
   }

   @Override
   public List<Object> exec() {
      if (!this.inMulti) {
         throw new IllegalStateException("EXEC without MULTI");
      }

      try {
         this.connection.sendCommand(Protocol.Command.EXEC);
         List<Object> unformatted = this.connection.getObjectMultiBulkReply();
         if (unformatted == null) {
            this.pipelinedResponses.clear();
            return null;
         }

         List<Object> formatted = new ArrayList<>(unformatted.size());

         for (Object o : unformatted) {
            try {
               Response<?> response = this.pipelinedResponses.poll();
               response.set(o);
               formatted.add(response.get());
            } catch (JedisDataException e) {
               formatted.add(e);
            }
         }

         return formatted;
      } catch (JedisConnectionException jce) {
         this.broken = true;
         throw jce;
      } finally {
         this.inMulti = false;
         this.inWatch = false;
         this.pipelinedResponses.clear();
      }
   }

   @Override
   public String discard() {
      if (!this.inMulti) {
         throw new IllegalStateException("DISCARD without MULTI");
      }

      try {
         this.connection.sendCommand(Protocol.Command.DISCARD);
         String status = this.connection.getStatusCodeReply();
         if (!"OK".equals(status)) {
            throw new JedisException("DISCARD command failed. Received response: " + status);
         } else {
            return status;
         }
      } catch (JedisConnectionException jce) {
         this.broken = true;
         throw jce;
      } finally {
         this.inMulti = false;
         this.inWatch = false;
         this.pipelinedResponses.clear();
      }
   }
}
