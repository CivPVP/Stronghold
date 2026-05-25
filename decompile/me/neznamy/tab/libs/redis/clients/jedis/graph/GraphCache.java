package me.neznamy.tab.libs.redis.clients.jedis.graph;

@Deprecated
interface GraphCache {
   String getLabel(int var1);

   String getRelationshipType(int var1);

   String getPropertyName(int var1);
}
