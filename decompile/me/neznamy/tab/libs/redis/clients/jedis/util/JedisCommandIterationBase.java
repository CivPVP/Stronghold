package me.neznamy.tab.libs.redis.clients.jedis.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;

public abstract class JedisCommandIterationBase<B, D> {
   private final Builder<B> builder;
   private final Queue<Entry> connections;
   private Entry connection;
   private B lastReply;
   private boolean roundRobinCompleted;
   private boolean iterationCompleted;

   protected JedisCommandIterationBase(ConnectionProvider connectionProvider, Builder<B> responseBuilder) {
      Map connectionMap = connectionProvider.getConnectionMap();
      ArrayList<Entry> connectionList = new ArrayList<>(connectionMap.entrySet());
      Collections.shuffle(connectionList);
      this.connections = new LinkedList<>(connectionList);
      this.builder = responseBuilder;
      this.iterationCompleted = true;
      this.roundRobinCompleted = this.connections.isEmpty();
   }

   public final boolean isIterationCompleted() {
      return this.roundRobinCompleted;
   }

   protected abstract boolean isNodeCompleted(B var1);

   protected abstract CommandArguments initCommandArguments();

   protected abstract CommandArguments nextCommandArguments(B var1);

   public final B nextBatch() {
      if (this.roundRobinCompleted) {
         throw new NoSuchElementException();
      }

      CommandArguments args;
      if (this.iterationCompleted) {
         this.connection = this.connections.poll();
         args = this.initCommandArguments();
      } else {
         args = this.nextCommandArguments(this.lastReply);
      }

      Object rawReply;
      if (this.connection.getValue() instanceof Connection) {
         rawReply = ((Connection)this.connection.getValue()).executeCommand(args);
      } else {
         if (!(this.connection.getValue() instanceof Pool)) {
            throw new IllegalArgumentException(this.connection.getValue().getClass() + "is not supported.");
         }

         try (Connection c = (Connection)((Pool)this.connection.getValue()).getResource()) {
            rawReply = c.executeCommand(args);
         }
      }

      this.lastReply = this.builder.build(rawReply);
      this.iterationCompleted = this.isNodeCompleted(this.lastReply);
      if (this.iterationCompleted && this.connections.isEmpty()) {
         this.roundRobinCompleted = true;
      }

      return this.lastReply;
   }

   protected abstract Collection<D> convertBatchToData(B var1);

   public final Collection<D> nextBatchList() {
      return this.convertBatchToData(this.nextBatch());
   }

   public final Collection<D> collect(Collection<D> c) {
      while (!this.isIterationCompleted()) {
         c.addAll(this.nextBatchList());
      }

      return c;
   }
}
