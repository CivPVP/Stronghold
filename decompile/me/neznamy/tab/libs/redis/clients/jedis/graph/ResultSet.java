package me.neznamy.tab.libs.redis.clients.jedis.graph;

@Deprecated
public interface ResultSet extends Iterable<Record> {
   int size();

   Header getHeader();

   Statistics getStatistics();

   enum ColumnType {
      UNKNOWN,
      SCALAR,
      NODE,
      RELATION;
   }
}
