package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

@Deprecated
public class GraphQueryParams implements IParams {
   private boolean readonly;
   private String query;
   private Map<String, Object> params;
   private Long timeout;

   public GraphQueryParams() {
   }

   public static GraphQueryParams queryParams() {
      return new GraphQueryParams();
   }

   public GraphQueryParams(String query) {
      this.query = query;
   }

   public static GraphQueryParams queryParams(String query) {
      return new GraphQueryParams(query);
   }

   public GraphQueryParams readonly() {
      return this.readonly(true);
   }

   public GraphQueryParams readonly(boolean readonly) {
      this.readonly = readonly;
      return this;
   }

   public GraphQueryParams query(String queryStr) {
      this.query = queryStr;
      return this;
   }

   public GraphQueryParams params(Map<String, Object> params) {
      this.params = params;
      return this;
   }

   public GraphQueryParams addParam(String key, Object value) {
      if (this.params == null) {
         this.params = new HashMap<>();
      }

      this.params.put(key, value);
      return this;
   }

   public GraphQueryParams timeout(long timeout) {
      this.timeout = timeout;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.query == null) {
         throw new JedisException("Query string must be set.");
      }

      if (this.params == null) {
         args.add(this.query);
      } else {
         args.add(RedisGraphQueryUtil.prepareQuery(this.query, this.params));
      }

      args.add(GraphProtocol.GraphKeyword.__COMPACT);
      if (this.timeout != null) {
         args.add(GraphProtocol.GraphKeyword.TIMEOUT).add(this.timeout).blocking();
      }
   }

   public boolean isReadonly() {
      return this.readonly;
   }
}
