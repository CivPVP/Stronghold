package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.List;

@Deprecated
public interface Header {
   List<ResultSet.ColumnType> getSchemaTypes();

   List<String> getSchemaNames();
}
