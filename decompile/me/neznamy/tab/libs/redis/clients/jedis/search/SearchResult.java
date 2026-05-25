package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Internal;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class SearchResult {
   private final long totalResults;
   private final List<Document> documents;
   public static Builder<SearchResult> SEARCH_RESULT_BUILDER = new SearchResult.PerFieldDecoderSearchResultBuilder(Document.SEARCH_DOCUMENT);

   private SearchResult(long totalResults, List<Document> documents) {
      this.totalResults = totalResults;
      this.documents = documents;
   }

   public long getTotalResults() {
      return this.totalResults;
   }

   public List<Document> getDocuments() {
      return Collections.unmodifiableList(this.documents);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{Total results:" + this.totalResults + ", Documents:" + this.documents + "}";
   }

   @Internal
   public static final class PerFieldDecoderSearchResultBuilder extends Builder<SearchResult> {
      private static final String TOTAL_RESULTS_STR = "total_results";
      private static final String RESULTS_STR = "results";
      private final Builder<Document> documentBuilder;

      public PerFieldDecoderSearchResultBuilder(Map<String, Boolean> isFieldDecode) {
         this(new Document.PerFieldDecoderDocumentBuilder(isFieldDecode));
      }

      private PerFieldDecoderSearchResultBuilder(Builder<Document> builder) {
         this.documentBuilder = Objects.requireNonNull(builder);
      }

      public SearchResult build(Object data) {
         List<KeyValue> list = (List<KeyValue>)data;
         long totalResults = -1L;
         List<Document> results = null;

         for (KeyValue kv : list) {
            String key = BuilderFactory.STRING.build(kv.getKey());
            switch (key) {
               case "total_results":
                  totalResults = BuilderFactory.LONG.build(kv.getValue());
                  break;
               case "results":
                  results = ((List)kv.getValue()).stream().map(this.documentBuilder::build).collect(Collectors.toList());
            }
         }

         return new SearchResult(totalResults, results);
      }
   }

   public static class SearchResultBuilder extends Builder<SearchResult> {
      private final boolean hasContent;
      private final boolean hasScores;
      private final boolean decode;
      private final Map<String, Boolean> isFieldDecode;

      public SearchResultBuilder(boolean hasContent, boolean hasScores, boolean decode) {
         this(hasContent, hasScores, decode, null);
      }

      public SearchResultBuilder(boolean hasContent, boolean hasScores, boolean decode, Map<String, Boolean> isFieldDecode) {
         this.hasContent = hasContent;
         this.hasScores = hasScores;
         this.decode = decode;
         this.isFieldDecode = isFieldDecode;
      }

      public SearchResult build(Object data) {
         List<Object> resp = (List<Object>)data;
         int step = 1;
         int scoreOffset = 0;
         int contentOffset = 1;
         if (this.hasScores) {
            step++;
            scoreOffset = 1;
            contentOffset++;
         }

         if (this.hasContent) {
            step++;
         }

         long totalResults = (Long)resp.get(0);
         List<Document> documents = new ArrayList<>(resp.size() - 1);

         for (int i = 1; i < resp.size(); i += step) {
            String id = BuilderFactory.STRING.build(resp.get(i));
            double score = this.hasScores ? BuilderFactory.DOUBLE.build(resp.get(i + scoreOffset)) : 1.0;
            List<byte[]> fields = this.hasContent ? (List)resp.get(i + contentOffset) : null;
            documents.add(Document.load(id, score, fields, this.decode, this.isFieldDecode));
         }

         return new SearchResult(totalResults, documents);
      }
   }
}
