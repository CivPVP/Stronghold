package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.Collection;
import java.util.function.IntFunction;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.RedisProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.JedisCommandIterationBase;

public class FtSearchIteration extends JedisCommandIterationBase<SearchResult, Document> {
   private int batchStart;
   private final int batchSize;
   private final IntFunction<CommandArguments> args;

   public FtSearchIteration(ConnectionProvider connectionProvider, int batchSize, String indexName, String query, FTSearchParams params) {
      this(connectionProvider, null, batchSize, indexName, query, params);
   }

   public FtSearchIteration(ConnectionProvider connectionProvider, int batchSize, String indexName, Query query) {
      this(connectionProvider, null, batchSize, indexName, query);
   }

   public FtSearchIteration(ConnectionProvider connectionProvider, RedisProtocol protocol, int batchSize, String indexName, String query, FTSearchParams params) {
      super(
         connectionProvider,
         protocol == RedisProtocol.RESP3
            ? SearchResult.SEARCH_RESULT_BUILDER
            : new SearchResult.SearchResultBuilder(!params.getNoContent(), params.getWithScores(), true)
      );
      this.batchSize = batchSize;
      this.args = limitFirst -> new CommandArguments(SearchProtocol.SearchCommand.SEARCH)
         .add(indexName)
         .add(query)
         .addParams(params.limit(limitFirst, this.batchSize));
   }

   public FtSearchIteration(ConnectionProvider connectionProvider, RedisProtocol protocol, int batchSize, String indexName, Query query) {
      super(
         connectionProvider,
         protocol == RedisProtocol.RESP3
            ? SearchResult.SEARCH_RESULT_BUILDER
            : new SearchResult.SearchResultBuilder(!query.getNoContent(), query.getWithScores(), true)
      );
      this.batchSize = batchSize;
      this.args = limitFirst -> new CommandArguments(SearchProtocol.SearchCommand.SEARCH).add(indexName).addParams(query.limit(limitFirst, this.batchSize));
   }

   protected boolean isNodeCompleted(SearchResult reply) {
      return this.batchStart >= reply.getTotalResults() - this.batchSize;
   }

   @Override
   protected CommandArguments initCommandArguments() {
      this.batchStart = 0;
      return this.args.apply(this.batchStart);
   }

   protected CommandArguments nextCommandArguments(SearchResult lastReply) {
      this.batchStart = this.batchStart + this.batchSize;
      return this.args.apply(this.batchStart);
   }

   protected Collection<Document> convertBatchToData(SearchResult batch) {
      return batch.getDocuments();
   }
}
