package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.commands.ProtocolCommand;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;

@Deprecated
public class GraphCommandObjects {
   private final RedisGraphCommands graph;
   private final Connection connection;
   private final ConnectionProvider provider;
   private Function<ProtocolCommand, CommandArguments> commArgs = comm -> new CommandArguments(comm);
   private final ConcurrentHashMap<String, Builder<ResultSet>> builders = new ConcurrentHashMap<>();

   public GraphCommandObjects(RedisGraphCommands graphCommands) {
      this.graph = graphCommands;
      this.connection = null;
      this.provider = null;
   }

   public GraphCommandObjects(Connection connection) {
      this.graph = null;
      this.connection = connection;
      this.provider = null;
   }

   public GraphCommandObjects(ConnectionProvider provider) {
      this.graph = null;
      this.connection = null;
      this.provider = provider;
   }

   public void setBaseCommandArgumentsCreator(Function<ProtocolCommand, CommandArguments> commArgs) {
      this.commArgs = commArgs;
   }

   public final CommandObject<ResultSet> graphQuery(String name, String query) {
      return new CommandObject<>(
         this.commArgs.apply(GraphProtocol.GraphCommand.QUERY).key(name).add(query).add(GraphProtocol.GraphKeyword.__COMPACT), this.getBuilder(name)
      );
   }

   public final CommandObject<ResultSet> graphReadonlyQuery(String name, String query) {
      return new CommandObject<>(
         this.commArgs.apply(GraphProtocol.GraphCommand.RO_QUERY).key(name).add(query).add(GraphProtocol.GraphKeyword.__COMPACT), this.getBuilder(name)
      );
   }

   public final CommandObject<ResultSet> graphQuery(String name, String query, long timeout) {
      return this.graphQuery(name, GraphQueryParams.queryParams(query).timeout(timeout));
   }

   public final CommandObject<ResultSet> graphReadonlyQuery(String name, String query, long timeout) {
      return this.graphQuery(name, GraphQueryParams.queryParams().readonly().query(query).timeout(timeout));
   }

   public final CommandObject<ResultSet> graphQuery(String name, String query, Map<String, Object> params) {
      return this.graphQuery(name, GraphQueryParams.queryParams(query).params(params));
   }

   public final CommandObject<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params) {
      return this.graphQuery(name, GraphQueryParams.queryParams().readonly().query(query).params(params));
   }

   public final CommandObject<ResultSet> graphQuery(String name, String query, Map<String, Object> params, long timeout) {
      return this.graphQuery(name, GraphQueryParams.queryParams(query).params(params).timeout(timeout));
   }

   public final CommandObject<ResultSet> graphReadonlyQuery(String name, String query, Map<String, Object> params, long timeout) {
      return this.graphQuery(name, GraphQueryParams.queryParams().readonly().query(query).params(params).timeout(timeout));
   }

   private CommandObject<ResultSet> graphQuery(String name, GraphQueryParams params) {
      return new CommandObject<>(
         this.commArgs.apply(!params.isReadonly() ? GraphProtocol.GraphCommand.QUERY : GraphProtocol.GraphCommand.RO_QUERY).key(name).addParams(params),
         this.getBuilder(name)
      );
   }

   public final CommandObject<String> graphDelete(String name) {
      return new CommandObject<>(this.commArgs.apply(GraphProtocol.GraphCommand.DELETE).key(name), BuilderFactory.STRING);
   }

   private Builder<ResultSet> getBuilder(String graphName) {
      if (!this.builders.containsKey(graphName)) {
         this.createBuilder(graphName);
      }

      return this.builders.get(graphName);
   }

   private void createBuilder(String graphName) {
      this.builders.computeIfAbsent(graphName, graphNameKey -> new ResultSetBuilder(new GraphCommandObjects.GraphCacheImpl(graphNameKey)));
   }

   private class GraphCacheImpl implements GraphCache {
      private final GraphCommandObjects.GraphCacheList labels;
      private final GraphCommandObjects.GraphCacheList propertyNames;
      private final GraphCommandObjects.GraphCacheList relationshipTypes;

      public GraphCacheImpl(String graphName) {
         this.labels = GraphCommandObjects.this.new GraphCacheList(graphName, "db.labels");
         this.propertyNames = GraphCommandObjects.this.new GraphCacheList(graphName, "db.propertyKeys");
         this.relationshipTypes = GraphCommandObjects.this.new GraphCacheList(graphName, "db.relationshipTypes");
      }

      @Override
      public String getLabel(int index) {
         return this.labels.getCachedData(index);
      }

      @Override
      public String getRelationshipType(int index) {
         return this.relationshipTypes.getCachedData(index);
      }

      @Override
      public String getPropertyName(int index) {
         return this.propertyNames.getCachedData(index);
      }
   }

   private class GraphCacheList {
      private final String name;
      private final String query;
      private final List<String> data = new CopyOnWriteArrayList<>();
      private final Lock dataLock = new ReentrantLock(true);

      public GraphCacheList(String name, String procedure) {
         this.name = name;
         this.query = "CALL " + procedure + "()";
      }

      public String getCachedData(int index) {
         if (index >= this.data.size()) {
            this.dataLock.lock();

            try {
               if (index >= this.data.size()) {
                  this.getProcedureInfo();
               }
            } finally {
               this.dataLock.unlock();
            }
         }

         return this.data.get(index);
      }

      private void getProcedureInfo() {
         ResultSet resultSet = this.callProcedure();
         Iterator<Record> it = resultSet.iterator();
         List<String> newData = new ArrayList<>();

         for (int i = 0; it.hasNext(); i++) {
            Record record = it.next();
            if (i >= this.data.size()) {
               newData.add(record.getString(0));
            }
         }

         this.data.addAll(newData);
      }

      private ResultSet callProcedure() {
         if (GraphCommandObjects.this.graph != null) {
            return GraphCommandObjects.this.graph.graphQuery(this.name, this.query);
         }

         CommandObject<ResultSet> commandObject = new CommandObject<>(
            new CommandArguments(GraphProtocol.GraphCommand.QUERY).key(this.name).add(this.query).add(GraphProtocol.GraphKeyword.__COMPACT),
            GraphCommandObjects.this.getBuilder(this.name)
         );
         if (GraphCommandObjects.this.connection != null) {
            return GraphCommandObjects.this.connection.executeCommand(commandObject);
         }

         try (Connection provided = GraphCommandObjects.this.provider.getConnection(commandObject.getArguments())) {
            return provided.executeCommand(commandObject);
         }
      }
   }
}
