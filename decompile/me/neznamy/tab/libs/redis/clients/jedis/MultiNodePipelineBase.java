package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.graph.GraphCommandObjects;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MultiNodePipelineBase extends PipelineBase {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   public static volatile int MULTI_NODE_PIPELINE_SYNC_WORKERS = 3;
   private final Map<HostAndPort, Queue<Response<?>>> pipelinedResponses;
   private final Map<HostAndPort, Connection> connections;
   private volatile boolean syncing = false;

   public MultiNodePipelineBase(CommandObjects commandObjects) {
      super(commandObjects);
      this.pipelinedResponses = new LinkedHashMap<>();
      this.connections = new LinkedHashMap<>();
   }

   protected final void prepareGraphCommands(ConnectionProvider connectionProvider) {
      GraphCommandObjects graphCommandObjects = new GraphCommandObjects(connectionProvider);
      graphCommandObjects.setBaseCommandArgumentsCreator(comm -> this.commandObjects.commandArguments(comm));
      super.setGraphCommands(graphCommandObjects);
   }

   protected abstract HostAndPort getNodeKey(CommandArguments var1);

   protected abstract Connection getConnection(HostAndPort var1);

   @Override
   protected final <T> Response<T> appendCommand(CommandObject<T> commandObject) {
      HostAndPort nodeKey = this.getNodeKey(commandObject.getArguments());
      Queue<Response<?>> queue;
      Connection connection;
      if (this.pipelinedResponses.containsKey(nodeKey)) {
         queue = this.pipelinedResponses.get(nodeKey);
         connection = this.connections.get(nodeKey);
      } else {
         Connection newOne = this.getConnection(nodeKey);
         this.connections.putIfAbsent(nodeKey, newOne);
         connection = this.connections.get(nodeKey);
         if (connection != newOne) {
            this.log.debug("Duplicate connection to {}, closing it.", nodeKey);
            IOUtils.closeQuietly(newOne);
         }

         this.pipelinedResponses.putIfAbsent(nodeKey, new LinkedList<>());
         queue = this.pipelinedResponses.get(nodeKey);
      }

      connection.sendCommand(commandObject.getArguments());
      Response<T> response = new Response<>(commandObject.getBuilder());
      queue.add(response);
      return response;
   }

   @Override
   public void close() {
      try {
         this.sync();
      } finally {
         this.connections.values().forEach(IOUtils::closeQuietly);
      }
   }

   @Override
   public final void sync() {
      if (!this.syncing) {
         this.syncing = true;
         ExecutorService executorService = Executors.newFixedThreadPool(MULTI_NODE_PIPELINE_SYNC_WORKERS);
         CountDownLatch countDownLatch = new CountDownLatch(this.pipelinedResponses.size());
         Iterator<Entry<HostAndPort, Queue<Response<?>>>> pipelinedResponsesIterator = this.pipelinedResponses.entrySet().iterator();

         while (pipelinedResponsesIterator.hasNext()) {
            Entry<HostAndPort, Queue<Response<?>>> entry = pipelinedResponsesIterator.next();
            HostAndPort nodeKey = entry.getKey();
            Queue<Response<?>> queue = entry.getValue();
            Connection connection = this.connections.get(nodeKey);
            executorService.submit(() -> {
               try {
                  for (Object o : connection.getMany(queue.size())) {
                     queue.poll().set(o);
                  }
               } catch (JedisConnectionException jce) {
                  this.log.error("Error with connection to " + nodeKey, jce);
                  pipelinedResponsesIterator.remove();
                  this.connections.remove(nodeKey);
                  IOUtils.closeQuietly(connection);
               } finally {
                  countDownLatch.countDown();
               }
            });
         }

         try {
            countDownLatch.await();
         } catch (InterruptedException e) {
            this.log.error("Thread is interrupted during sync.", e);
         }

         executorService.shutdownNow();
         this.syncing = false;
      }
   }

   @Deprecated
   public Response<Long> waitReplicas(int replicas, long timeout) {
      return this.appendCommand(this.commandObjects.waitReplicas(replicas, timeout));
   }
}
