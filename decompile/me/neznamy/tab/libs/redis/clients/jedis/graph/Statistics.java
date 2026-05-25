package me.neznamy.tab.libs.redis.clients.jedis.graph;

@Deprecated
public interface Statistics {
   int nodesCreated();

   int nodesDeleted();

   int indicesCreated();

   int indicesDeleted();

   int labelsAdded();

   int relationshipsDeleted();

   int relationshipsCreated();

   int propertiesSet();

   boolean cachedExecution();

   String queryIntervalExecutionTime();
}
