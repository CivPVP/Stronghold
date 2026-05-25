package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class IndexOptions implements IParams {
   public static final int USE_TERM_OFFSETS = 1;
   public static final int KEEP_FIELD_FLAGS = 2;
   public static final int KEEP_TERM_FREQUENCIES = 8;
   public static final int DEFAULT_FLAGS = 11;
   private final int flags;
   private List<String> stopwords;
   private long expire = 0L;
   private IndexDefinition definition;

   public IndexOptions(int flags) {
      this.flags = flags;
   }

   public static IndexOptions defaultOptions() {
      return new IndexOptions(11);
   }

   public IndexOptions setStopwords(String... stopwords) {
      this.stopwords = Arrays.asList(stopwords);
      return this;
   }

   public IndexOptions setNoStopwords() {
      this.stopwords = new ArrayList<>(0);
      return this;
   }

   public IndexOptions setTemporary(long expire) {
      this.expire = expire;
      return this;
   }

   public IndexDefinition getDefinition() {
      return this.definition;
   }

   public IndexOptions setDefinition(IndexDefinition definition) {
      this.definition = definition;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.definition != null) {
         this.definition.addParams(args);
      }

      if ((this.flags & 1) == 0) {
         args.add(SearchProtocol.SearchKeyword.NOOFFSETS.name());
      }

      if ((this.flags & 2) == 0) {
         args.add(SearchProtocol.SearchKeyword.NOFIELDS.name());
      }

      if ((this.flags & 8) == 0) {
         args.add(SearchProtocol.SearchKeyword.NOFREQS.name());
      }

      if (this.expire > 0L) {
         args.add(SearchProtocol.SearchKeyword.TEMPORARY.name());
         args.add(Long.toString(this.expire));
      }

      if (this.stopwords != null) {
         args.add(SearchProtocol.SearchKeyword.STOPWORDS.name());
         args.add(Integer.toString(this.stopwords.size()));
         if (!this.stopwords.isEmpty()) {
            args.addObjects(this.stopwords);
         }
      }
   }
}
